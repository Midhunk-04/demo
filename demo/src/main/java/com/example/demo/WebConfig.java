package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Adjust the path to your uploads directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/C:/Users/Midhun/Desktop/LastPass/appymr/demo/uploads/");
    }
}