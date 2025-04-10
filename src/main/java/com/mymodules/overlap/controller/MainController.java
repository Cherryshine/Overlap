package com.mymodules.overlap.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class MainController {

    @GetMapping("/")
    public ModelAndView landingPage()
    {
        return new ModelAndView("landing" );
    }
    @GetMapping("/captcha")
    public ModelAndView captchaPage()
    {
        return new ModelAndView("captcha" );
    }
    @GetMapping("/create-schedule")
    public ModelAndView scheduleCreatePage()
    {
        return new ModelAndView("create-schedule");
    }
    @GetMapping("/select-time")
    public ModelAndView selectTime()
    {
        return new ModelAndView("select_constructor_time" );
    }
    @GetMapping("/test")
    public ModelAndView test(){
        return new ModelAndView("show-schedule");
    }
    @GetMapping("/main")
    public ModelAndView main(){
        return new ModelAndView("temp");
    }
    @GetMapping("/nav")
    public ModelAndView nav(){
        return new ModelAndView("navigation-bar");
    }

    @GetMapping("/mypage")
    public ModelAndView mypage(){
        return new ModelAndView("mypage");
    }

    @GetMapping("/report-bug")
    public ModelAndView reportBug(){
        return new ModelAndView("report-bug");
    }
}
