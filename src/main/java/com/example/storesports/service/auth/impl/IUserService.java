package com.example.storesports.service.auth.impl;

import com.example.storesports.repositories.UserRepository;
import com.example.storesports.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.storesports.entity.User;

@Service
@RequiredArgsConstructor
public class IUserService implements UserService {

    private final ModelMapper modelMapper;

    private final UserRepository userRepository;

    @Override
    public User getAccountByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("người dùng không tồn tại" + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Người dùng không tồn tại" + username));


        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                AuthorityUtils.createAuthorityList(user.getRole().toString())
        );

    }


}