package com.elibrary.elibrary.controller;

import com.elibrary.elibrary.dto.LoginRequest;
import com.elibrary.elibrary.dto.RegisterRequest;
import com.elibrary.elibrary.dto.AuthResponse;
import com.elibrary.elibrary.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}