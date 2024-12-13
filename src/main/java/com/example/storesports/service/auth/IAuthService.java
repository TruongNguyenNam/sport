package com.example.storesports.service.auth;


import com.example.storesports.infrastructure.constant.Role;
import com.example.storesports.core.auth.payload.LoginInfoDto;
import com.example.storesports.core.auth.payload.RegisterForm;
import com.example.storesports.core.auth.payload.UserResponse;
import com.example.storesports.entity.Token;
import com.example.storesports.entity.User;
import com.example.storesports.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class IAuthService implements AuthService {

    @Autowired
    private UserService service;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IJWTTokenService ijwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public LoginInfoDto login(String username) {
        User entity = service.getAccountByUsername(username);

        LoginInfoDto dto = modelMapper.map(entity, LoginInfoDto.class);

        dto.setToken(ijwtTokenService.generateJWT(entity.getUsername()));

        Token token = ijwtTokenService.generateRefreshToken(entity);
        dto.setRefreshToken(token.getKey());

        return dto;
    }

    @Override
    public UserResponse register(RegisterForm registerForm) {
        if (userRepository.existsByUsername(registerForm.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại.");
        }

        if (userRepository.existsByEmail(registerForm.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }

        User user = modelMapper.map(registerForm, User.class);

        user.setPassword(passwordEncoder.encode(registerForm.getPassword()));

        user.setRole(Role.ADMIN);

        User savedUser = userRepository.save(user);

        UserResponse userResponse = modelMapper.map(savedUser, UserResponse.class);
        userResponse.setMessage("Đăng ký thành công.");
        userResponse.setRole("USER");
        return userResponse;

    }


}
