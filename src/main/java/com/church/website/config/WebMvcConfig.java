package com.church.website.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir}")
    private String ministryUploadDir;

    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/ministry/**")
                .addResourceLocations("file:" + ministryUploadDir + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/uploads/**");
    }
}
