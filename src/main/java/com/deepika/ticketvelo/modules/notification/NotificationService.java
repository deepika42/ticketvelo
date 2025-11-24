package com.deepika.ticketvelo.modules.notification;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @KafkaListener(topics = "ticket-updates", groupId = "ticketvelo-group")
    public void listen(String message) {
        // Simulate a slow email server
        try {
            System.out.println("📧 Worker received: " + message);
            System.out.println("... Sending email (Simulating 2s delay) ...");
            Thread.sleep(2000);
            System.out.println("Email Sent!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}