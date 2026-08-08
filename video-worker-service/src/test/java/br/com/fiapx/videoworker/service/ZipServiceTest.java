package br.com.fiapx.videoworker.service;

import br.com.fiapx.videoworker.config.ProcessingProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

class ZipServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateZipWithGeneratedFrames() throws Exception {
        Path framesDir = tempDir.resolve("frames");
        Path zipsDir = tempDir.resolve("zips");
        Files.createDirectories(framesDir);
        Files.writeString(framesDir.resolve("frame_0001.png"), "frame-1");
        Files.writeString(framesDir.resolve("frame_0002.png"), "frame-2");

        ZipService zipService = new ZipService(new ProcessingProperties(
                new ProcessingProperties.Ffmpeg("ffmpeg"),
                new ProcessingProperties.Storage(framesDir.toString(), zipsDir.toString())
        ));

        Path zipPath = zipService.createZip(UUID.randomUUID(), framesDir);

        assertThat(Files.exists(zipPath)).isTrue();
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            assertThat(zipFile.size()).isEqualTo(2);
        }
    }
}
