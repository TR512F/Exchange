package com.bubnov.v5.service;

import com.bubnov.v5.model.Role;
import com.bubnov.v5.model.User;
import com.bubnov.v5.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService{

    private final UserRepository repository;

    public UserDetailsService userDetailsService() {
        return this;
    }

    public User save(User user) {
        return repository.save(user);
    }

    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("User with this Username already exists");
        }
        if (repository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with this Email already exists");
        }
        return save(user);
    }

    public void setRole(String username, Role role) {
        User user = (User) loadUserByUsername(username);
        user.setRole(role);
        repository.save(user);
    }

    public void blockUser(String username, Boolean isLocked) {
        User user = (User) loadUserByUsername(username);
        user.setLocked(isLocked);
        repository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}