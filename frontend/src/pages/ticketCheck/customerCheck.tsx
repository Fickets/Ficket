import React, { useEffect, useRef, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import * as faceapi from "face-api.js";
import { checkFace } from '../../service/ticketCheck/ticketCheck';
import { Client } from '@stomp/stompjs';
import { SocketMessage } from "../../types/ticketCheck";

const FaceDetectionPage: React.FC = () => {
    const { eventId } = useParams<{ eventId: string }>();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const connectId = queryParams.get("connectId");

    const [client, setClient] = useState<Client | null>(null)

    const [socketMessage, setSocketMessage] = useState<SocketMessage | null>(null);



    const videoRef = useRef<HTMLVideoElement>(null);
    const canvasRef = useRef<HTMLCanvasElement>(null);

    const [modelsLoaded, setModelsLoaded] = useState(false);
    const [isCapturing, setIsCapturing] = useState(false);
    const [zoneColor, setZoneColor] = useState("green");
    const detectionZoneSize = { width: 400, height: 300 }; // 감지 영역 크기 (고정값)

    const startVideo = async () => {
        const stream = await navigator.mediaDevices.getUserMedia({ video: true });
        if (videoRef.current) {
            videoRef.current.srcObject = stream;
        }
    };

    useEffect(() => {
        const loadModels = async () => {
            await faceapi.nets.tinyFaceDetector.loadFromUri("/models");
            setModelsLoaded(true);
            startVideo();
        };

        loadModels();

        return () => {
            if (videoRef.current?.srcObject) {
                const stream = videoRef.current.srcObject as MediaStream;
                stream.getTracks().forEach((track) => track.stop());
            }
        };
    }, []);

    useEffect(() => {
        if (!modelsLoaded || !videoRef.current || !canvasRef.current) return;

        const video = videoRef.current;
        const canvas = canvasRef.current;

        const detectFaces = async () => {
            const displaySize = { width: video.videoWidth, height: video.videoHeight };
            faceapi.matchDimensions(canvas, displaySize);

            const detections = await faceapi.detectAllFaces(
                video,
                new faceapi.TinyFaceDetectorOptions()
            );
            const resizedDetections = faceapi.resizeResults(detections, displaySize);

            const context = canvas.getContext("2d");
            if (!context) return;

            context.clearRect(0, 0, canvas.width, canvas.height);

            let detected = false;

            resizedDetections.forEach((detection) => {
                const { x, y, width, height } = detection.box;
                const isInside =
                    x >= video.videoWidth / 2 - detectionZoneSize.width / 2 &&
                    y >= video.videoHeight / 2 - detectionZoneSize.height / 2 &&
                    x + width <= video.videoWidth / 2 + detectionZoneSize.width / 2 &&
                    y + height <= video.videoHeight / 2 + detectionZoneSize.height / 2;

                if (isInside) {
                    detected = true;
                    if (!isCapturing) {
                        setIsCapturing(true);
                        captureFace(video, detection.box);
                    }
                }
            });

            setZoneColor(detected ? "red" : "green");
        };

        const interval = setInterval(detectFaces, 100);

        return () => clearInterval(interval);
    }, [modelsLoaded, isCapturing]);

    const captureFace = (video: HTMLVideoElement, box: faceapi.Box) => {
        const canvas = document.createElement("canvas");
        canvas.width = box.width;
        canvas.height = box.height;
        const context = canvas.getContext("2d");

        if (context) {
            context.drawImage(
                video,
                box.x,
                box.y,
                box.width,
                box.height,
                0,
                0,
                box.width,
                box.height
            );

            const faceImage = canvas.toDataURL("image/png");
            console.log(faceImage);
            // const faceFile = dataUrlToFile(faceImage, "image.png"); // Data URL을 파일로 변환
            sendImageToServer(faceImage)
            // setTimeout(() => setIsCapturing(false), 5000);
        }
    };

    // 서버로 이미지를 전송하는 함수
    const sendImageToServer = async (base64Image: string) => {
        try {
            // Base64 데이터에서 실제 이미지만 추출 (비동기 처리)
            const fileImage = dataUrlToFile(base64Image, "image.png");

            // 서버로 POST 요청
            const response = await checkFace(fileImage, eventId, connectId);
            console.log(response);
        } catch (error: any) {
            alert(error.message);
        }
    };

    // Data URL을 파일로 변환하는 함수
    const dataUrlToFile = (url: string, fileName: string) => {
        const image_data = atob(url.split(",")[1]);
        const arraybuffer = new ArrayBuffer(image_data.length);
        const view = new Uint8Array(arraybuffer);

        for (let i = 0; i < image_data.length; i++) {
            view[i] = image_data.charCodeAt(i) & 0xff;
        }

        const blob = new Blob([arraybuffer], { type: "image/png" });
        return new File([blob], fileName, {
            type: "image/png",
            lastModified: Date.now(),
        });
    };

    // 세션연결
    useEffect(() => {
        const stored = localStorage.getItem('ADMIN_STORE');
        if (stored) {
            const obj = JSON.parse(stored);
            if (obj.state.accessToken !== '') {
                const token = obj.state.accessToken
                // WebSocket 연결 설정
                const connectionOptions = {
                    brokerURL: 'ws://localhost:9000/ticketing-check/ws',
                    connectHeaders: {
                        Authorization: token
                    }, // 연결 시 헤더 설정
                    onConnect: () => {
                        newClient.subscribe(
                            `/sub/check/${eventId}/${connectId}`,
                            message => {
                                try {
                                    // JSON 메시지를 파싱
                                    const parsedMessage: SocketMessage = JSON.parse(message.body);
                                    console.log(parsedMessage)
                                    if (parsedMessage.data.message != null) {
                                        setSocketMessage(parsedMessage);
                                    } else {
                                        setSocketMessage(null);
                                        setIsCapturing(false)
                                    }
                                    // 상태 업데이트
                                } catch (error) {
                                    console.error("Failed to parse message or invalid data", error);
                                }

                            },
                        );
                    },
                    onDisconnect: () => { }
                };
                const newClient = new Client();
                newClient.configure(connectionOptions);
                // 웹소켓 세션 활성화
                newClient.activate();
                setClient(newClient);
            }
        }
    }, []);


    return (
        <div className="relative bg-gray-800 text-white rounded-md min-h-screen">
            <div className="flex ml-[20px] pt-[25px]">
                <h2 className="text-[40px] font-bold mb-4 text-center">Ficket</h2>
                <p className="text-[40px] ml-[15px]">{connectId}</p>
            </div>
            <div className="relative">
                <video
                    ref={videoRef}
                    autoPlay
                    muted
                    className="p-[50px] w-full h-auto rounded-md"
                ></video>
                <canvas
                    ref={canvasRef}
                    className="absolute top-0 left-0 w-full h-auto pointer-events-none"
                ></canvas>
                <div
                    className="absolute border-4 pointer-events-none"
                    style={{
                        top: "50%",
                        left: "50%",
                        transform: "translate(-50%, -50%)",
                        width: `${detectionZoneSize.width}px`,
                        height: `${detectionZoneSize.height}px`,
                        borderColor: zoneColor,
                        transition: "border-color 0.3s ease",
                    }}
                ></div>
            </div>
            <div className="flex flex-col bg-gray-800 items-center">
                <div className="w-full h-[460px] border border-black bg-black">
                    <p className="text-[40px] m-[35px]">이름: {socketMessage?.name}</p>
                    <p className="text-[40px] m-[35px]">생년: {socketMessage?.birth}</p>
                    <p className="text-[40px] m-[35px]">자리: {socketMessage?.seatLoc}</p>
                </div>
            </div>
        </div>

    );
};

export default FaceDetectionPage;