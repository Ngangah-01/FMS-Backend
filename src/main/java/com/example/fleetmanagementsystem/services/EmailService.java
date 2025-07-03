package com.example.fleetmanagementsystem.services;

import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAccountCreationEmail(String toEmail, String username, String password ,String role) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Your Fleet Management System Account Has Been Created");

        String formattedRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();

        String htmlContent = """
            <h2>Welcome to Fleet Management System!</h2>
            <p>Your %s account has been successfully created. Below are your login credentials:</p>
            <ul>
                <li><strong>Username:</strong> %s</li>
                <li><strong>Password:</strong> %s</li>
            </ul>
            <p>Please log in to the system at <a href="https://your-fleet-system.com/login">here</a> and change your password immediately for security.</p>
            <p>If you did not request this account, please contact the system administrator via +254745115711.Charges Apply😂😂</p>
            <p>Best regards,<br>Fleet Management System Team</p>
            """.formatted(formattedRole, username, password);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

//    public void sendAccountCreationEmail(@Email(message = "Invalid email format") String email, String username, String plainPassword) {
//
//        try {
//            sendAccountCreationEmail(email, username, plainPassword, "user");
//        } catch (MessagingException e) {
//            throw new RuntimeException("Failed to send account creation email", e);
//        }
//    }

    public void sendAccountDeletionEmail(String toEmail, String username, String role) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Your Fleet Management System Account Has Been Deleted");

        String formattedRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();

        String htmlContent = """
            <h2>Account Deletion Notification</h2>
            <p>Dear %s,</p>
            <p>Your %s account in the Fleet Management System has been deleted by an administrator.</p>
            <p>If you believe this was done in error, please contact the system administrator immediately.</p>
            <p>Best regards,<br>Fleet Management System Team</p>
            """.formatted(username, formattedRole);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }


}