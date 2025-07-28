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

    public void sendAccountCreationEmail(String toEmail, Long idNumber, String password ,String role) throws MessagingException {
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
                <li><strong>ID_Number:</strong> %s</li>
                <li><strong>Password:</strong> %s</li>
            </ul>
            <p>Please log in to the system at <a href="https://your-fleet-system.com/login">here</a> and change your password immediately for security.</p>
            <p>If you did not request this account, please contact the system administrator via +254745115711.Charges Apply😂😂</p>
            <p>Best regards,<br>Fleet Management System Team</p>
            """.formatted(formattedRole, idNumber, password);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    public void sendAccountDeletionEmail(String toEmail, Long idNumber, String role) throws MessagingException {
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
            """.formatted(idNumber, formattedRole);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }


    public void sendPasswordChangeEmail(@Email(message = "Invalid email format") String email, Long idNumber, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Your Fleet Management System Password Has Been Changed");

//            String formattedRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();

            String htmlContent = """
                <h2>Password Change Notification</h2>
                <p>Dear %s,</p>
                <p>Your account with ID Number <strong>%s</strong> has had its password changed.</p>
                <p>Here are your new login credentials:</p>
                <ul>
                    <li><strong>ID Number:</strong> %s</li>
                    <li><strong>Password:</strong> %s</li>
                <p>Your password in the Fleet Management System has been successfully changed.</p>
                <p>If you did not initiate this change, please contact the system administrator immediately.</p>
                <p>Best regards,<br>Fleet Management System Team</p>
                """.formatted(idNumber, password);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password change email", e);
        }
    }
}