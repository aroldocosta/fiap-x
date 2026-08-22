package br.com.fiapx.videoapi.listener;

import br.com.fiapx.common.event.VideoStatusEvent;
import br.com.fiapx.videoapi.domain.VideoStatus;
import br.com.fiapx.videoapi.service.VideoService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VideoStatusListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoStatusListener.class);

    private final VideoService videoService;

    public VideoStatusListener(VideoService videoService) {
        this.videoService = videoService;
    }

    @SqsListener("${app.sqs.queues.video-status}")
    public void handle(VideoStatusEvent event) {
        LOGGER.info("Received video status event: videoId={} status={}", event.videoId(), event.status());
        videoService.updateVideoStatus(
                event.videoId(),
                VideoStatus.valueOf(event.status()),
                event.zipStoragePath(),
                event.errorMessage()
        );
    }
}
