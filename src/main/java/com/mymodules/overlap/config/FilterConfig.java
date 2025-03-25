package com.mymodules.overlap.config;

import com.mymodules.overlap.filter.TurnstileValidationFilter;
import com.mymodules.overlap.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;

    @Bean
    public FilterRegistrationBean<TurnstileValidationFilter> turnstileValidationFilter() {
        FilterRegistrationBean<TurnstileValidationFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new TurnstileValidationFilter(captchaService, jwtUtil));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Integer.MIN_VALUE);

        return registrationBean;
    }
}