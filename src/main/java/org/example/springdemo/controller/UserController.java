package org.example.springdemo.controller;

import org.example.springdemo.model.UserModel;
import org.example.springdemo.repository.AppRepository;
import org.example.springdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {
    // Instance variables for dependency injection
    @Autowired
    private AppRepository userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @GetMapping("/")
    public String showHome(Model model) {
        String dynamicClass = "nav-active";
        model.addAttribute("home", dynamicClass);
        return "index";
    }

    @GetMapping("/login")
    public String showLoginForm(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/profile";
        }
        return "login";
    }

    @GetMapping("/register")
    public String displayRegForm(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/profile";
        }
        model.addAttribute("RegUserData", new UserModel());
        return "register";
    }

    @PostMapping("/register")
    public String submitRegForm(@ModelAttribute("RegUserData") UserModel user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return "redirect:/regsuccess?user_id=" + user.getUser_id();
    }

    @GetMapping("/regsuccess")
    public String showRegistrationSuccess(@RequestParam(value = "user_id", required = false) Integer userId, Model model) {
        if (userId == null) {
            model.addAttribute("errorMessage", "No user ID provided");
            return "regsuccess";
        }

        UserModel user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            model.addAttribute("errorMessage", "No user found with ID: " + userId);
            return "regsuccess";
        }

        model.addAttribute("user", user);
        userService.sendRegistrationSuccessEmail(user); // Delegate to UserService
        return "regsuccess";
    }

    @GetMapping("/forgetpass")
    public String showResetPassword(Model model) {
        model.addAttribute("UserEmail", new UserModel());
        return "forgetpass";
    }

    @PostMapping("/forgetpass")
    public String processForgetPasswordForm(
            @ModelAttribute("UserEmail") UserModel user,
            @RequestParam(value = "verificationCode", required = false) String verificationCode,
            Model model) {
        UserModel existingUser = userRepo.findByEmail(user.getEmail()).orElse(null);

        if (existingUser == null) {
            model.addAttribute("errorMessage", "No account found with this email");
            model.addAttribute("UserEmail", user);
            return "forgetpass";
        }

        if (verificationCode == null || verificationCode.isEmpty()) {
            boolean emailSent = userService.sendPasswordResetEmail(user.getEmail());
            model.addAttribute("UserEmail", user);
            if (emailSent) {
                model.addAttribute("successMessage", "Verification code sent to your email");
                model.addAttribute("showVerificationInput", true);
            } else {
                model.addAttribute("errorMessage", "Failed to send verification email");
            }
            return "forgetpass";
        }

        boolean isValidCode = userService.validateVerificationCode(user.getEmail(), verificationCode);
        if (isValidCode) {
            return "redirect:/resetpass?user_id=" + existingUser.getUser_id();
        }

        model.addAttribute("errorMessage", "Invalid or expired verification code");
        model.addAttribute("showVerificationInput", true);
        model.addAttribute("UserEmail", user);
        return "forgetpass";
    }

    @GetMapping("/resetpass")
    public String showResetPasswordForm(@RequestParam("user_id") Integer userId, Model model) {
        UserModel user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        UserModel resetPass = new UserModel();
        resetPass.setUser_id(userId);
        model.addAttribute("ResetPass", resetPass);
        return "resetpass";
    }

    @PostMapping("/resetpass")
    public String submitResetPassword(@ModelAttribute("ResetPass") UserModel resetPass, Model model) {
        UserModel existingUser = userRepo.findById(resetPass.getUser_id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        existingUser.setPassword(passwordEncoder.encode(resetPass.getPassword()));
        userRepo.save(existingUser);
        return "redirect:/login?success=Password reset successful";
    }

    @GetMapping("/profile")
    public String displayProfileForm(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; // Redirect to login if not authenticated
        }
        Integer userId = Integer.parseInt(authentication.getName()); // Get user_id from authentication
        UserModel user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
        model.addAttribute("ProfileUserData", user);
        model.addAttribute("editMode", false);
        String dynamicClass = "nav-active";
        model.addAttribute("profile", dynamicClass);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @ModelAttribute("ProfileUserData") UserModel updatedUser,
            @RequestParam(value = "action", required = false) String action,
            Authentication authentication,
            Model model) {
        Integer userId = Integer.parseInt(authentication.getName());
        UserModel existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        if (!existingUser.getUser_id().equals(updatedUser.getUser_id())) {
            model.addAttribute("errorMessage", "You can only update your own profile");
            model.addAttribute("RegUserData", existingUser);
            model.addAttribute("editMode", true);
            return "profile";
        }

        if ("update".equals(action)) {
            existingUser.setF_name(updatedUser.getF_name());
            existingUser.setL_name(updatedUser.getL_name());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhone_no(updatedUser.getPhone_no());
            userRepo.save(existingUser);
            model.addAttribute("successMessage", "Profile updated successfully");
            model.addAttribute("editMode", false);
        }

        model.addAttribute("RegUserData", existingUser);
        return "profile";
    }

    @GetMapping("/network")
    public String showNetwork(Model model) {
        // Display all users when landing on the page
        List<UserModel> allUsers = userRepo.findAll();
        model.addAttribute("users", allUsers);
        String dynamicClass = "nav-active";
        model.addAttribute("network", dynamicClass);
        return "network";
    }

    @GetMapping("/network/search")
    @ResponseBody
    public List<UserModel> searchNetwork(@RequestParam("query") String query) {
        List<UserModel> allUsers = userRepo.findAll();

        // If search bar is empty, return all users
        if (query == null || query.trim().isEmpty()) {
            return allUsers;
        }

        // Filter users based on email, first name, or last name
        String searchTerm = query.trim().toLowerCase();
        return allUsers.stream()
                .filter(user ->
                        user.getF_name().toLowerCase().contains(searchTerm) ||
                                user.getL_name().toLowerCase().contains(searchTerm) ||
                                user.getEmail().toLowerCase().contains(searchTerm))
                .toList();
    }

    @GetMapping("/networkprofile/{id}")
    public String showNetworkProfile(@PathVariable("id") Integer userId, Model model) {
        UserModel user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        model.addAttribute("user", user);
        String dynamicClass = "nav-active";
        model.addAttribute("network", dynamicClass);
        return "networkprofile";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUserManagement(Authentication authentication, Model model) {
        List<UserModel> allUsers = userRepo.findAll();
        model.addAttribute("users", allUsers);
        model.addAttribute("adminMode", true);
        model.addAttribute("AdminUserData", new UserModel()); // Fix applied here
        return "admin/dashboard";
    }

    @GetMapping("/admin/user-add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddUserForm(Model model) {
        model.addAttribute("AdminUserData", new UserModel());
        model.addAttribute("users", userRepo.findAll());
        return "admin/dashboard";
    }

    @PostMapping("/admin/user-add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addUser(@ModelAttribute("AdminUserData") UserModel user, Model model) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        model.addAttribute("successMessage", "User added successfully");
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("AdminUserData", new UserModel()); // Reset form
        return "admin/dashboard";
    }

    @GetMapping("/admin/user-update/{user_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showUpdateUserForm(@PathVariable("user_id") Integer userId, Model model) {
        UserModel user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        model.addAttribute("AdminUserData", user);
        model.addAttribute("users", userRepo.findAll());
        return "admin/dashboard";
    }

    @PostMapping("/admin/user-update/{user_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@PathVariable("user_id") Integer userId,
                             @ModelAttribute("AdminUserData") UserModel updatedUser,
                             Model model) {
        UserModel existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        existingUser.setF_name(updatedUser.getF_name());
        existingUser.setL_name(updatedUser.getL_name());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone_no(updatedUser.getPhone_no());
        userRepo.save(existingUser);
        model.addAttribute("successMessage", "User updated successfully");
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("AdminUserData", new UserModel()); // Reset form
        return "admin/dashboard";
    }

    @PostMapping("/admin/user/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@RequestParam("userId") Integer userId,
                             @RequestParam("action") String action,
                             Model model) {
        if ("delete".equals(action)) {
            UserModel user = userRepo.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            userRepo.delete(user);
            model.addAttribute("successMessage", "User deleted successfully");
        } else {
            model.addAttribute("errorMessage", "Invalid action");
        }
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("AdminUserData", new UserModel()); // Reset form
        return "admin/dashboard";
    }
}

