package br.com.fiapx.notification.controller;

import br.com.fiapx.notification.dto.NotificationResponseDTO;
import br.com.fiapx.notification.security.AuthenticatedUser;
import br.com.fiapx.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(notificationService.listUserNotifications(user));
    }
}
