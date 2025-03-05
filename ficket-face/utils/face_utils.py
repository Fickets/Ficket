from insightface.app import FaceAnalysis
import cv2
import numpy as np
from config import logger

# Lazy Loading으로 모델 불러오기 (최초 실행 시 로드)
face_analyzer = None

def get_face_analyzer():
    """Lazy Initialization of FaceAnalysis for optimized loading"""
    global face_analyzer
    if face_analyzer is None:
        logger.info("Loading FaceAnalysis model... (CPU Mode)")
        face_analyzer = FaceAnalysis(
            name='buffalo_l',  # 정확도가 높은 모델 사용
            allowed_modules=['detection', 'recognition'],
            providers=['CPUExecutionProvider']  # CPU 사용
        )
        face_analyzer.prepare(ctx_id=-1)  # CPU 모드
        face_analyzer.det_size = (640, 640)  # 감지 사이즈 최적화
    return face_analyzer

def preprocess_image(image_data):
    """Preprocess image: Decode, resize, and convert to RGB."""
    try:
        img_array = np.frombuffer(image_data, np.uint8)
        img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)
        if img is None:
            logger.error("Failed to decode image")
            return None

        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        img = cv2.resize(img, (112, 112))  # 모델 입력 크기로 변환
        return img
    except Exception as e:
        logger.error(f"Error in preprocess_image: {str(e)}")
        return None

def get_face_embedding(image_data):
    """Extract optimized face embedding."""
    try:
        img = preprocess_image(image_data)
        if img is None:
            return None

        face_analyzer = get_face_analyzer()
        faces = face_analyzer.get(img)

        if not faces:
            logger.warning("No face detected.")
            return None

        embedding = faces[0].embedding
        return embedding / np.linalg.norm(embedding)  # L2 정규화
    except Exception as e:
        logger.error(f"Error extracting face embedding: {str(e)}")
        return None

def cosine_similarity(embedding1, embedding2):
    """Compute cosine similarity between two embeddings."""
    try:
        return np.dot(embedding1, embedding2) / (np.linalg.norm(embedding1) * np.linalg.norm(embedding2))
    except Exception as e:
        logger.error(f"Error calculating cosine similarity: {str(e)}")
        return 0.0
