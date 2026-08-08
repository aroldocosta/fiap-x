package br.com.fiapx.videoapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.upload")
public record StorageProperties(String dir) {
}
