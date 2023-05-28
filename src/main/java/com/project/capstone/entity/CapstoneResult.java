package com.project.capstone.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CapstoneResult {
    boolean status;
    String message;
    int count = 0;
    List<DetectResult> data;
}
