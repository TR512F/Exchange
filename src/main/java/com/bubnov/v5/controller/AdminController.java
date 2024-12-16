package com.bubnov.v5.controller;

import com.bubnov.v5.model.Role;
import com.bubnov.v5.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private UserService userService;

    @PutMapping("/block/{username}")
    public ResponseEntity<?> blockUser(@PathVariable String username) {
        userService.blockUser(username, true);
        return ResponseEntity.ok("User blocked successfully");
    }

    @PutMapping("/unblock/{username}")
    public ResponseEntity<String> unblockUser(@PathVariable String username) {
        userService.blockUser(username, false);
        return ResponseEntity.ok("User unblocked successfully");
    }

    @PutMapping("/set-admin/{username}")
    public ResponseEntity<?> setAdmin(@PathVariable String username) {
        userService.setRole(username, Role.ROLE_ADMIN);
        return ResponseEntity.ok("Role Admin set successfully successfully");
    }

    @PutMapping("/set-user/{username}")
    public ResponseEntity<String> setUser(@PathVariable String username) {
        userService.setRole(username, Role.ROLE_USER);
        return ResponseEntity.ok("Role User set successfully successfully");
    }
}
