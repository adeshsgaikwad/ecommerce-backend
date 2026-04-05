package com.ecommerce.ecommerce_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// @Async: all email sends are non-blocking — they run in a separate thread
// so the API response is never delayed by email delivery
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        sendEmail(toEmail,
                "Email Verification — Your OTP",
                "Your OTP is: " + otp + "\nThis OTP expires in 5 minutes.");
        log.info("OTP email sent to {}", toEmail);
    }

    @Async
    public void sendOrderConfirmationEmail(String toEmail, Long orderId) {
        sendEmail(toEmail,
                "Order Confirmed — #" + orderId,
                "Your order #" + orderId + " has been confirmed. Thank you for shopping with us!");
        log.info("Order confirmation email sent to {} for orderId {}", toEmail, orderId);
    }

    @Async
    public void sendSellerApprovalEmail(String toEmail, String shopName) {
        sendEmail(toEmail,
                "Seller Account Approved",
                "Congratulations! Your shop \"" + shopName + "\" has been approved. You can now add products.");
        log.info("Seller approval email sent to {}", toEmail);
    }

    @Async
    public void sendSellerRejectionEmail(String toEmail, String shopName, String reason) {
        sendEmail(toEmail,
                "Seller Application Update",
                "Your application for \"" + shopName + "\" was not approved.\nReason: " + reason);
        log.info("Seller rejection email sent to {}", toEmail);
    }

    @Async
    public void sendNewOrderAlertToSeller(String sellerEmail, Long orderId) {
        sendEmail(sellerEmail,
                "New Order Received — #" + orderId,
                "You have received a new order #" + orderId + ". Please log in to process it.");
        log.info("New order alert sent to seller {} for orderId {}", sellerEmail, orderId);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}