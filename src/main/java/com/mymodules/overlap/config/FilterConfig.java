package com.mymodules.overlap.config;

import com.mymodules.overlap.filter.TurnstileValidationFilter;
import com.mymodules.overlap.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor  // ✅ 생성자 주입 자동으로 처리
public class FilterConfig {

    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil; // ✅ 주입받기만 하면 됨!

    @Bean
    public FilterRegistrationBean<TurnstileValidationFilter> turnstileValidationFilter() {
        FilterRegistrationBean<TurnstileValidationFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new TurnstileValidationFilter(captchaService, jwtUtil)); // ✅ 주입받은 객체 넘김
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Integer.MIN_VALUE);

        return registrationBean;
    }
}