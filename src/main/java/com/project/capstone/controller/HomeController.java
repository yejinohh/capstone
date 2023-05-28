package com.project.capstone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;

@Controller
public class HomeController {
    // 기본페이지 요청 메서드
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
