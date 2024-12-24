package com.bubnov.v5.controller;

import com.bubnov.v5.model.Role;
import com.bubnov.v5.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {
    private final UserService userService;
    private final Map<String, String> response = new HashMap<>();

    @PutMapping("/block")
    public ResponseEntity<?> blockUser(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()){
            response.put("message", "Username cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }
        userService.blockUser(username, true);
        response.put("message", "User blocked successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/unblock")
    public ResponseEntity<?> unblockUser(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()){
            response.put("message", "Username cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }
        userService.blockUser(username, false);
        response.put("message", "User unblocked successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/set-admin")
    public ResponseEntity<?> setAdmin(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()){
            response.put("message", "Username cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }
        userService.setRole(username, Role.ROLE_ADMIN);
        response.put("message", "Role Admin set successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/set-user")
    public ResponseEntity<?> setUser(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()){
            response.put("message", "Username cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }
        userService.setRole(username, Role.ROLE_USER);
        response.put("message", "Role User set successfully");
        return ResponseEntity.ok(response);
    }
}
