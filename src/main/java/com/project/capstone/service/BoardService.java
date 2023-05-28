package com.project.capstone.service;

import com.project.capstone.dto.BoardDto;
import com.project.capstone.dto.MemberDto;
import com.project.capstone.entity.Board;
import com.project.capstone.entity.Member;
import com.project.capstone.repository.BoardRepository;
import lombok.RequiredArgsConstructor;


import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.util.List;
import java.util.UUID;


@Service
@Component
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public Integer saveBoard(BoardDto boardDto, Member member, MultipartFile file) throws Exception{

        Board board = boardDto.createBoard();
        board.setMember(member);

        String projectPath = "C:\\IdeaProjects\\capstone\\src\\main\\resources\\static\\files"; //저장 경로 지정
        UUID uuid = UUID.randomUUID(); //식별자
        String fileName = uuid + "_" + file.getOriginalFilename(); //저장될 파일이름 = 식별자+파일이름
        System.out.println(fileName);
        File saveFile = new File(projectPath, fileName);
        file.transferTo(saveFile);

        board.setFilename(fileName);
        board.setFilepath("/files/" + fileName); //서버에서 접근할 때는 static 아래 경로로만으로도 가능
        boardRepository.save(board);


        return board.getBoardId();
    }

    @Transactional(readOnly = true)
    public BoardDto getBoardDtl(Integer boardId) throws EntityNotFoundException {
        //Board -> BoardDTO
        Board board = boardRepository.findByBoardId(boardId);
        BoardDto boardDto = BoardDto.of(board);
        return boardDto;
    }

    @Transactional(readOnly = true)
    public Page<Board> getBoardPage(Pageable pageable, Integer memberId){
        return boardRepository.findByMemberId(pageable, memberId);
    }

    public Board viewById(Integer boardId) {
        return boardRepository.findByBoardId(boardId);
    }

    public void boardDelete(Integer id){
        boardRepository.deleteById(id);
    }

    public List<Board> findAll(){
        return boardRepository.findAll();
    }
}
