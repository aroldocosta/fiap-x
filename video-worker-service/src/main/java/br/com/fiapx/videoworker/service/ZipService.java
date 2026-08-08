package br.com.fiapx.videoworker.service;

import br.com.fiapx.videoworker.config.ProcessingProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

    private final Path zipsRoot;

    public ZipService(ProcessingProperties properties) {
        this.zipsRoot = Path.of(properties.storage().zipsDir()).toAbsolutePath().normalize();
    }

    public Path createZip(UUID videoId, Path framesDirectory) {
        try {
            Files.createDirectories(zipsRoot);
            Path zipPath = zipsRoot.resolve(videoId + ".zip");
            List<Path> frameFiles;
            try (Stream<Path> pathStream = Files.list(framesDirectory)) {
                frameFiles = pathStream
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .toList();
            }

            if (frameFiles.isEmpty()) {
                throw new IllegalStateException("No frames generated for video " + videoId);
            }

            try (OutputStream outputStream = Files.newOutputStream(zipPath);
                 ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                for (Path frameFile : frameFiles) {
                    zipOutputStream.putNextEntry(new ZipEntry(frameFile.getFileName().toString()));
                    try (InputStream inputStream = Files.newInputStream(frameFile)) {
                        inputStream.transferTo(zipOutputStream);
                    }
                    zipOutputStream.closeEntry();
                }
            }
            return zipPath;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create ZIP archive", ex);
        }
    }
}
