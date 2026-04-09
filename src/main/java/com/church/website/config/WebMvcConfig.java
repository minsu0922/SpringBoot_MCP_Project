package com.church.website.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 정적 리소스 핸들러 설정
 * 업로드된 파일을 /uploads/** URL로 서빙
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir}")
    private String ministryUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/ministry/** → 실제 업로드 디렉토리
        registry.addResourceHandler("/uploads/ministry/**")
                .addResourceLocations("file:" + ministryUploadDir + "/");
    }
}
