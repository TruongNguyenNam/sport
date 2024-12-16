package com.example.storesports.infrastructure.configuration.auditor;

import org.springframework.data.domain.AuditorAware;

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
public class AuditorAwareImpl implements AuditorAware<Integer> {

    @Override
    public Optional<Integer> getCurrentAuditor() {
        return Optional.of(1);
    }

}