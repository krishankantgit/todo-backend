package com.todoapp.controller;

import java.util.Optional;

import com.todoapp.dto.AuthResponse;
import com.todoapp.dto.LoginRequest;
import com.todoapp.dto.RegisterRequest;
import com.todoapp.model.User;
import com.todoapp.repository.UserRepository;
import com.todoapp.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already registered.");
        }

        User user = new User(
                req.email(),
                passwordEncoder.encode(req.password()),
                req.name()
        );
        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, savedUser.getName(), savedUser.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.email());

        if (userOpt.isPresent() && passwordEncoder.matches(req.password(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token, user.getName(), user.getEmail()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
    }
}