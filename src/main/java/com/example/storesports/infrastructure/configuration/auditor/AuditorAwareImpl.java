package com.example.storesports.infrastructure.configuration.auditor;

import com.example.storesports.entity.User;
import com.example.storesports.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-audit
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 28/07/18
 * Time: 04.17
 * To change this template use File | Settings | File Templates.
 */
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<Integer> {
    private final UserRepository userRepository;
//    @Override
//    public Optional<Integer> getCurrentAuditor() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return Optional.empty();
//        }
//
//        if (!authentication.getAuthorities().stream()
//                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"))) {
//            return Optional.empty();
//        }
//
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        String username = userDetails.getUsername();
//        User currentUser = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại: " + username));
//        return Optional.of(Math.toIntExact(currentUser.getId()));
//    }

    @Override
    public Optional<Integer> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (!authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"))) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof UserDetails && authentication.getDetails() instanceof Claims) {
            Claims claims = (Claims) authentication.getDetails();
            Integer userId = claims.get("userId", Integer.class);
            if (userId == null) {
                return Optional.empty();
            }
            return Optional.of(userId);
        }
        return Optional.empty();
    }


//    public Optional<Integer> getCurrentAuditor() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return Optional.empty();
//        }
//
//        if (authentication.getPrincipal() instanceof UserDetails && authentication.getDetails() instanceof Claims) {
//            Claims claims = (Claims) authentication.getDetails();
//            Integer userId = claims.get("userId", Integer.class);
//            if (userId == null) {
//                return Optional.empty();
//            }
//            return Optional.of(userId);
//        }
//
//        return Optional.empty();
//    }  // cái này dùng cho cả 2 phía clienr và admin

}