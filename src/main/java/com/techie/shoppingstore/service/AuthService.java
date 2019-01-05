package com.techie.shoppingstore.service;

import com.techie.shoppingstore.dto.LoginRequestDto;
import com.techie.shoppingstore.dto.RegisterRequestDto;
import com.techie.shoppingstore.model.User;
import com.techie.shoppingstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public boolean existsByUserName(RegisterRequestDto registerRequestDto) {
        return userRepository.existsByUsername(registerRequestDto.getUsername());
    }

    public void createUser(RegisterRequestDto registerRequestDto) {
        String encodedPassword = passwordEncoder.encode(registerRequestDto.getPassword());

        User user = new User(registerRequestDto.getEmail(),
                registerRequestDto.getUsername(),
                encodedPassword);
        userRepository.save(user);
    }

    public void authenticate(LoginRequestDto loginRequestDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword())
                );

        SecurityContextHolder.getContext().setAuthentication(authenticate);
    }
}
