package com.project.capstone.repository;

import com.project.capstone.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "com.project.capstone.repository.MemberRepository")
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Member findByEmail(String email);
}
