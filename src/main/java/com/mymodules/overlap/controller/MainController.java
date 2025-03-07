package com.mymodules.overlap.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MainController {

    @GetMapping("/")
    public ModelAndView landingPage()
    {
        return new ModelAndView("landing" );
    }
    @GetMapping("/create-schedule")
    public ModelAndView scheduleCreatePage()
    {
        return new ModelAndView("create-schedule" );
    }

    @GetMapping("/select-time")
    public ModelAndView selectTime()
    {
        return new ModelAndView("select_constructor_time" );
    }
    @GetMapping("/test")
    public ModelAndView test(){
        return new ModelAndView("schedule");
    }

    @GetMapping("/main")
    public ModelAndView main(){
        return new ModelAndView("template" );
    }

    @GetMapping("/nav")
    public ModelAndView nav(){
        return new ModelAndView("nav" );
    }



//    // Create Board
//    @PostMapping("/create")
//    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto req) {
//        BoardResponseDto responseDto = boardService.createArticle(req);
//        return ResponseEntity.ok(responseDto);
//    }

//    // 게시판 페이지 렌더링
//    @GetMapping("/")
//    public ModelAndView boardPage() {
//        return new ModelAndView("board-list");
//    }
//
//    // 게시글 목록 API
//    @GetMapping("/api/boards")
//    public ResponseEntity<List<BoardResponseDto>> getBoardList() {
//        List<BoardResponseDto> responseDto = boardService.getBoardList();
//        return ResponseEntity.ok(responseDto);
//    }
//
//    // Read Board Details
//    @GetMapping("/detail/{boardId}")
//    public ResponseEntity<BoardDetailResponseDto> getBoardDetail(@PathVariable Long boardId) {
//        BoardDetailResponseDto responseDto = boardService.getBoardDetail(boardId);
//        System.out.println(responseDto);
//        System.out.println(ResponseEntity.ok(responseDto));
//        return ResponseEntity.ok(responseDto);
//    }
//
//    // Update Board
//    @PutMapping("/update/{boardId}")
//    public ResponseEntity<String> updateBoard(@PathVariable Long boardId, @RequestBody BoardRequestDto req) {
//        boardService.updateBoard(req, boardId);
//        return ResponseEntity.ok("게시글이 성공적으로 수정되었습니다.");
//    }
//
//    // Delete Board
//    @DeleteMapping("/delete/{boardId}")
//    public ResponseEntity<String> deleteBoard(@PathVariable Long boardId) {
//        boardService.deleteBoard(boardId);
//        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
//    }


}
