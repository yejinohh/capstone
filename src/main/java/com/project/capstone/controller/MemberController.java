package com.project.capstone.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.capstone.dto.MemberDto;
import com.project.capstone.entity.KakaoProfile;
import com.project.capstone.entity.Member;
import com.project.capstone.entity.OAuthToken;
import com.project.capstone.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import javax.validation.Valid;

@Controller

@RequiredArgsConstructor
public class MemberController {

    //생성자 주입
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private String cosKey = "cos1234";

    //회원가입 페이지 출력 요청
    @GetMapping("/members/new")
    public String saveFrom(Model model){
        model.addAttribute("memberDto", new MemberDto());
        return "member/memberForm";
    }

    @PostMapping("/members/new")
    public String saveForm(@Valid MemberDto memberDto , BindingResult bindingResult, Model model){

        if(bindingResult.hasErrors()){
            return "member/memberForm";
        }

        try{
            System.out.println(memberDto);
            Member member = Member.toMemberEntity(memberDto, passwordEncoder);
            memberService.save(member);
        }catch (IllegalStateException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/";
    }

    //로그인 페이지
    @GetMapping("/members/login")
    public String loginMember(@ModelAttribute MemberDto memberDto){
        return "/member/memberLoginForm";
    }

    @GetMapping("/members/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "member/memberLoginForm";
    }

    @GetMapping("/auth/kakao/callback")
    public String kakaoCallback(String code) { //데이터를 리턴해주는 컨트롤러 함수

        //POST방식으로 key=value 데이터를 요청 (카카오 쪽으로)
        RestTemplate rt = new RestTemplate();

        //HttpHeader오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //HttpBody오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "1cbdf2315d3162f5f206c7f6aebfd43d");
        params.add("redirect_uri", "http://localhost:8080/auth/kakao/callback");
        params.add("code", code);

        //HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        //exchange는 HttpEntity라는 오브젝트를 받으므로 HttpEntity사용
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        //Post 방식으로 Http 요청하기 -> response 변수의 응답 받음.
        ResponseEntity<String> kakaoTokenResponse = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        OAuthToken oauthToken = null;

        try{
            oauthToken = objectMapper.readValue(kakaoTokenResponse.getBody(), OAuthToken.class);
        }catch (JsonMappingException e){
            e.printStackTrace();
        }catch (JsonProcessingException e){
            e.printStackTrace();;
        }

        //System.out.println(oauthToken.getAccess_token());

        RestTemplate rt2 = new RestTemplate();

        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Authorization","Bearer " + oauthToken.getAccess_token());
        headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers2);

        ResponseEntity<String> kakaoProfileResponse = rt2.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        ObjectMapper objectMapper2  = new ObjectMapper();
        KakaoProfile kakaoProfile = null;

        try{
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        }catch (JsonMappingException e){
            e.printStackTrace();
        }catch (JsonProcessingException e){
            e.printStackTrace();;
        }

        //System.out.println("카카오 아이디: " + kakaoProfile.getId());
        //System.out.println("카카오 이메일: " + kakaoProfile.getKakao_account().getEmail());
        //Member 오브젝터 : username, password, email
        System.out.println("우박이오 유저네임: " + kakaoProfile.getKakao_account().getEmail()+"_"+kakaoProfile.getId());
        System.out.println("우박이오 이메일: " + kakaoProfile.getKakao_account().getEmail());
        System.out.println("우박이오 패스워드: " + cosKey);


        //맴버 객체 생성
        MemberDto memberDto = new MemberDto();
        memberDto.setEmail(kakaoProfile.getKakao_account().getEmail());
        memberDto.setPassword(cosKey);
        memberDto.setName(kakaoProfile.getKakao_account().getEmail()+"_"+kakaoProfile.getId());

        Member kakaoMember = Member.toMemberEntity(memberDto, passwordEncoder);

        //가입자 혹은 비가입자 체크해서 처리
        Member originMember = memberService.searchMember(kakaoMember.getName());

        if(originMember.getName() == null){
            //가입된 회원이 아니면 회원 가입
            System.out.print("기존 회원이 아니므로 회원 가입 후 ");
            memberService.save(kakaoMember);
        }
        //로그인 처리
        System.out.println("로그인 되었습니다.");
        String status;

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(kakaoMember.getEmail(), cosKey);
        System.out.println(kakaoMember.getName());
        System.out.println(kakaoMember.getPassword());


        try {
            // AuthenticationManager 에 token 을 넘기면 UserDetailsService 가 받아 처리하도록 한다.
            Authentication authentication = authenticationManager.authenticate(token);
            // 실제 SecurityContext 에 authentication 정보를 등록한다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (DisabledException | LockedException | BadCredentialsException e) {
            if (e.getClass().equals(BadCredentialsException.class)) {
                status = "invalid-password";
            } else if (e.getClass().equals(DisabledException.class)) {
                status = "locked";
            } else if (e.getClass().equals(LockedException.class)) {
                status = "disable";
            } else {
                status = "unknown";
            }
            System.out.println(status);
        }

        return "redirect:/";
    }

}
