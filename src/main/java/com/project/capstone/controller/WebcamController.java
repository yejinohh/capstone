package com.project.capstone.controller;

import com.project.capstone.entity.Board;
import com.project.capstone.entity.CapstoneResult;
import com.project.capstone.entity.DetectResult;
import com.project.capstone.entity.Member;
import com.project.capstone.service.BoardService;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Controller
public class WebcamController {

    @Autowired
    private BoardService boardService;
    ClassPathResource classPathResource;

    @GetMapping("/webcam")
    public String webcam() {

        return "webcam";
    }

    @PostMapping("/getImage")
    @ResponseBody
    public CapstoneResult getImage(@RequestParam Map<String, Object> map,
                           @AuthenticationPrincipal Member member, @RequestBody String requestBody) {
        try {
            //System.out.println("Request Parameters: " + map);
            //특정 매개변수 데이터 확인
            String imageData = (String) map.get("data");
            byte[] decodedImage = Base64.getDecoder().decode(imageData);

            String emotion = (String) map.get("emotion");
            System.out.println("Emotion: " + emotion);

            String projectPath = "C:\\IdeaProjects\\capstone\\src\\main\\resources\\static\\capture"; //저장 경로 지정
            UUID uuid = UUID.randomUUID(); //식별자
            String captureFilename = projectPath + "\\capture_" + uuid + ".jpg";
            FileOutputStream fos = new FileOutputStream(captureFilename);
            fos.write(decodedImage);
            fos.flush();

            if (member.getId() == null) {
                CapstoneResult capstoneResult = new CapstoneResult();
                capstoneResult.setStatus(false);
                capstoneResult.setMessage("memberID: " + member.getId());
                capstoneResult.setData(null);
                return capstoneResult;
            }

            List<Board> board = boardService.findAll();
            List<String> listFilename = new ArrayList<>();

            //현재 유저의 아이디와 같은 게시물을 새로운 리스트로 옮겨줌
            for (int i = 0; i < board.size(); i++) {
                //System.out.println( "boardId : " + board.get(i).getBoardId() + ",  memberId: " + board.get(i).getMember().getId());
                if (member.getId() == board.get(i).getMember().getId()) {
                    listFilename.add(board.get(i).getFilename());
                }
            }

            String repositoryFilepath = "C:\\IdeaProjects\\capstone\\src\\main\\resources\\static\\files\\";
            List<DetectResult> detectResultList = new ArrayList<>();

            for(int i = 0; i < listFilename.size(); i++){
                System.out.println(listFilename.get(i));

                String filename1 = captureFilename;
                String filename2 = repositoryFilepath + listFilename.get(i);

                double similarity = compareFace(filename1, filename2);


                if(similarity > 0.8){
                    DetectResult detectResult = new DetectResult();
                    detectResult.setName("ddd");
                    detectResult.setSimilarity(similarity);
                    detectResult.setEmotion(emotion);
                    detectResult.setCurrentTime(LocalDate.now());
                    detectResultList.add(detectResult);
                    System.out.println("0.8 이상일 경우 출력: " + similarity);
                }
            }

            CapstoneResult capstoneResult = new CapstoneResult();

            if(detectResultList.size() >= 1) {
                capstoneResult.setStatus(true);
                capstoneResult.setCount(detectResultList.size());
                capstoneResult.setMessage("Success");
                capstoneResult.setData(detectResultList);
            } else {
                capstoneResult.setStatus(true);
                capstoneResult.setData(detectResultList);
            }
            return capstoneResult;

        } catch (Exception e) {
            CapstoneResult capstoneResult = new CapstoneResult();
            capstoneResult.setStatus(false);
            capstoneResult.setMessage(e.toString());
            capstoneResult.setData(null);
            return capstoneResult;
        }
    }

    protected double compareFace(String capture, String repository){

        Mat img1 = Imgcodecs.imread(capture, Imgcodecs.IMREAD_COLOR);
        Mat img2 = Imgcodecs.imread(repository, Imgcodecs.IMREAD_COLOR);

        Mat hist1 = new Mat();
        Mat hist2 = new Mat();

        Imgproc.calcHist(Collections.singletonList(img1), new MatOfInt(0), new Mat(), hist1, new MatOfInt(256), new MatOfFloat(0, 256));
        Imgproc.calcHist(Collections.singletonList(img2), new MatOfInt(0), new Mat(), hist2, new MatOfInt(256), new MatOfFloat(0, 256));

        // 히스토그램 데이터 타입 변환
        hist1.convertTo(hist1, CvType.CV_32F);
        hist2.convertTo(hist2, CvType.CV_32F);

        double similarity = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL);

        return similarity;

    }


}
