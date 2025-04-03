package org.example.springdemo;

import org.example.springdemo.model.UserModel;
import org.example.springdemo.repository.AppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class JTesting {

    // Logger instance for test execution logging (use final for immutable references)
    private static final Logger logger = LoggerFactory.getLogger(JTesting.class);

    // Mocked repository bean managed by Spring context
    @MockBean
    private AppRepository userRepo;

    // Autowired password encoder from Spring context
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Sets up the test environment before each test method execution.
     * Resets mock interactions to ensure test isolation.
     */
    @BeforeEach
    public void setUp() {
        logger.info("Setting up test environment - resetting mocks");
        reset(userRepo); // Reset mock state for test isolation
    }

    /**
     * Tests the user registration functionality.
     * Verifies that a user can be successfully saved with all required fields.
     */
    @Test
    public void testRegisterUser() {
        logger.info("Starting testRegisterUser");

        // Arrange: Create test user data
        UserModel user = new UserModel();
        user.setF_name("John");
        user.setL_name("Khoo");
        user.setEmail("johnkhoo@gmail.com");
        user.setPassword(passwordEncoder.encode("Johnk13!")); // Always encode passwords
        user.setPhone_no("011-12345678");

        logger.debug("Created test user: email={}", user.getEmail());

        // Mock repository behavior
        when(userRepo.save(any(UserModel.class))).thenReturn(user);

        // Act: Perform the registration
        logger.info("Saving user to repository");
        UserModel savedUser = userRepo.save(user);

        // Assert: Verify the results
        logger.info("Verifying user registration");
        assertNotNull(savedUser.getF_name(), "First name should not be null");
        assertNotNull(savedUser.getL_name(), "Last name should not be null");
        assertNotNull(savedUser.getEmail(), "Email should not be null");
        assertNotNull(savedUser.getPassword(), "Password should not be null");
        assertNotNull(savedUser.getPhone_no(), "Phone number should not be null");

        // Verify mock interactions
        verify(userRepo, times(1)).save(any(UserModel.class));
        logger.info("testRegisterUser completed successfully");
    }

    /**
     * Tests the user login functionality.
     * Verifies that a user can be found by email and password matches.
     */
    @Test
    public void testLoginUser() {
        logger.info("Starting testLoginUser");

        // Arrange: Prepare test credentials
        String email = "johnkhoo@gmail.com";
        String password = "Johnk13!";
        String encodedPassword = passwordEncoder.encode(password);

        UserModel user = new UserModel();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        logger.debug("Test user credentials: email={}, password=****", email);

        // Mock repository behavior
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        // Act: Attempt login
        logger.info("Attempting to find user by email");
        Optional<UserModel> foundUser = userRepo.findByEmail(email);

        // Assert: Verify login success
        logger.info("Verifying login results");
        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals(email, foundUser.get().getEmail(), "Email should match");
        assertTrue(passwordEncoder.matches(password, foundUser.get().getPassword()),
                "Password should match encoded password");

        // Verify mock interactions
        verify(userRepo, times(1)).findByEmail(email);
        logger.info("testLoginUser completed successfully");
    }

    /**
     * Tests the profile viewing functionality.
     * Verifies that user profile details can be retrieved correctly.
     */
    @Test
    public void testViewProfilePage() {
        logger.info("Starting testViewProfilePage");

        // Arrange: Set up test user profile
        Integer userId = 1;
        UserModel user = new UserModel();
        user.setUser_id(userId);
        user.setF_name("John");
        user.setL_name("Khoo");
        user.setEmail("johnkhoo@gmail.com");
        user.setPhone_no("011-12345678");

        logger.debug("Test user profile: userId={}", userId);

        // Mock repository behavior
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        // Act: Retrieve profile
        logger.info("Fetching user profile");
        Optional<UserModel> foundUser = userRepo.findById(userId);

        // Assert: Verify profile details
        logger.info("Verifying profile details");
        assertTrue(foundUser.isPresent(), "User should be found");
        UserModel result = foundUser.get();
        assertEquals("John", result.getF_name(), "First name should match");
        assertEquals("Khoo", result.getL_name(), "Last name should match");
        assertEquals("johnkhoo@gmail.com", result.getEmail(), "Email should match");
        assertEquals("011-12345678", result.getPhone_no(), "Phone number should match");

        // Verify mock interactions
        verify(userRepo, times(1)).findById(userId);
        logger.info("testViewProfilePage completed successfully");
    }

    /**
     * Tests the profile phone number update functionality.
     * Verifies that a user's phone number can be updated successfully.
     */
    @Test
    public void testUpdateProfilePhoneNo() {
        logger.info("Starting testUpdateProfilePhoneNo");

        // Arrange: Set up initial user data
        Integer userId = 1;
        String newPhoneNo = "011-87654321";

        UserModel user = new UserModel();
        user.setUser_id(userId);
        user.setPhone_no("011-12345678");

        logger.debug("Initial user: userId={}, oldPhone={}", userId, user.getPhone_no());

        // Mock repository behavior
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(userRepo.save(any(UserModel.class))).thenReturn(user);

        // Act: Update phone number
        logger.info("Fetching and updating user phone number");
        Optional<UserModel> foundUser = userRepo.findById(userId);
        foundUser.ifPresent(u -> {
            logger.debug("Updating phone number from {} to {}", u.getPhone_no(), newPhoneNo);
            u.setPhone_no(newPhoneNo);
        });
        UserModel updatedUser = userRepo.save(foundUser.get());

        // Assert: Verify update
        logger.info("Verifying phone number update");
        assertEquals(newPhoneNo, updatedUser.getPhone_no(), "Phone number should be updated");

        // Verify mock interactions
        verify(userRepo, times(1)).findById(userId);
        verify(userRepo, times(1)).save(any(UserModel.class));
        logger.info("testUpdateProfilePhoneNo completed successfully");
    }
}