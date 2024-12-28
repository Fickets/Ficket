# Flask 프로젝트 초기 실행 가이드

## 1. 가상환경 설정
Flask 프로젝트를 시작하기 위해 가상환경을 설정합니다.

### 가상환경 생성 및 활성화
```bash
# 가상환경 생성 (venv는 폴더 이름)
python -m venv venv

# 가상환경 활성화 (OS에 따라 명령어가 다름)
# Windows
venv\Scripts\activate

# macOS/Linux
source venv/bin/activate
```

### 가상환경 비활성화
```bash
deactivate
```

---

## 2. 라이브러리 설치

### 설치된 라이브러리 목록 저장
```bash
pip freeze > requirements.txt
```

### 저장된 목록으로 라이브러리 설치
```bash
pip install -r requirements.txt
```

---

## 3. 데이터베이스 마이그레이션 설정
Flask-Migrate를 사용하여 데이터베이스 마이그레이션을 관리합니다.

### 1) 초기화
```bash
flask db init
```

### 2) 마이그레이션 생성
```bash
flask db migrate -m "Initial migration"
```

### 3) 마이그레이션 적용
```bash
flask db upgrade
```
---

## 4. Flask 애플리케이션 실행
Flask 애플리케이션을 실행하여 초기 설정이 완료되었는지 확인합니다.

```bash
flask run
```

### 실행 결과
- 기본적으로 `http://127.0.0.1:5000`에서 애플리케이션이 실행됩니다.

---

## 5. 주요 명령어 정리
- 가상환경 생성: `python -m venv venv`
- 가상환경 활성화: `source venv/bin/activate` (macOS/Linux) 또는 `venv\Scripts\activate` (Windows)
- Flask 실행: `flask run`
- 데이터베이스 초기화: `flask db init`
- 마이그레이션 생성: `flask db migrate -m "message"`
- 마이그레이션 적용: `flask db upgrade`

---


## 6. 프로젝트 구조 예시
```plaintext
```plaintext
ficket-face/
├── app.py                # Flask 애플리케이션 진입점
├── config/               # 설정 관련 모듈
│   ├── __init__.py
│   ├── config.py
│   ├── eureka_client_setup.py
│   ├── rabbitmq_listener.py
│   └── s3_config.py
├── database/             # 데이터베이스 설정
│   ├── __init__.py
│   └── database.py
├── face_app/             # 주요 애플리케이션 코드
│   ├── apis/
│   │   ├── __init__.py
│   │   └── api.py
│   ├── models/
│   │   ├── __init__.py
│   │   └── model.py
│   └── schemas/
│       ├── __init__.py
│       ├── response.py
│       └── schema.py
├── migrations/           # 마이그레이션 관련 파일
│   ├── versions/
│   ├── env.py
│   ├── README
│   ├── script.py.mako
│   └── alembic.ini
├── utils/                # 유틸리티 모듈
│   ├── __init__.py
│   ├── face_utils.py
│   ├── prometheus_metrics.py
│   ├── response.py
│   ├── s3_utils.py
│   ├── tracing.py
│   └── vector_security_utils.py
├── venv/                 # 가상환경 폴더
├── .gitignore            # Git에 포함하지 않을 파일 목록
├── private.pem           # 개인 키 파일
├── requirements.txt      # 의존성 관리 파일
└── README.md             # 프로젝트 설명 파일
```
