# Python 3.12을 기본 이미지로 사용
FROM python:3.12

# 필수 시스템 라이브러리 설치 (OpenCV 실행을 위한 libGL.so.1 포함)
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*  # 캐시 삭제로 이미지 크기 최적화

# 작업 디렉터리 설정
WORKDIR /app

# 환경 변수 설정
ENV CONFIG_SERVER_URL=http://ec2-54-180-138-77.ap-northeast-2.compute.amazonaws.com:8888/face-service/prod
ENV EUREKA_URL=http://ec2-54-180-138-77.ap-northeast-2.compute.amazonaws.com:8761/eureka
ENV EUREKA_INSTANCE_HOST=ec2-54-180-229-80.ap-northeast-2.compute.amazonaws.com

# 최신 pip & 필수 Python 패키지 설치
RUN pip install --no-cache-dir --upgrade pip setuptools wheel

# 의존성 목록 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 프로젝트 코드 복사
COPY . .

# 마이그레이션 실행 후 Flask 실행
CMD ["sh", "-c", "flask db upgrade && python3 -m flask run --host=0.0.0.0 --port=5000"]