package com.example.storesports;

import com.example.storesports.infrastructure.configuration.auditor.AuditorAwareImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableScheduling
public class StoreSportsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreSportsApplication.class, args);
        System.out.println("Đang chạy...");
    }


}
