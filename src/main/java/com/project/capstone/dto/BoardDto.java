package com.project.capstone.dto;

import com.project.capstone.entity.Board;
import com.project.capstone.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BoardDto {

    private Integer boardId;

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    private String title;
    
    private String content;

    private String filename;

    private String filepath;

    private Member member;

    //엔티티 자체를 반환 할 수 있지만 그럴때는 엔티티 클래스에 화면에서만 사용하는 값이 추가됨.
    //상픔 등록: 화면으로 전달받은 DTO객체를 Entity객체로 변환
    //상품 조회: Entity객체를 DTO객체로 바꿔주는 작업을 함
    private static ModelMapper modelMapper = new ModelMapper();
    public Board createBoard(){
        return modelMapper.map(this, Board.class);
    }
    public static BoardDto of (Board board){
        return modelMapper.map(board, BoardDto.class);
    }

}
