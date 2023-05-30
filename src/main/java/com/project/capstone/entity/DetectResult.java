package com.project.capstone.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class DetectResult {

    String name;
    int similarity;
    String emotion;
    String currentTime;

}
