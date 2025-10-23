package com.dms.auth_service.controller;

import com.dms.auth_service.model.User;
import com.dms.auth_service.repository.UserRepository;
import com.dms.auth_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody User user) {
    Optional<User> dbUser = userRepository.findByUsername(user.getUsername());

    if (dbUser.isPresent() && encoder.matches(user.getPassword(), dbUser.get().getPassword())) {
        String token = jwtUtil.generateToken(dbUser.get().getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    } else {
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }
}

}