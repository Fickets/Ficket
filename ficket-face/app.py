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
    openapi="3.0.0"  # OpenAPI 명시적 설정
)

# Namespace 정의
face_ns = api.namespace("api/v1/faces", description="Face Operations")

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
# 얼굴 관계 설정 모델 정의
relationship_model = api.model(
    "SetRelationship", {
        "faceId": fields.Integer(required=True, description="Face ID"),
        "faceImgUrl": fields.String(required=True, description="Face Image URL"),
        "ticketId": fields.Integer(required=True, description="Ticket ID"),
        "eventScheduleId": fields.Integer(required=True, description="Event Schedule ID")
    }
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
            ticket_id=None,
            event_schedule_id=event_schedule_id
        )
        db.session.add(new_face)
        db.session.commit()

        # 응답에 face_id와 face_img(URL)를 포함
        return make_response(200, "얼굴 등록에 성공했습니다.", {
            "faceId": new_face.face_id,
            "faceUrl": new_face.face_img
        })

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


@face_ns.route("/<int:ticket_id>")
class DeleteFace(Resource):
    def delete(self, ticket_id):
        """
        주어진 ticketId에 해당하는 얼굴 데이터를 삭제
        """
        # 데이터베이스에서 해당 ticketId에 해당하는 Face 레코드 조회
        face = Face.query.filter_by(ticket_id=ticket_id).first()

        if not face:
            return make_response(404, "해당 ticketId에 대한 얼굴 데이터가 없습니다.")

        # 데이터 삭제
        db.session.delete(face)
        db.session.commit()

        return make_response(200, "얼굴 데이터가 성공적으로 삭제되었습니다.")

@face_ns.route("/set-relationship")
class SetRelationship(Resource):
    @api.expect(relationship_model)  # 요청 모델
    @api.doc(description="얼굴 데이터와 Ticket 및 Event Schedule 관계를 설정")
    def post(self):
        """얼굴 데이터 관계 설정"""
        # 요청 데이터 파싱
        data = request.json
        face_id = data.get("faceId")
        face_img_url = data.get("faceImgUrl")
        ticket_id = data.get("ticketId")
        event_schedule_id = data.get("eventScheduleId")

        # 유효성 검사
        if not all([face_id, face_img_url, ticket_id, event_schedule_id]):
            return make_response(400, "모든 필드를 입력해야 합니다.")

        # 데이터베이스에서 Face 엔트리 조회
        face = Face.query.filter_by(face_id=face_id).first()
        if not face:
            return make_response(404, f"Face ID {face_id}에 해당하는 데이터가 없습니다.")

        if face.face_img != face_img_url:
            return make_response(409, f"Face Img가 request와 일치하지 않습니다.")

        if face.event_schedule_id != event_schedule_id :
            return make_response(409, f"Event Schedule Id가 request와 일치하지 않습니다.")


        face.ticket_id = ticket_id

        try:
            db.session.commit()
            return make_response(200, "관계가 성공적으로 설정되었습니다.", {
                "faceId": face.face_id,
                "faceImgUrl": face.face_img,
                "ticketId": face.ticket_id,
                "eventScheduleId": face.event_schedule_id
            })
        except Exception as e:
            db.session.rollback()
            return make_response(500, "데이터베이스 업데이트 중 오류가 발생했습니다.", {"error": str(e)})


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
