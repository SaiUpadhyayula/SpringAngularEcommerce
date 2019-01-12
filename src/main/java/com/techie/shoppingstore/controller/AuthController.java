package com.techie.shoppingstore.controller;

import com.techie.shoppingstore.dto.AuthenticationResponse;
import com.techie.shoppingstore.dto.LoginRequestDto;
import com.techie.shoppingstore.dto.RegisterRequestDto;
import com.techie.shoppingstore.exceptions.ApiResponse;
import com.techie.shoppingstore.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth/")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return new ResponseEntity<>(authService.authenticate(loginRequestDto), HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        if (authService.existsByUserName(registerRequestDto)) {
            return new ResponseEntity<>(new ApiResponse(400, "Username already exists"), HttpStatus.BAD_REQUEST);
        }

        authService.createUser(registerRequestDto);
        return new ResponseEntity<>(new ApiResponse(200, "User Registration Completed Successfully!!"), HttpStatus.OK);
    }
}
