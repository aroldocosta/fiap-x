package br.com.fiapx.videoworker.service;

import br.com.fiapx.common.config.SqsQueueNames;
import br.com.fiapx.common.event.VideoProcessingEvent;
import br.com.fiapx.common.event.VideoStatusEvent;
import br.com.fiapx.videoworker.config.ProcessingProperties;
import br.com.fiapx.videoworker.domain.VideoStatus;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class VideoProcessingOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoProcessingOrchestrator.class);

    private final FfmpegService ffmpegService;
    private final ZipService zipService;
    private final SqsTemplate sqsTemplate;
    private final Path framesRoot;
    private final Environment environment;

    public VideoProcessingOrchestrator(
            FfmpegService ffmpegService,
            ZipService zipService,
            SqsTemplate sqsTemplate,
            ProcessingProperties processingProperties,
            Environment environment) {
        this.ffmpegService = ffmpegService;
        this.zipService = zipService;
        this.sqsTemplate = sqsTemplate;
        this.framesRoot = Path.of(processingProperties.storage().framesDir()).toAbsolutePath().normalize();
        this.environment = environment;
    }

    public void process(VideoProcessingEvent event) {
        Path framesDirectory = framesRoot.resolve(safeUserDirectory(event.userId())).resolve(event.videoId().toString());

        try {
            publishStatus(event, VideoStatus.PROCESSING, null, null);
            ffmpegService.extractFrames(Path.of(event.originalStoragePath()).toAbsolutePath().normalize(), framesDirectory);
            Path zipPath = zipService.createZip(event.videoId(), framesDirectory);
            publishStatus(event, VideoStatus.FINISHED, zipPath, null);
        } catch (NonRetryableVideoProcessingException ex) {
            publishStatus(event, VideoStatus.ERROR, null, truncateError(ex.getMessage()));
        } catch (Exception ex) {
            publishStatus(event, VideoStatus.ERROR, null, truncateError(ex.getMessage()));
            throw ex instanceof RuntimeException runtimeException
                    ? runtimeException
                    : new IllegalStateException("Video processing failed", ex);
        } finally {
            deleteFramesDirectory(framesDirectory);
        }
    }

    private void publishStatus(VideoProcessingEvent sourceEvent, VideoStatus status, Path zipPath, String errorMessage) {
        VideoStatusEvent event = new VideoStatusEvent(
                sourceEvent.videoId(),
                sourceEvent.userId(),
                sourceEvent.userEmail(),
                status.name(),
                zipPath != null ? zipPath.toString() : null,
                errorMessage,
                LocalDateTime.now()
        );
        sendStatusEvent(SqsQueueNames.VIDEO_STATUS_API_QUEUE, event);
        sendStatusEvent(SqsQueueNames.VIDEO_STATUS_NOTIFICATION_QUEUE, event);
    }

    private void sendStatusEvent(String queueName, VideoStatusEvent event) {
        logLocalSqsSend(queueName, event);
        sqsTemplate.send(to -> to.queue(queueName).payload(event));
    }

    private void logLocalSqsSend(String queueName, Object payload) {
        if (environment.matchesProfiles("local")) {
            LOGGER.info("[LOCAL SQS SEND] queue={} payload={}", queueName, payload);
        }
    }

    private String safeUserDirectory(UUID userId) {
        return userId.toString();
    }

    private void deleteFramesDirectory(Path framesDirectory) {
        if (Files.notExists(framesDirectory)) {
            return;
        }

        try (Stream<Path> pathStream = Files.walk(framesDirectory)) {
            pathStream
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            LOGGER.warn("Falha ao deletar arquivo temporario de frames: {}", path, ex);
                        }
                    });
        } catch (IOException ex) {
            LOGGER.warn("Falha ao percorrer diretorio temporario de frames: {}", framesDirectory, ex);
        }
    }

    private String truncateError(String message) {
        if (message == null || message.isBlank()) {
            return "Unknown processing error";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
