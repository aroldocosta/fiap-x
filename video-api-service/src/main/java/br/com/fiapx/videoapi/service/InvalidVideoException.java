package br.com.fiapx.videoapi.service;

public class InvalidVideoException extends RuntimeException {

    public InvalidVideoException(String message) {
        super(message);
    }
}
