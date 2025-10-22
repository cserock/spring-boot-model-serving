package com.example.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/posts";
    }

    @GetMapping("/hello") 
    @ResponseBody 
    public String hello() { 
        return "Hello World!!"; 
    } 
}
