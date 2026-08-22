package br.com.fiapx.videoapi.listener;

import br.com.fiapx.common.event.VideoStatusEvent;
import br.com.fiapx.videoapi.domain.VideoStatus;
import br.com.fiapx.videoapi.service.VideoService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VideoStatusListenerTest {

    @Test
    void shouldForwardStatusEventToVideoService() {
        VideoService videoService = mock(VideoService.class);
        VideoStatusListener listener = new VideoStatusListener(videoService);
        UUID videoId = UUID.randomUUID();

        listener.handle(new VideoStatusEvent(
                videoId,
                UUID.randomUUID(),
                "user@fiapx.com",
                "FINISHED",
                "/tmp/video.zip",
                null,
                LocalDateTime.now()
        ));

        verify(videoService).updateVideoStatus(videoId, VideoStatus.FINISHED, "/tmp/video.zip", null);
    }
}
