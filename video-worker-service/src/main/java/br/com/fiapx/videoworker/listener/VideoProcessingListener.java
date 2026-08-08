package br.com.fiapx.videoworker.listener;

import br.com.fiapx.common.event.VideoProcessingEvent;
import br.com.fiapx.videoworker.service.VideoProcessingOrchestrator;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VideoProcessingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoProcessingListener.class);

    private final VideoProcessingOrchestrator orchestrator;

    public VideoProcessingListener(VideoProcessingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @SqsListener("${spring.cloud.aws.sqs.queues.video-uploaded:video-uploaded-queue}")
    public void handle(VideoProcessingEvent event) {
        LOGGER.info("Starting processing for videoId={}", event.videoId());
        orchestrator.process(event);
    }
}
