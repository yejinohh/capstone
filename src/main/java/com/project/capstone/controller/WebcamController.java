package com.project.capstone.controller;

import com.project.capstone.entity.*;
import com.project.capstone.service.BoardService;
import net.minidev.json.JSONObject;
import org.aspectj.bridge.MessageUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
@Controller
public class WebcamController {

    @Autowired
    private BoardService boardService;
    private OAuthToken oAuthToken;
    private MessageUtil logger;


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
                capstoneResult.setDetectResult(null);
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
            DetectResult detectResult= new DetectResult();

            for(int i = 0; i < listFilename.size(); i++){
                System.out.println(listFilename.get(i));
                String filename1 = captureFilename;
                String filename2 = repositoryFilepath + listFilename.get(i);

                double similarity = compareFace(filename1, filename2);


                if(similarity > 0.8){
                    //System.out.println(listFilename.get(i));
                    //날짜
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String stringDate = simpleDateFormat.format(new Date());
                    //유사도
                    int similarityPercentage = (int) (similarity * 100);
                    //감정
                    Map<String, String> labelMapping = new HashMap<>();
                    labelMapping.put("angry", "화남");
                    labelMapping.put("disgusted", "역겨움");
                    labelMapping.put("fearful", "불안");
                    labelMapping.put("happy", "기쁨");
                    labelMapping.put("neutral", "중립");
                    labelMapping.put("sad", "슬픔");
                    labelMapping.put("surprised", "놀람");

                    detectResult.setName(board.get(i).getTitle());
                    detectResult.setSimilarity(similarityPercentage);
                    detectResult.setEmotion(labelMapping.get(emotion));
                    detectResult.setCurrentTime(stringDate);
                }
            }
            
            CapstoneResult capstoneResult = new CapstoneResult();

            if(detectResult.getName() != null) {
                capstoneResult.setStatus(true);
                capstoneResult.setMessage("Success");
                capstoneResult.setDetectResult(detectResult);
                //카카오메시지
                sendKakaoMessage(capstoneResult.getDetectResult(), member);

            } else {
                capstoneResult.setStatus(false);
                capstoneResult.setDetectResult(null);
                System.out.println("사람이 탐지 되었지만 발견인물은 없음");
            }

            return capstoneResult;

        } catch (Exception e) {
            CapstoneResult capstoneResult = new CapstoneResult();
            capstoneResult.setStatus(false);
            capstoneResult.setMessage(e.toString());
            capstoneResult.setDetectResult(null);
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

    protected void sendKakaoMessage(DetectResult detectResult, @AuthenticationPrincipal Member member){

        String text = "⚠\uFE0F등록인물이 탐지 되었습니다.⚠\uFE0F\r\n" +
                "✔\uFE0F발견 인물 : " +  detectResult.getName() + "\n" +
                "✔\uFE0F유사도 : " + detectResult.getSimilarity() + "% \n" +
                "✔\uFE0F발견 시간: " + detectResult.getCurrentTime() + "\n" +
                "✔\uFE0F감정 예측: " + detectResult.getEmotion() + "\n" ;

        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer " + member.getToken());
        headers.add("Content-type", "application/x-www-form-urlencoded");

        JSONObject templateObject = new JSONObject();
        templateObject.put("object_type", "text");
        templateObject.put("text", text);

        JSONObject linkObject = new JSONObject();
        linkObject.put("web_url", "https://localhost:8080.com");
        linkObject.put("mobile_web_url", "https://localhost:8080.com");


        templateObject.put("link", linkObject);
        templateObject.put("button_title", "바로 확인");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("template_object", templateObject.toString());


        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = rt.exchange(
                    "https://kapi.kakao.com/v2/api/talk/memo/default/send",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
        } catch (Exception e) {
            // 예외 발생 시 예외 메시지 출력 또는 로깅
            e.printStackTrace();
        }

        if (responseEntity != null) {
            int statusCode = responseEntity.getStatusCodeValue();
            System.out.println("응답 상태 코드: " + statusCode);

            String responseBody = responseEntity.getBody().toString();
            System.out.println("응답 본문: " + responseBody);
        } else {
            System.out.println("응답이 없습니다.");
        }

    }

}
