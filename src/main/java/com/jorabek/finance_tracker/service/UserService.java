package com.jorabek.finance_tracker.service;

import com.jorabek.finance_tracker.entity.User;
import com.jorabek.finance_tracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) {
        System.out.println("Processing registration for user: " + user.getUsername());

        // NoOpPasswordEncoder is used in SecurityConfig, so encode() returns the plain
        // password.
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        System.out.println("Raw password: " + rawPassword);
        System.out.println("Encoded password (should be same if NoOp): " + encodedPassword);

        user.setPassword(encodedPassword);
        user.setRole("ROLE_USER");
        userRepository.save(user);

        System.out.println("User successfully saved: " + user.getUsername());
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
