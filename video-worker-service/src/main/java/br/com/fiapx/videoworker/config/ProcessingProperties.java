package br.com.fiapx.videoworker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record ProcessingProperties(
        Ffmpeg ffmpeg,
        Storage storage
) {
    public record Ffmpeg(String binaryPath) {
    }

    public record Storage(String framesDir, String zipsDir) {
    }
}
