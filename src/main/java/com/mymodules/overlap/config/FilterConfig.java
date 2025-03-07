package com.mymodules.overlap.config;

import com.mymodules.overlap.filter.TurnstileValidationFilter;
import com.mymodules.overlap.service.CaptchaService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private final CaptchaService captchaService;

    public FilterConfig(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @Bean
    public FilterRegistrationBean<TurnstileValidationFilter> turnstileFilter() {
        FilterRegistrationBean<TurnstileValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TurnstileValidationFilter(captchaService));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Integer.MIN_VALUE);  // 필터 우선순위 최상위
        return registrationBean;
    }
}