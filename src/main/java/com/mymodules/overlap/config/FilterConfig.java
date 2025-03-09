package com.mymodules.overlap.config;

import com.mymodules.overlap.filter.TurnstileValidationFilter;
import com.mymodules.overlap.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FilterConfig {

    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;

    public FilterConfig(CaptchaService captchaService) {
        this.captchaService = captchaService;
        this.jwtUtil = new JwtUtil();
    }

    @Bean
    public FilterRegistrationBean<TurnstileValidationFilter> turnstileValidationFilter(CaptchaService captchaService) {
        FilterRegistrationBean<TurnstileValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TurnstileValidationFilter(captchaService,jwtUtil));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);  // ✅ 가장 먼저 실행
        return registrationBean;
    }


}