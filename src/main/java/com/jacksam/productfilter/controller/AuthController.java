package com.jacksam.productfilter.controller;

import com.jacksam.productfilter.dto.LoginRequest;
import com.jacksam.productfilter.dto.LoginResponse;
import com.jacksam.productfilter.dto.UserDTO;
import com.jacksam.productfilter.entity.User;
import com.jacksam.productfilter.repository.UserRepository;
import com.jacksam.productfilter.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Account disabled");
        }

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName()).toList();

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);

        return ResponseEntity.ok(new LoginResponse(
                token, "Bearer", 86400000, UserDTO.from(user)));
    }
}
