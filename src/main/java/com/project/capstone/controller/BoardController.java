package com.project.capstone.controller;

import com.project.capstone.dto.BoardDto;
import com.project.capstone.entity.Board;
import com.project.capstone.entity.Member;
import com.project.capstone.service.BoardService;
import com.project.capstone.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.model.IModel;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

@Controller
@Log
@RequiredArgsConstructor
public class BoardController {

    @Autowired
    private BoardService boardService;
    private MemberService memberService;

     //등록 초기 화면
    @GetMapping("/upload/new")
    public String boardForm(Model model){
        model.addAttribute("boardDto", new BoardDto());
        return "board/boardForm";
    }

    //등록 완료
    @PostMapping("/upload/new")
    public String boardNew(@Valid BoardDto boardDto, BindingResult bindingResult, Model model, MultipartFile file,
                           @AuthenticationPrincipal Member member)throws Exception {

        if (bindingResult.hasErrors()) {
            return "board/boardForm";
        }

        if(file.isEmpty()){
          model.addAttribute("errorMessage", "사진은 필수 입력 값입니다");
          return "board/boardForm";
        }

        try {
            System.out.println("Name: " + member.getName() + "  Password: " + member.getPassword() + "  Role: " + member.getRole() + "   Id: " + member.getId());
            System.out.println(boardDto);
            boardService.saveBoard(boardDto, member, file);
            model.addAttribute("message","글 작성이 완료되었습니다");

        } catch (Exception e) {
            model.addAttribute("errorMessage", "글 등록 중 에러가 발생하였습니다.");
            return "board/boardForm";
        }

        return "/board/boardList";
    }

    //게시글 목록
    @GetMapping("/board/list")
    public String boardList(Model model, @PageableDefault(size=8  ,sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable,
                            @AuthenticationPrincipal Member member, Principal principal) {


        Page<Board> list = boardService.getBoardPage(pageable, member.getId());

        int nowPage = list.getPageable().getPageNumber();
        int startPage = Math.max(nowPage - 4 ,1);
        int endPage = Math.min(nowPage + 5, list.getTotalPages());

        model.addAttribute("list", list); //반환 List를 list라는 이름으로 받아서 넘김
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        //System.out.println(nowPage + ". " + startPage + ",  " + endPage);

        return"/board/boardList";

    }

    //게시글 수정
    @GetMapping("/modify/{boardId}")
    public String boardDtl(@PathVariable("boardId") Integer boardId, Model model){

        try {
            BoardDto boardDto = boardService.getBoardDtl(boardId);
            model.addAttribute("boardDto", boardDto);

        } catch(EntityNotFoundException e){
            model.addAttribute("errorMessage", "존재하지 않는 상품 입니다.");
            model.addAttribute("boardDto", new BoardDto());
            return "board/boardForm";
        }
        return "board/boardForm";
    }

    //게시글 수정 완료
    @PostMapping("/modify/{boardId}")
    public String boardUpdate(@Valid BoardDto boardDto, BindingResult bindingResult, Model model, MultipartFile file, @AuthenticationPrincipal Member member)throws Exception {

        if (bindingResult.hasErrors()) {
            return "board/boardForm";
        }

        if(file.isEmpty()){
            model.addAttribute("errorMessage", "사진은 필수 입력 값입니다");
            return "board/boardForm";
        }

        try {
            boardService.saveBoard(boardDto, member, file);
            model.addAttribute("message","글 수정이이 완료되었습니다");

        } catch (Exception e) {
            model.addAttribute("errorMessage", "글 수정 중 에러가 발생하였습니다.");
            return "board/boardForm";
        }

        return "/index";
    }

    // 게시글 상세보기
    @GetMapping("/board/view/{boardId}")
    public String boardView(@PathVariable Integer boardId, @AuthenticationPrincipal Member member, Model model){

        Board board = boardService.viewById(boardId);

        if(board == null) {
            return "redirect:/board/list";

        }

        if(board.getMember().getId() != member.getId()){
            System.out.println("접근 권한 없음");
            return "redirect:/board/list";
        }

        model.addAttribute("board", board);
        return "board/boardView";
    }

    @PostMapping("/board/delete")
    public String boardDelete(@RequestParam("boardId") Integer boardId) {
        boardService.boardDelete(boardId);
        return "redirect:/board/list";
    }


}
