package com.example.storesports.infrastructure.configuration.auditor;

import com.example.storesports.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@RequiredArgsConstructor
public class AuditConfiguration {

    private final UserRepository userRepository;

//    @Bean
//    public AuditorAware<Integer> auditorProvider() {
//        return new AuditorAwareImpl(userRepository);
//    }

    @Bean
    public AuditorAware<Integer> auditorProvider() {
        return new AuditorAwareImpl(userRepository);
    }

}