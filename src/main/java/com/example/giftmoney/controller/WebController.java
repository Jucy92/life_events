package com.example.giftmoney.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/statistics")
    public String statistics() {
        return "statistics";
    }

    @GetMapping("/find-username")
    public String findUsername() {
        return "find-username";
    }

    @GetMapping("/find-password")
    public String findPassword() {
        return "find-password";
    }

}
