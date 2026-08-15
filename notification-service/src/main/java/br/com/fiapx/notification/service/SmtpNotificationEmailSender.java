package br.com.fiapx.notification.service;

import br.com.fiapx.common.event.VideoStatusEvent;
import br.com.fiapx.notification.config.NotificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpNotificationEmailSender implements NotificationEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpNotificationEmailSender.class);

    private final JavaMailSender javaMailSender;
    private final NotificationProperties notificationProperties;

    public SmtpNotificationEmailSender(JavaMailSender javaMailSender, NotificationProperties notificationProperties) {
        this.javaMailSender = javaMailSender;
        this.notificationProperties = notificationProperties;
    }

    @Override
    public void send(VideoStatusEvent event, String status, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(notificationProperties.fromEmail());
        mailMessage.setTo(event.userEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        javaMailSender.send(mailMessage);

        LOGGER.info(
                "Notification email sent: userId={} userEmail={} videoId={} status={} from={} to={} subject={} message={}",
                event.userId(),
                event.userEmail(),
                event.videoId(),
                status,
                mailMessage.getFrom(),
                event.userEmail(),
                subject,
                message
        );
    }
}
