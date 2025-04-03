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

/**
 * Service class for handling user-related operations such as email notifications and password reset.
 */
@Service
public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName()); // Logger for tracking service operations
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";     // Characters for generating verification codes
    private static final int CODE_LENGTH = 6;                                          // Length of the verification code

    @Value("${spring.mail.username}")
    private String fromEmail; // Email sender address injected from application.properties

    @Autowired
    private AppRepository userRepo; // Repository for UserModel database operations

    @Autowired
    private ForgetPasswordRepository forgetPasswordRepo; // Repository for ForgetPasswordModel database operations

    @Autowired
    private JavaMailSender mailSender; // Spring's email sender utility

    /**
     * Sends a password reset email with a verification code to the user.
     * @param email The user's email address
     * @return True if email is sent successfully, false otherwise
     */
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

    /**
     * Validates a verification code for password reset.
     * @param email The user's email
     * @param verificationCode The code to validate
     * @return True if the code is valid and unused, false otherwise
     */
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

    /**
     * Sends a registration success email to the user.
     * @param user The registered user
     * @return True if email is sent successfully, false otherwise
     */
    public boolean sendRegistrationSuccessEmail(UserModel user) {
        LOGGER.info("Sending registration success email to: " + user.getEmail());
        return sendEmail(user, "Welcome to Our App - Registration Successful!",
                buildRegistrationEmailText(user.getF_name()));
    }

    /**
     * Generates a random verification code.
     * @return A 6-character alphanumeric code
     */
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    /**
     * Saves a password reset entry to the database.
     * @param forgetPassword The entry to save
     * @return True if saved successfully, false if an error occurs
     */
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

    /**
     * Sends an email to the user.
     * @param user The recipient
     * @param subject The email subject
     * @param text The email body
     * @return True if sent successfully, false if an error occurs
     */
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

    /**
     * Builds the text for a password reset email.
     * @param firstName User's first name
     * @param verificationCode The code to include
     * @param expiry The code's expiry time
     * @return Formatted email text
     */
    private String buildPasswordResetEmailText(String firstName, String verificationCode, LocalDateTime expiry) {
        return "Hello " + firstName + ",\n\n" +
                "You requested a password reset. Use the verification code below:\n" +
                "Verification Code: " + verificationCode + "\n\n" +
                "This code expires at " + expiry + " (12 hours from now).\n" +
                "If you didn’t request this, ignore this email.\n\n" +
                "Best regards,\nXYZ Solution Pte Ltd";
    }

    /**
     * Builds the text for a registration success email.
     * @param firstName User's first name
     * @return Formatted email text
     */
    private String buildRegistrationEmailText(String firstName) {
        return "Hello " + firstName + ",\n\n" +
                "Congratulations! Your registration was successful.\n" +
                "You can now log in to your account and explore our app.\n\n" +
                "Best regards,\nXYZ Solution Pte Ltd";
    }

    /**
     * Checks if a verification code is valid, unused, and not expired.
     * @param entry The ForgetPasswordModel entry
     * @param verificationCode The code to check
     * @param now Current time
     * @return True if valid, false otherwise
     */
    private boolean isCodeValid(ForgetPasswordModel entry, String verificationCode, LocalDateTime now) {
        return entry.getFp_verification_code().equals(verificationCode) &&
                !entry.isFp_is_used() &&
                now.isBefore(entry.getFp_expiry());
    }
}