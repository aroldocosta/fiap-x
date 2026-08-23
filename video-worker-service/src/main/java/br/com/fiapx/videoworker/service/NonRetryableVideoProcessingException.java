package br.com.fiapx.videoworker.service;

public class NonRetryableVideoProcessingException extends IllegalStateException {

    public NonRetryableVideoProcessingException(String message) {
        super(message);
    }

    public NonRetryableVideoProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
