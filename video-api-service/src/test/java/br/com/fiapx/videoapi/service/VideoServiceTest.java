package br.com.fiapx.videoapi.service;

import br.com.fiapx.videoapi.config.StorageProperties;
import br.com.fiapx.videoapi.domain.Video;
import br.com.fiapx.videoapi.domain.VideoStatus;
import br.com.fiapx.videoapi.repository.VideoRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VideoServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldUpdateStatusAndMetadata() {
        VideoRepository repository = mock(VideoRepository.class);
        Video video = new Video();
        video.setUserId(UUID.randomUUID());
        video.setUserEmail("user@fiapx.com");
        video.setTitle("video");
        video.setOriginalFileName("video.mp4");
        video.setOriginalStoragePath("/tmp/video.mp4");
        video.setStatus(VideoStatus.PENDING);

        UUID videoId = UUID.randomUUID();
        when(repository.findById(videoId)).thenReturn(Optional.of(video));

        VideoService service = new VideoService(
                repository,
                mock(SqsTemplate.class),
                new StorageProperties(tempDir.toString(), tempDir.resolve("frames").toString(), tempDir.resolve("zips").toString()),
                mock(Environment.class)
        );

        service.updateVideoStatus(videoId, VideoStatus.FINISHED, "/tmp/video.zip", null);

        assertThat(video.getStatus()).isEqualTo(VideoStatus.FINISHED);
        assertThat(video.getZipStoragePath()).isEqualTo("/tmp/video.zip");
        assertThat(video.getErrorMessage()).isNull();
    }

    @Test
    void shouldThrowWhenVideoDoesNotExistDuringStatusUpdate() {
        VideoRepository repository = mock(VideoRepository.class);
        UUID videoId = UUID.randomUUID();
        when(repository.findById(videoId)).thenReturn(Optional.empty());

        VideoService service = new VideoService(
                repository,
                mock(SqsTemplate.class),
                new StorageProperties(tempDir.toString(), tempDir.resolve("frames").toString(), tempDir.resolve("zips").toString()),
                mock(Environment.class)
        );

        assertThatThrownBy(() -> service.updateVideoStatus(videoId, VideoStatus.ERROR, null, "failure"))
                .isInstanceOf(VideoNotFoundException.class)
                .hasMessage("Video not found");
    }
}
