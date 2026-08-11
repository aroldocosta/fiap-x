package br.com.fiapx.notification.service;

import br.com.fiapx.common.event.VideoStatusEvent;

public interface NotificationEmailSender {

    void send(VideoStatusEvent event, String status, String subject, String message);
}
