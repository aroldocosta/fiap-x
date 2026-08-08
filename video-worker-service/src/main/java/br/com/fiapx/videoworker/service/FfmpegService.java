package br.com.fiapx.videoworker.service;

import br.com.fiapx.videoworker.config.ProcessingProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class FfmpegService {

    private final String ffmpegBinaryPath;

    public FfmpegService(ProcessingProperties processingProperties) {
        this.ffmpegBinaryPath = processingProperties.ffmpeg().binaryPath();
    }

    public void extractFrames(Path videoPath, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
            Path ffmpegLog = Files.createTempFile("ffmpeg-", ".log");
            Process process = new ProcessBuilder(buildCommand(videoPath, outputDirectory))
                    .redirectErrorStream(true)
                    .redirectOutput(ffmpegLog.toFile())
                    .start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String output = Files.readString(ffmpegLog);
                throw new IllegalStateException("FFmpeg failed with exit code " + exitCode + ": " + output);
            }
            Files.deleteIfExists(ffmpegLog);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to execute FFmpeg", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("FFmpeg execution was interrupted", ex);
        }
    }

    private List<String> buildCommand(Path videoPath, Path outputDirectory) {
        return List.of(
                ffmpegBinaryPath,
                "-i", videoPath.toString(),
                "-vf", "fps=1",
                outputDirectory.resolve("frame_%04d.png").toString()
        );
    }
}
