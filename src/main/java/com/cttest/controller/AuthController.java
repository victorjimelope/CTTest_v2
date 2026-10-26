package com.cttest.controller;

import com.cttest.model.dto.LoginRequest;
import com.cttest.model.dto.LoginResponse;
import com.cttest.model.dto.RefreshTokenRequest;
import com.cttest.security.CustomUserDetails;
import com.cttest.security.JwtTokenProvider;
import com.cttest.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(
                userDetails.getId(), userDetails.getUsername(), userDetails.isAdmin());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getId());

        jakarta.servlet.http.Cookie jwtCookie = new jakarta.servlet.http.Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(new LoginResponse(
                token, "Bearer", userDetails.getId(),
                userDetails.getUsername(), userDetails.isAdmin(),
                userDetails.getCenterId(), refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletResponse servletResponse) {
        if (!jwtTokenProvider.validateToken(request.refreshToken()) || !jwtTokenProvider.isRefreshToken(request.refreshToken())) {
            return ResponseEntity.badRequest().build();
        }

        Integer userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken());
        var user = userService.findById(userId);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.isAdmin());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        jakarta.servlet.http.Cookie jwtCookie = new jakarta.servlet.http.Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60);
        servletResponse.addCookie(jwtCookie);

        return ResponseEntity.ok(new LoginResponse(
                token, "Bearer", user.getId(),
                user.getUsername(), user.isAdmin(),
                user.getCenter() != null ? user.getCenter().getId() : null,
                refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        jakarta.servlet.http.Cookie jwtCookie = new jakarta.servlet.http.Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("userId", userDetails.getId());
        response.put("username", userDetails.getUsername());
        response.put("admin", userDetails.isAdmin());
        response.put("centerId", userDetails.getCenterId());

        return ResponseEntity.ok(response);
    }

}
