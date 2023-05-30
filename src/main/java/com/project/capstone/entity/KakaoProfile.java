package com.project.capstone.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class KakaoProfile {

    public Long id;
    public KakaoAccount kakao_account;

    @Data
    @JsonIgnoreProperties(ignoreUnknown=true)
    public class KakaoAccount{
        public String email;
    }
}
