package com.bubnov.v5.controller;

import com.bubnov.v5.model.Role;
import com.bubnov.v5.model.User;
import com.bubnov.v5.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private UserRepository userRepository;

    @PutMapping("/block/{username}")
    public ResponseEntity<?> blockUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setLocked(true);
        userRepository.save(user);
        return ResponseEntity.ok("User blocked successfully");
    }

    @PutMapping("/unblock/{username}")
    public ResponseEntity<String> unblockUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setLocked(false);
        userRepository.save(user);
        return ResponseEntity.ok("User unblocked successfully");
    }

    @PutMapping("/set-admin/{username}")
    public ResponseEntity<?> setAdmin(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
        return ResponseEntity.ok("Role Admin set successfully successfully");
    }

    @PutMapping("/set-user/{username}")
    public ResponseEntity<String> setUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
        return ResponseEntity.ok("Role User set successfully successfully");
    }
}
