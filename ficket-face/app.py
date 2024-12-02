from flask import Flask, request
from flask_restx import Api, Resource, fields
from flask_cors import CORS
from config import load_config_from_server
from rabbitmq_listener import start_rabbitmq_listener_thread
from eureka_client_setup import initialize_eureka_client
from face_utils import get_face_embedding, cosine_similarity
from vector_security_utils import encrypt_vector, decrypt_vector
from database import db, initialize_database
from models import Face
from response import make_response
from s3_utils import upload_file_to_s3
from werkzeug.datastructures import FileStorage

# Flask 애플리케이션 생성
app = Flask(__name__)

CORS(app)

# 초기 설정 로드
config = load_config_from_server()

# 데이터베이스 초기화
initialize_database(app)

# Swagger UI를 제공하는 API 생성
api = Api(
    app,
    title="FACE SERVICE API",
    version="v1",  # API 명세 버전
    description="Face Service 명세서",
    prefix="/face-swagger/v3/api-docs",  # Swagger UI 경로
    doc="/face-swagger/v3/api-docs",
    openapi="3.0.0"  # OpenAPI 명시적 설정
)

# Namespace 정의
face_ns = api.namespace("faces", description="Face Operations")

# 얼굴 업로드 모델 정의
upload_model = api.model(
    "UploadFace", {
        "ticket_id": fields.Integer(required=True, description="Ticket ID (Long)"),
        "event_schedule_id": fields.Integer(required=True, description="Event Schedule ID (Long)")
    }
)

# 얼굴 매칭 모델 정의
match_model = api.model(
    "MatchFace", {
        "event_schedule_id": fields.Integer(required=True, description="Event Schedule ID (Long)")
    }
)

file_upload_parser = api.parser()
file_upload_parser.add_argument(
    "file",
    location="files",
    type=FileStorage,  # 파일 타입 지정
    required=True,
    help="업로드할 이미지 파일 (얼굴 이미지)",
)
file_upload_parser.add_argument(
    "ticket_id",
    location="form",
    type=int,
    required=True,
    help="티켓 ID (정수값)",
)
file_upload_parser.add_argument(
    "event_schedule_id",
    location="form",
    type=int,
    required=True,
    help="이벤트 일정 ID (정수값)",
)

match_parser = api.parser()
match_parser.add_argument(
    "file",
    location="files",
    type=FileStorage,  # 파일 타입 지정
    required=True,
    help="업로드할 이미지 파일 (얼굴 이미지)",
)
match_parser.add_argument(
    "event_schedule_id",
    location="form",
    type=int,
    required=True,
    help="이벤트 일정 ID (정수값)",
)

# 얼굴 벡터를 DB에 저장하는 엔드포인트
@face_ns.route("/upload")
class UploadFace(Resource):
    @api.expect(file_upload_parser)  # RequestParser 적용
    @api.doc(
        consumes=["multipart/form-data"],  # 요청 Content-Type 명시
    )
    def post(self):
        """얼굴 벡터를 데이터베이스에 저장"""
        args = file_upload_parser.parse_args()  # 파라미터 파싱
        file = request.files.get("file")  # 업로드된 파일 가져오기
        ticket_id = args.get("ticket_id")
        event_schedule_id = args.get("event_schedule_id")

        if not file:
            return make_response(400, "요청에 파일이 포함되지 않았습니다.")

        image_data = file.read()
        embedding = get_face_embedding(image_data)
        if embedding is None:
            return make_response(400, "얼굴이 감지되지 않았습니다.")

        encrypted_embedding = encrypt_vector(embedding)

        # S3에 파일 업로드 및 URL 생성
        file.seek(0)  # 파일 포인터를 처음으로 리셋
        file_url = upload_file_to_s3(file)

        new_face = Face(
            vector=encrypted_embedding,
            face_img=file_url,
            ticket_id=ticket_id,
            event_schedule_id=event_schedule_id
        )
        db.session.add(new_face)
        db.session.commit()

        return make_response(200, "얼굴 등록에 성공했습니다.")

# 입력 얼굴과 DB 얼굴 벡터 비교 엔드포인트
@face_ns.route("/match")
class MatchFace(Resource):
    @api.expect(match_parser)  # 수정된 match_parser 사용
    @api.doc(
        consumes=["multipart/form-data"],  # 요청 Content-Type 명시
    )
    def post(self):
        """입력 얼굴과 DB의 얼굴 벡터 비교"""
        args = match_parser.parse_args()  # 파라미터 파싱
        file = request.files.get("file")  # 업로드된 파일 가져오기
        event_schedule_id = args.get("event_schedule_id")  # 폼 데이터에서 event_schedule_id 가져오기

        if not file:
            return make_response(400, "파일이 업로드되지 않았습니다.")

        if not event_schedule_id:
            return make_response(400, "이벤트 일정 ID가 누락되었습니다.")

        image_data = file.read()
        embedding = get_face_embedding(image_data)
        if embedding is None:
            return make_response(400, "얼굴을 감지하지 못했습니다.")

        # DB에서 해당 event_schedule_id에 해당하는 얼굴 벡터 가져오기
        faces = Face.query.filter_by(event_schedule_id=event_schedule_id).all()

        if not faces:
            return make_response(404, "해당 event_schedule_id에 대한 얼굴이 없습니다.")

        # 유사도 계산
        max_similarity = -1
        best_match = None

        for face in faces:
            decrypted_embedding = decrypt_vector(face.vector)
            similarity = cosine_similarity(embedding, decrypted_embedding)

            if similarity > max_similarity:
                max_similarity = similarity
                best_match = {
                    "face_id": face.face_id,
                    "face_img": face.face_img,
                    "ticket_id": face.ticket_id,
                    "event_schedule_id": face.event_schedule_id,
                    "similarity": float(similarity),
                }

        # 임계값 설정
        threshold = 0.4
        if best_match and max_similarity > threshold:
            return make_response(200, "얼굴이 일치합니다.", best_match)
        else:
            return make_response(404, "일치하는 얼굴을 찾을 수 없습니다.")



# Namespace를 API에 추가
api.add_namespace(face_ns)

if __name__ == "__main__":
    # RabbitMQ 리스너 스레드 시작
    start_rabbitmq_listener_thread(config, app)

    # Eureka 클라이언트 초기화
    initialize_eureka_client()

    # 데이터베이스 테이블 생성
    with app.app_context():
        db.create_all()

    # Flask 앱 실행
    app.run(host='127.0.0.1', port=5000, debug=True)
