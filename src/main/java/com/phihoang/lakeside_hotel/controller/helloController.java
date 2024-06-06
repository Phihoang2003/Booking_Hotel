package com.phihoang.lakeside_hotel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class helloController {
    @GetMapping("/")
    public String hellWorld(){
        return "Hello to Hoang Phi developer";
    }

}
