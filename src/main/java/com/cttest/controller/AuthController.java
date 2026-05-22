package com.cttest.controller;

import com.cttest.model.dto.LoginRequest;
import com.cttest.model.dto.LoginResponse;
import com.cttest.model.dto.RefreshTokenRequest;
import com.cttest.security.CustomUserDetails;
import com.cttest.security.JwtTokenProvider;
import com.cttest.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(
                userDetails.getId(), userDetails.getUsername(), userDetails.isAdmin());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new LoginResponse(
                token, "Bearer", userDetails.getId(),
                userDetails.getUsername(), userDetails.isAdmin(), refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken()) || !jwtTokenProvider.isRefreshToken(request.refreshToken())) {
            return ResponseEntity.badRequest().build();
        }

        Integer userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken());
        var user = userService.findById(userId);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.isAdmin());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return ResponseEntity.ok(new LoginResponse(
                token, "Bearer", user.getId(),
                user.getUsername(), user.isAdmin(), refreshToken));
    }

}
