package com.project.capstone.repository;

import com.project.capstone.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository(value = "com.project.capstone.repository.BoardRepository")
public interface BoardRepository extends JpaRepository<Board, Integer> {

    Board findByBoardId(Integer boardId);
    Page<Board> findByMemberId(Pageable pageable, Integer memberId);
}
