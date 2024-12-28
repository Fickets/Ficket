# face_utils.py
from insightface.app import FaceAnalysis
import cv2
import numpy as np

# FaceAnalysis 객체 생성 및 초기화
app = FaceAnalysis(name='buffalo_l', allowed_modules=['detection', 'recognition'], providers=['CPUExecutionProvider'])
app.prepare(ctx_id=-1)  # CPU 모드 (GPU가 있는 경우 ctx_id=0)

def get_face_embedding(image_data):
    # 이미지 바이트 데이터를 메모리에서 바로 디코딩
    img_array = np.frombuffer(image_data, np.uint8)
    img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

    # 얼굴 분석 (탐지 및 인식)
    faces = app.get(img)

    # 첫 번째 얼굴의 임베딩(벡터) 반환
    if faces:
        return faces[0].embedding
    else:
        print("No face_app detected in the provided image data")
        return None

def cosine_similarity(embedding1, embedding2):
    # 코사인 유사도 계산
    dot_product = np.dot(embedding1, embedding2)
    norm1 = np.linalg.norm(embedding1)
    norm2 = np.linalg.norm(embedding2)
    return dot_product / (norm1 * norm2)