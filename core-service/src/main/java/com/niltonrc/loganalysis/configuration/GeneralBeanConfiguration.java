package com.niltonrc.loganalysis.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneralBeanConfiguration
{
    @Bean
    ObjectMapper getObjectMapper()
    {
        return new ObjectMapper();
    }
}
