package org.example.springdemo.controller;

import org.example.springdemo.model.UserModel;
import org.example.springdemo.repository.AppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FeatureController {

    @Autowired
    private AppRepository userRepo;

    private static final String UPLOAD_DIR = "src/main/resources/static/images/profile/";

    @PostMapping("/upload/profileimage")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @RequestParam("profileImage") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            // Get the authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                response.put("error", "User not authenticated");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            // authentication.getName() now returns the user_id as a String
            Integer userId = Integer.parseInt(auth.getName());
            UserModel user = userRepo.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found with ID: " + userId));

            // Create upload directory if it doesn't exist
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Read the uploaded image
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));

            // Resize the image to 150x150 (as specified in your HTML display)
            BufferedImage resizedImage = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, 150, 150);

            // Determine the file extension
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFileName = userId + "_profile" + extension;

            // Save the resized image
            Path filePath = Paths.get(UPLOAD_DIR + newFileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, extension.substring(1), baos);
            Files.write(filePath, baos.toByteArray());

            // Update the user's imageUrl
            String imageUrl = "/images/profile/" + newFileName;
            user.setImageUrl(imageUrl);
            userRepo.save(user);

            // Return the new image URL
            response.put("imageUrl", imageUrl);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            response.put("error", "Failed to upload image: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}