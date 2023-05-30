package com.project.capstone.repository;

import com.project.capstone.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository(value = "com.project.capstone.repository.MemberRepository")
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Member findByEmail(String email);


    Optional<Member> findByEmail_(String email);
    Member findByName(String name);
}
