package com.videopostingsystem.videopostingsystem.users;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(HttpSession session){
        return userService.deleteAccount(session);
    }

    @DeleteMapping("/delete-account/{user}")
    public ResponseEntity<?> deleteAccountAdmin(@PathVariable("user") String user, HttpSession session){
        return userService.deleteAccountAdmin(user, session);
    }

    @GetMapping("/getUsername")
    public ResponseEntity<?> myUsername(HttpSession session){
        return userService.getUsername(session);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session){
        return userService.getProfile(session);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileModel updateProfileModel, HttpSession session){
        return userService.updateProfile(updateProfileModel, session);
    }

    @GetMapping("/{user}")
    public ResponseEntity<?> getUserProfile(@PathVariable("user") String user, HttpSession session){
        return userService.getUserProfile(user, session);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(HttpSession session){
        return userService.getUsers(session);
    }
}
