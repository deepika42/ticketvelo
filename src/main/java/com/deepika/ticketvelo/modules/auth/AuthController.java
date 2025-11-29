package com.deepika.ticketvelo.modules.auth;

import com.deepika.ticketvelo.config.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login-as-guest")
    public Map<String, String> loginAsGuest() {
        // Simulate a Guest Login (Create a random User ID)
        Long randomUserId = (long) (Math.random() * 10000);
        String token = jwtUtil.generateToken(randomUserId);

        return Map.of(
                "token", token,
                "userId", String.valueOf(randomUserId)
        );
    }
}