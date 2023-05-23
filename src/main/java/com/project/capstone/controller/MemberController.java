package com.project.capstone.controller;

import com.project.capstone.dto.MemberDto;
import com.project.capstone.entity.Member;
import com.project.capstone.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.validation.Valid;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {
    //생성자 주입
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    //회원가입 페이지 출력 요청
    @GetMapping("/new")
    public String saveFrom(Model model){
        model.addAttribute("memberDto", new MemberDto());
        return "member/memberForm";
    }

    @PostMapping("/new")
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
    @GetMapping("/login")
    public String loginMember(@ModelAttribute MemberDto memberDto){
        return "/member/memberLoginForm";
    }

    @GetMapping("/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "/member/memberLoginForm";
    }
}
