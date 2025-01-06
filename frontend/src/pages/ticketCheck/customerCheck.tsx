import React, { useEffect, useRef, useState } from "react";
import * as faceapi from "face-api.js";
import { checkFace } from "../../service/ticketCheck/ticketCheck";

const FaceDetectionPage: React.FC = () => {
    const videoRef = useRef<HTMLVideoElement>(null);
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const [modelsLoaded, setModelsLoaded] = useState(false);
    const detectionZone = { x: 100, y: 100, width: 400, height: 300 };
    const [isCapturing, setIsCapturing] = useState(false);
    const [face, setFace] = useState<string | null>(null);

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
            faceapi.draw.drawDetections(canvas, resizedDetections);

            resizedDetections.forEach((detection) => {
                const { x, y, width, height } = detection.box;
                const isInside =
                    x >= detectionZone.x &&
                    y >= detectionZone.y &&
                    x + width <= detectionZone.x + detectionZone.width &&
                    y + height <= detectionZone.y + detectionZone.height;

                if (isInside && !isCapturing) {
                    setIsCapturing(true);
                    captureFace(video, detection.box);
                }
            });
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

            const faceFile = dataUrlToFile(faceImage, "image.png");
            sendImageToServer(faceFile);
            setTimeout(() => setIsCapturing(false), 5000); // Allow recapture after delay 5 second
        }
    };

    const dataUrlToFile = (url: string, fileName: string) => {
        const image_data = atob(url.split(",")[1]); // data:image/gif;base64 필요없으니 떼주고, base64 인코딩을 풀어준다

        const arraybuffer = new ArrayBuffer(image_data.length);
        const view = new Uint8Array(arraybuffer);

        for (let i = 0; i < image_data.length; i++) {
            view[i] = image_data.charCodeAt(i) & 0xff;
        }

        const blob = new Blob([arraybuffer], { type: "image/png" }); // Blob 타입 명시
        const file = new File([blob], fileName, {
            type: "image/png",
            lastModified: Date.now(),
        });

        return file;
    };

    const sendImageToServer = async (file: File) => {
        try {
            const response = await checkFace(file, 1);
            console.log(response);
        } catch (error: any) {
            alert(error.message);
        }
    };

    return (
        <div className="relative w-[640px] h-auto mx-auto mt-10 bg-gray-800 text-white p-4 rounded-md">
            {/* 상단 텍스트 */}
            <div className="flex justify-between items-center mb-4">
                <h2 className="text-lg font-bold">Ficket</h2>
                <span className="text-lg">4</span>
            </div>

            {/* 비디오 및 캔버스 */}
            <div className="relative">
                <video
                    ref={videoRef}
                    autoPlay
                    muted
                    className="absolute top-0 left-0 w-full border border-black"
                ></video>
                <canvas
                    ref={canvasRef}
                    className="absolute top-0 left-0 w-full pointer-events-none"
                ></canvas>
                {/* 녹색 테두리와 텍스트 */}
                <div
                    className="absolute border-4 border-green-500"
                    style={{
                        top: `${detectionZone.y}px`,
                        left: `${detectionZone.x}px`,
                        width: `${detectionZone.width}px`,
                        height: `${detectionZone.height}px`,
                    }}
                ></div>
                <p
                    className="absolute bottom-0 left-1/2 transform -translate-x-1/2 bg-black bg-opacity-70 text-orange-400 p-2 rounded"
                    style={{
                        top: `${detectionZone.y + detectionZone.height + 10}px`,
                    }}
                >
                    조금 더 떨어져 주세요
                </p>
            </div>

            {/* 하단 정보 */}
            <div className="mt-6 text-center">
                <div className="text-gray-200">
                    <p>
                        h0 h1 h2 h3 h4 h5
                    </p>
                    <p>
                        g0 g1 g2
                    </p>
                </div>
            </div>
        </div>
    );
};

export default FaceDetectionPage;
