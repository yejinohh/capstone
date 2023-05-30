let cameraStream;
const videoElement = document.getElementById('videoElement');
let isCaptureEnabled = true;

Promise.all([
     faceapi.nets.tinyFaceDetector.loadFromUri("./models"),
     faceapi.nets.faceLandmark68Net.loadFromUri("./models"),
     faceapi.nets.faceRecognitionNet.loadFromUri("./models"),
     faceapi.nets.faceExpressionNet.loadFromUri("./models"),
 ]);
 //카메라 시작
 function startCamera() {

     navigator.mediaDevices.getUserMedia({ video: true })
         .then(function(stream) {
             videoElement.srcObject = stream;
             cameraStream = stream;
         })
         .catch(function(error) {
             console.log('Error accessing camera: ', error);
         });
 }
 //카메라 종료
 function stopCamera() {
     if (cameraStream && cameraStream.getTracks().length > 0) {
         cameraStream.getTracks().forEach(function(track) {
             track.stop();
         });
         videoElement.srcObject = null;

     }
 }

 //얼굴 감지 및 표정 분석
 function startDetection(){

    console.log("startDetection");
    const canvas = faceapi.createCanvasFromMedia(videoElement);
    document.body.append(canvas);

    const displaySize = { width: videoElement.width, height: videoElement.height };

    faceapi.matchDimensions(canvas, displaySize);

    //사람이 감지 되었는지 초기화
    let isPersonDetected = false;
    let captureCount = 0;
    let captureTimer;

    //10분마다 한번씩 캡쳐 되도록함(10분동안 비활성화)
    function startCaptureTimer(){
        captureTimer = setTimeout(() => {
            clearTimeout(captureTimer);
            captureTimer = undefined;
        }, 600000);
    }

    async function captureImage(maxExpression){

        const captureCanvas = document.createElement("canvas");
        captureCanvas.width = videoElement.width;
        captureCanvas.height = videoElement.height;

        const context = captureCanvas.getContext("2d");
        context.drawImage(videoElement, 0, 0, captureCanvas.width, captureCanvas.height);
        const imageData = context.getImageData(0,0,captureCanvas.width, captureCanvas.height);

        const imageUrl = captureCanvas.toDataURL();

       if(imageData){
            //const img = document.createElement('img');
            //img.src = imageUrl;
            //document.body.appendChild(img);
            await ajaxSendImage(imageUrl, maxExpression);
        }

        captureCount++;
    }

    setInterval(async () => {

        const detections = await faceapi
           .detectAllFaces(videoElement, new faceapi.TinyFaceDetectorOptions())
           .withFaceLandmarks()
           .withFaceExpressions();

        if(detections.length > 0){
            console.log("only detecting")
        }

        if(detections.length > 0 && !isPersonDetected && !captureTimer){
            console.log("--detected & captured--");

            const face = detections[0];
            const expressions = face.expressions;
            console.log(expressions)
            const maxExpression = Object.keys(expressions).reduce((a, b) => expressions[a] > expressions[b] ? a : b);
            //console.log('Emotions:', maxExpression, expressions[maxExpression]);
            console.log(face);

            isPersonDetected = true;
            //이미지 캡쳐
            captureImage(maxExpression);
            //캡쳐 타이머 시작
            startCaptureTimer();

        }else if(detections.length == 0 ){
        //사람이 감지 되지 않으면
            isPersonDetected = false;
        }
    },3000);
 }

//버튼 이벤트 발생시 startDetection 시작
videoElement.addEventListener("playing", () => {
     startDetection();
});


const startButton = document.getElementById('startButton');
startButton.addEventListener('click', startCamera);

const stopButton = document.getElementById('stopButton');
stopButton.addEventListener('click', stopCamera);



async function ajaxSendImage(imageUrl, maxExpression) {

     let imageData = imageUrl.split(',')[1]; // 이미지 데이터 부분 추출

    let image = {
        "data" : imageData,
        "emotion" : maxExpression
    }

    await $.ajax({
         type : "POST",       // HTTP method type(GET, POST)
         url : "/getImage",   // 컨트롤러에서 대기중인 URL 주소
         data : image,        // Json 형식의 데이터
         success : function(data){   // 비동기통신의 성공일경우 success 콜백, data는 응답받은 데이터
         console.log(data);
             if(data["status" == true]){
                console.log(data["count"]);
             }else{
                console.log(data["message"]);
             }
         },
         error : function(XMLHttpRequest, textStatus, errorThrown){ // 비동기 통신이 실패할경우 error 콜백
             alert("fail")
         }
    });
}