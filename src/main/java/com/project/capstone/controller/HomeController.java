package com.project.capstone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/thymeleaf")
public class HomeController {
    //기본페이지 요청 메서드
    @GetMapping("/")
    public String index(){
        return "index";
    }
}
