package br.com.fiapx.videoworker.service;

import br.com.fiapx.common.config.SqsQueueNames;
import br.com.fiapx.common.event.VideoNotificationEvent;
import br.com.fiapx.common.event.VideoProcessingEvent;
import br.com.fiapx.videoworker.config.ProcessingProperties;
import br.com.fiapx.videoworker.domain.Video;
import br.com.fiapx.videoworker.domain.VideoStatus;
import br.com.fiapx.videoworker.repository.VideoRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VideoProcessingOrchestrator {

    private final VideoRepository videoRepository;
    private final FfmpegService ffmpegService;
    private final ZipService zipService;
    private final SqsTemplate sqsTemplate;
    private final Path framesRoot;

    public VideoProcessingOrchestrator(
            VideoRepository videoRepository,
            FfmpegService ffmpegService,
            ZipService zipService,
            SqsTemplate sqsTemplate,
            ProcessingProperties processingProperties) {
        this.videoRepository = videoRepository;
        this.ffmpegService = ffmpegService;
        this.zipService = zipService;
        this.sqsTemplate = sqsTemplate;
        this.framesRoot = Path.of(processingProperties.storage().framesDir()).toAbsolutePath().normalize();
    }

    public void process(VideoProcessingEvent event) {
        Video video = videoRepository.findById(event.videoId())
                .orElseThrow(() -> new VideoNotFoundException("Video not found: " + event.videoId()));

        try {
            markProcessing(video);
            Path framesDirectory = framesRoot.resolve(safeUserDirectory(event.userId())).resolve(event.videoId().toString());
            ffmpegService.extractFrames(Path.of(event.originalStoragePath()).toAbsolutePath().normalize(), framesDirectory);
            Path zipPath = zipService.createZip(event.videoId(), framesDirectory);
            markFinished(video, zipPath);
            publishNotification(video, "Video processing finished", "Your video has been processed successfully.");
        } catch (Exception ex) {
            markError(event.videoId(), ex);
            publishErrorNotification(event, ex);
        }
    }

    @Transactional
    protected void markProcessing(Video video) {
        video.setStatus(VideoStatus.PROCESSING);
        video.setErrorMessage(null);
        videoRepository.save(video);
    }

    @Transactional
    protected void markFinished(Video video, Path zipPath) {
        video.setStatus(VideoStatus.FINISHED);
        video.setZipStoragePath(zipPath.toString());
        video.setErrorMessage(null);
        videoRepository.save(video);
    }

    @Transactional
    protected void markError(UUID videoId, Exception ex) {
        videoRepository.findById(videoId).ifPresent(video -> {
            video.setStatus(VideoStatus.ERROR);
            video.setErrorMessage(truncateError(ex.getMessage()));
            videoRepository.save(video);
        });
    }

    private void publishNotification(Video video, String subject, String message) {
        sqsTemplate.send(to -> to.queue(SqsQueueNames.VIDEO_NOTIFICATION_QUEUE).payload(
                new VideoNotificationEvent(
                        video.getId(),
                        video.getUserId(),
                        video.getUserEmail(),
                        subject,
                        message,
                        LocalDateTime.now()
                )
        ));
    }

    private void publishErrorNotification(VideoProcessingEvent event, Exception ex) {
        sqsTemplate.send(to -> to.queue(SqsQueueNames.VIDEO_NOTIFICATION_QUEUE).payload(
                new VideoNotificationEvent(
                        event.videoId(),
                        event.userId(),
                        event.userEmail(),
                        "Video processing failed",
                        truncateError(ex.getMessage()),
                        LocalDateTime.now()
                )
        ));
    }

    private String safeUserDirectory(UUID userId) {
        return userId.toString();
    }

    private String truncateError(String message) {
        if (message == null || message.isBlank()) {
            return "Unknown processing error";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
