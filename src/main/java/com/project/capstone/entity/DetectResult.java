package com.project.capstone.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.time.LocalDate;

@Getter
@Setter
public class DetectResult {

    String name;
    double similarity;
    String emotion;
    LocalDate currentTime;

}
