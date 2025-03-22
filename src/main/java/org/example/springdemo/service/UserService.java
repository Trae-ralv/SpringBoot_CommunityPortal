package org.example.springdemo.service;

import org.example.springdemo.model.ForgetPasswordModel;
import org.example.springdemo.model.UserModel;
import org.example.springdemo.repository.AppRepository;
import org.example.springdemo.repository.ForgetPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    @Value("${spring.mail.username}")
    private String fromEmail; // Inject from application.properties

    @Autowired
    private AppRepository userRepo;

    @Autowired
    private ForgetPasswordRepository forgetPasswordRepo;

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendPasswordResetEmail(String email) {
        LOGGER.info("Received email input: " + email);
        UserModel user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            LOGGER.warning("No user found with email: " + email);
            return false;
        }
        LOGGER.info("User found: " + user.getEmail() + ", ID: " + user.getUser_id());

        String verificationCode = generateVerificationCode();
        LOGGER.info("Generated verification code: " + verificationCode);

        LocalDateTime expiry = LocalDateTime.now().plusHours(12);
        ForgetPasswordModel forgetPassword = new ForgetPasswordModel();
        forgetPassword.setFp_user_id(user.getUser_id());
        forgetPassword.setFp_verification_code(verificationCode);
        forgetPassword.setFp_expiry(expiry);
        forgetPassword.setFp_is_used(false);

        if (!saveForgetPasswordEntry(forgetPassword)) {
            return false;
        }

        return sendEmail(user, "Password Reset Request",
                buildPasswordResetEmailText(user.getF_name(), verificationCode, expiry));
    }

    public boolean validateVerificationCode(String email, String verificationCode) {
        LOGGER.info("Validating code: " + verificationCode + " for email: " + email);
        UserModel user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            LOGGER.warning("No user found with email: " + email);
            return false;
        }

        ForgetPasswordModel latestEntry = forgetPasswordRepo.findTopByFpUserIdOrderByFpExpiryDesc(user.getUser_id());
        if (latestEntry == null) {
            LOGGER.warning("No verification code found for user ID: " + user.getUser_id());
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (isCodeValid(latestEntry, verificationCode, now)) {
            LOGGER.info("Verification code is valid");
            latestEntry.setFp_is_used(true);
            forgetPasswordRepo.save(latestEntry);
            return true;
        }
        LOGGER.warning("Invalid, expired, or used code: " + verificationCode);
        return false;
    }

    public boolean sendRegistrationSuccessEmail(UserModel user) {
        LOGGER.info("Sending registration success email to: " + user.getEmail());
        return sendEmail(user, "Welcome to Our App - Registration Successful!",
                buildRegistrationEmailText(user.getF_name()));
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    private boolean saveForgetPasswordEntry(ForgetPasswordModel forgetPassword) {
        try {
            forgetPasswordRepo.save(forgetPassword);
            LOGGER.info("Saved verification code to forget_password table");
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to save to forget_password table: " + e.getMessage());
            return false;
        }
    }

    private boolean sendEmail(UserModel user, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
            LOGGER.info("Email sent successfully to: " + user.getEmail());
            return true;
        } catch (Exception e) {
            LOGGER.severe("Failed to send email: " + e.getMessage());
            return false;
        }
    }

    private String buildPasswordResetEmailText(String firstName, String verificationCode, LocalDateTime expiry) {
        return "Hello " + firstName + ",\n\n" +
                "You requested a password reset. Use the verification code below:\n" +
                "Verification Code: " + verificationCode + "\n\n" +
                "This code expires at " + expiry + " (12 hours from now).\n" +
                "If you didn’t request this, ignore this email.\n\n" +
                "Best regards,\nXYZ Solution Pte Ltd";
    }

    private String buildRegistrationEmailText(String firstName) {
        return "Hello " + firstName + ",\n\n" +
                "Congratulations! Your registration was successful.\n" +
                "You can now log in to your account and explore our app.\n\n" +
                "Best regards,\nXYZ Solution Pte Ltd";
    }

    private boolean isCodeValid(ForgetPasswordModel entry, String verificationCode, LocalDateTime now) {
        return entry.getFp_verification_code().equals(verificationCode) &&
                !entry.isFp_is_used() &&
                now.isBefore(entry.getFp_expiry());
    }
}