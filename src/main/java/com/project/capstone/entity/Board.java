package com.project.capstone.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter

public class Board {

    @Id
    @Column(name = "board_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer boardId;

    private String title;

    private String content;

    private String filename;

    private String filepath;

    @JoinColumn(name = "upload_date")
    private Date uploadDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private Member member;

}
