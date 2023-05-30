package com.project.capstone.service;


import com.project.capstone.entity.Member;
import com.project.capstone.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public void save(Member member) {
        //1. dto -> entity로 변환
        //2. repository의 save 메서드 호출
        //repository의 save메서드 호출(조건: entity객체를 넘겨줘야함)
        validateDuplicateMember(member);
        memberRepository.save(member);
    }

    public void updateMemberToken(String email, String accessToken) {
        Optional<Member> optionalMember = memberRepository.findByEmail_(email);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setToken(accessToken);
            memberRepository.save(member); // token 필드만 업데이트하여 저장
        }
    }

    //중복된 email
    private void validateDuplicateMember(Member member) {

        Member existingMember = memberRepository.findByEmail(member.getEmail());
        if (existingMember != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(username);

        if(member == null){
            throw new UsernameNotFoundException(username);
        }

        User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles()
                .build();

        return member;

    }

    @Transactional(readOnly = true)
    public Member searchMember(String memberName){;
        Member member = memberRepository.findByName(memberName);
        if(member==null){
            member = new Member();
        }
        return member;
    }

}
