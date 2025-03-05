package com.mymodules.overlap.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventGroupController {

    @PostMapping("eventGroup/create")
    public String createEventGroup(){
        return "redirect/test";
    }

}
