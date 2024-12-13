package com.example.storesports.Infrastructure.configuration.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configution {
        @Bean
        public ModelMapper modelMapper(){
            return new ModelMapper();
        }


}
