package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:3002")
public class UserController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestParam("user") String userJson,
                                         @RequestParam("profilePicture") MultipartFile file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(userJson, User.class);
            logger.info("Parsed User: " + user);

            // Generate a unique username
            String uniqueUsername = generateUniqueUsername(user.getName());
            user.setName(uniqueUsername);
            logger.info("Unique Username: " + uniqueUsername);

            // Save the file to a directory
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs(); // Create the directory if it does not exist
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + fileName;
            file.transferTo(new File(filePath));
            logger.info("File saved at: " + filePath);

            // Set the profile picture path
            user.setProfilePicture(fileName);

            // Save the user to MongoDB
            User savedUser = userService.saveUser(user);
            logger.info("User saved to MongoDB: " + savedUser);

            return ResponseEntity.ok("Your Unique Identity: " + savedUser.getName());
        } catch (IOException e) {
            logger.error("Error processing signup request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing signup request");
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error");
        }
    }

    private String generateUniqueUsername(String baseName) {
        String sanitizedBaseName = baseName.replaceAll("\\s+", "").toLowerCase();
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);
        return sanitizedBaseName + uniqueSuffix;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }

        User user = userService.findByUsername(username);
        if (user != null) {
            // Return user details including profile picture
            return ResponseEntity.ok(user);
        } else {
            logger.warn("Failed login attempt for username: " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ACCESS DENIED");
        }
    }

    @GetMapping("/userExists")
    public ResponseEntity<Boolean> userExists(@RequestParam("username") String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/getUser")
    public ResponseEntity<User> getUser(@RequestParam("username") String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Path filePath = Paths.get("C:/Users/Midhun/Desktop/LastPass/appymr/demo/uploads").resolve(filename);
        FileSystemResource resource = new FileSystemResource(filePath.toFile());

        if (resource.exists()) {
            String fileExtension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
            MediaType mediaType;

            switch (fileExtension) {
                case ".jpg":
                case ".jpeg":
                    mediaType = MediaType.IMAGE_JPEG;
                    break;
                case ".png":
                    mediaType = MediaType.IMAGE_PNG;
                    break;
                default:
                    mediaType = MediaType.APPLICATION_OCTET_STREAM; // Fallback
                    break;
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
