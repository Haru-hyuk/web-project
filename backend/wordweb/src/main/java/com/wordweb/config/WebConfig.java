package com.wordweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // React Router를 위한 설정: 모든 경로를 index.html로 리다이렉트
        registry.addViewController("/")
                .setViewName("forward:/index.html");
        
        // API 경로가 아닌 모든 경로를 index.html로 리다이렉트
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
    }
}

