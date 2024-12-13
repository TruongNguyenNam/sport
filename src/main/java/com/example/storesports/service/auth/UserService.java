package com.example.storesports.service.auth;



import com.example.storesports.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
//    List<UserResponse> getAllUsers();
//
//    UserResponse getUserById(Integer userId);
//
//    UserResponse createUser(UserRequest userRequest);
//
//    UserResponse updateUser(Integer userId, UserRequest userRequestDTO);
//
//    void softDeleteUser(Integer userId);

    User getAccountByUsername(String username);
}
