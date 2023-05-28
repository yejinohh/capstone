package com.project.capstone.entity;

import com.project.capstone.constant.Role;
import com.project.capstone.dto.MemberDto;
import jdk.jfr.Enabled;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Setter
@Getter
public class Member implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String password;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member toMemberEntity(MemberDto memberDto, PasswordEncoder passwordEncoder){
        Member memberEntity = new Member();
        memberEntity.setEmail(memberDto.getEmail());
        String password = passwordEncoder.encode(memberDto.getPassword());
        memberEntity.setPassword(password);
        memberEntity.setName(memberDto.getName());
        memberEntity.setRole(Role.USER);
        return memberEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
