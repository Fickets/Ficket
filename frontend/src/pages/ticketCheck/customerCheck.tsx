import React, { useEffect, useRef, useState } from "react";
import { useParams, useLocation } from "react-router-dom";
import * as faceapi from "face-api.js";
import { checkFace } from "../../service/ticketCheck/ticketCheck";
import { Client } from "@stomp/stompjs";
import { SocketMessage } from "../../types/ticketCheck";

const FaceDetectionPage: React.FC = () => {
    const { eventId } = useParams<{ eventId: string }>();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const connectId = queryParams.get("connectId");

    const [viewMessage, setViewMessage] = useState<string>("얼굴인식을 해주세요.")
    const [client, setClient] = useState<Client | null>(null);
    const [socketMessage, setSocketMessage] = useState<SocketMessage | null>(null);
    const [modelsLoaded, setModelsLoaded] = useState(false);
    const [isCapturing, setIsCapturing] = useState(false);
    const [zoneColor, setZoneColor] = useState("purple");
    const detectionZoneSize = { width: 400, height: 300 };

    const videoRef = useRef<HTMLVideoElement>(null);
    const canvasRef = useRef<HTMLCanvasElement>(null);

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

            setZoneColor(detected ? "#00F260" : "#c94b4b"); // 초록(탐지됨) / 빨간(탐지되지 않음)
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
            sendImageToServer(faceImage);
        }
    };

    const sendImageToServer = async (base64Image: string) => {
        try {
            console.log(base64Image)
            const fileImage = dataUrlToFile(base64Image, "image.png");
            const response = await checkFace(fileImage, eventId, connectId);
            console.log(response);
        } catch (error: any) {
            console.log(error.message);
            setViewMessage("얼굴인식에 실패했습니다.")
            setIsCapturing(false)
        }
    };

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

    useEffect(() => {
        const stored = localStorage.getItem("ADMIN_STORE");
        if (stored) {
            const obj = JSON.parse(stored);
            if (obj.state.accessToken !== "") {
                const token = obj.state.accessToken;
                const connectionOptions = {
                    brokerURL: "ws://localhost:9000/ticketing-check/ws",
                    connectHeaders: {
                        Authorization: token,
                    },
                    onConnect: () => {
                        const newClient = new Client();
                        newClient.subscribe(`/sub/check/${eventId}/${connectId}`, (message) => {
                            try {
                                const parsedMessage: SocketMessage = JSON.parse(message.body);
                                if (parsedMessage.data.message != null) {
                                    setSocketMessage(parsedMessage);
                                } else {
                                    setSocketMessage(null);
                                    setIsCapturing(false)
                                    setViewMessage("얼굴인식을 해주세요.")
                                }
                            } catch (error) {
                                console.log("Failed to parse message or invalid data", error);
                            }
                        });
                    },
                    onDisconnect: () => { },
                };
                const newClient = new Client();
                newClient.configure(connectionOptions);
                newClient.activate();
                setClient(newClient);
            }
        }
    }, []);

    return (
        <div className="min-h-screen bg-white text-gray-800">
            <header className="py-4 px-6 bg-purple-500 text-white flex justify-between items-center shadow-md">
                <h1 className="text-[40px] font-bold">Ficket</h1>
                <span className="text-lg font-medium">{connectId}</span>
            </header>
            <main className="flex flex-col items-center py-6">
                <div className="relative w-full max-w-3xl">
                    <video
                        ref={videoRef}
                        autoPlay
                        muted
                        className="w-full h-auto rounded-lg shadow-lg"
                    ></video>
                    <canvas
                        ref={canvasRef}
                        className="absolute top-0 left-0 w-full h-full pointer-events-none"
                    ></canvas>
                    <div
                        className="absolute border-4 pointer-events-none rounded-lg"
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
                <section className="mt-6 bg-purple-100 py-6 px-8 rounded-lg shadow-md w-full max-w-lg">
                    <h2 className="text-[35px] font-bold text-purple-700">검사 결과</h2>

                    {socketMessage ? (
                        <div>
                            <p className="text-[30px] font-semibold text-gray-800">이름: {socketMessage?.name}</p>
                            <p className="text-[30px] font-semibold text-gray-800">생년월일: {socketMessage?.birth}</p>
                            <p className="text-[30px] font-semibold text-gray-800">좌석: </p>

                            <div className="grid grid-cols-2 gap-4">
                                {socketMessage?.seatLoc.map((element, index) => (
                                    <div key={index} className="text-center">
                                        <p className="text-[30px]">{element}</p>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ) : (
                        <p className="text-gray-600 text-[30px]">{viewMessage}</p>
                    )}
                </section>
            </main>
        </div>

    );
};

export default FaceDetectionPage;