package com.example.storesports.core.auth.controller;

import com.example.storesports.entity.User;
import com.example.storesports.infrastructure.constant.Role;
import com.example.storesports.infrastructure.validation.RefreshTokenValid;
import com.example.storesports.core.auth.payload.*;
import com.example.storesports.repositories.UserRepository;
import com.example.storesports.service.auth.IAuthService;
import com.example.storesports.service.auth.IJWTTokenService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/auth")
@Validated
public class AuthController {

    @Autowired
    private IAuthService authService;

    @Autowired
    private IJWTTokenService ijwtTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public LoginInfoDto login(@RequestBody @Valid loginForm loginForm){
                    Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginForm.getUsername(),
                            loginForm.getPassword())
            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    return authService.login(loginForm.getUsername());
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<TokenDTO> refreshToken(@RefreshTokenValid String refreshToken) {
        try {
            TokenDTO newToken = ijwtTokenService.getNewToken(refreshToken);
            return ResponseEntity.ok(newToken);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterForm registerForm) {
        UserResponse userResponse = authService.register(registerForm);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }



//    @Autowired
//    private ModelMapper modelMapper;
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder encoder;
//    @PostMapping("/addUser")
//    public UserResponse addUser(@RequestBody UserRequest userRequest) {
//        String encodedPassword = encoder.encode(userRequest.getPassword());
//        User newUser = new User();
//        newUser.setUsername(userRequest.getUsername());
//        newUser.setPassword(encodedPassword);
//        newUser.setEmail(userRequest.getEmail());
//        newUser.setRole(Role.ADMIN);
////        newUser.setDeleted(userRequest.isDeleted());
//
//        User savedUser = userRepository.save(newUser);
//        return modelMapper.map(savedUser, UserResponse.class);
//    }





}
