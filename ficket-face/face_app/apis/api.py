from flask import request, Blueprint
from flask_restx import Api, Resource, fields
from werkzeug.datastructures import FileStorage
from face_app.models.model import Face
from utils import get_face_embedding, cosine_similarity, encrypt_vector, decrypt_vector, upload_file_to_s3, \
    delete_file_from_s3, generate_presigned_url
from database import db
from config import logger
from face_app.schemas.response import ResponseSchema
from apscheduler.schedulers.background import BackgroundScheduler
from urllib.parse import urlparse

# Blueprint 생성
api_blueprint = Blueprint("api", __name__)
api = Api(api_blueprint, title="Face Service API", version="v1", description="Face Service API documentation")

# API 모델 정의
upload_model = api.model(
    "UploadFace", {
        "ticket_id": fields.Integer(required=True, description="Ticket ID"),
        "event_schedule_id": fields.Integer(required=True, description="Event Schedule ID"),
    }
)

match_model = api.model(
    "MatchFace", {
        "event_schedule_id": fields.Integer(required=True, description="Event Schedule ID"),
    }
)

response_model = api.model(
    "Response", {
        "status": fields.Integer(required=True, description="HTTP status code"),
        "message": fields.String(required=True, description="Response message"),
        "data": fields.Raw(required=False, description="Optional data")
    }
)

relationship_model = api.model(
    "SetRelationship", {
        "faceId": fields.Integer(required=True, description="Face ID"),
        "faceImgUrl": fields.String(required=True, description="Face image URL"),
        "ticketId": fields.Integer(required=True, description="Ticket ID"),
        "eventScheduleId": fields.Integer(required=True, description="Event Schedule ID"),
    }
)

file_upload_parser = api.parser()
file_upload_parser.add_argument("file", location="files", type=FileStorage, required=True, help="Face image file")
file_upload_parser.add_argument("event_schedule_id", location="form", type=int, required=True, help="Event schedule ID")

match_parser = api.parser()
match_parser.add_argument("file", location="files", type=FileStorage, required=True, help="Face image file")
match_parser.add_argument("event_schedule_id", location="form", type=int, required=True, help="Event schedule ID")


# 매일 자정에 ticket_id가 NULL인 Face 삭제
def delete_null_ticket_faces():
    try:
        faces_to_delete = Face.query.filter(Face.ticket_id == None).all()
        if faces_to_delete:
            for face in faces_to_delete:
                delete_file_from_s3(face.face_img)
                db.session.delete(face)
            db.session.commit()
            logger.info(f"{len(faces_to_delete)} face(s) with NULL ticket_id deleted successfully.")
        else:
            logger.warning("No faces with NULL ticket_id found.")
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error occurred while deleting faces with NULL ticket_id: {e}")


scheduler = BackgroundScheduler()
scheduler.add_job(delete_null_ticket_faces, 'cron', hour=0, minute=0)
scheduler.start()


# API 리소스 정의
@api.route("/upload")
class UploadFace(Resource):
    @api.expect(file_upload_parser)
    @api.response(200, "Face uploaded successfully", response_model)
    @api.response(400, "Bad Request", response_model)
    def post(self):
        args = file_upload_parser.parse_args()
        file = request.files.get("file")
        event_schedule_id = args.get("event_schedule_id")

        if not file:
            return ResponseSchema.make_response(400, "File not provided."), 400

        image_data = file.read()
        embedding = get_face_embedding(image_data)
        if embedding is None:
            return ResponseSchema.make_response(400, "No face detected."), 400

        encrypted_embedding = encrypt_vector(embedding)
        file.seek(0)
        file_url = upload_file_to_s3(file)

        new_face = Face(
            vector=encrypted_embedding,
            face_img=file_url,
            ticket_id=None,
            event_schedule_id=event_schedule_id
        )
        db.session.add(new_face)
        db.session.commit()

        return ResponseSchema.make_response(200, "Face uploaded successfully.", {
            "faceId": new_face.face_id,
            "faceUrl": generate_presigned_url(new_face.face_img, 600)
        }), 200


@api.route("/match")
class MatchFace(Resource):
    @api.expect(match_parser)
    @api.response(200, "Face match found", response_model)
    @api.response(204, "No matching face found", response_model)
    @api.response(400, "Bad Request", response_model)
    def post(self):
        args = match_parser.parse_args()
        file = request.files.get("file")
        event_schedule_id = args.get("event_schedule_id")

        if not file or not event_schedule_id:
            return ResponseSchema.make_response(400, "File or event_schedule_id missing."), 400

        image_data = file.read()
        embedding = get_face_embedding(image_data)
        if embedding is None:
            return ResponseSchema.make_response(400, "No face detected."), 400

        faces = Face.query.filter_by(event_schedule_id=event_schedule_id).all()
        if not faces:
            return ResponseSchema.make_response(404, "No faces found for the event schedule."), 404

        max_similarity = -1
        best_match = None

        for face in faces:
            decrypted_embedding = decrypt_vector(face.vector)
            similarity = cosine_similarity(embedding, decrypted_embedding)
            if similarity > max_similarity:
                max_similarity = similarity
                best_match = {
                    "face_id": face.face_id,
                    "face_img": generate_presigned_url(face.face_img),
                    "ticket_id": face.ticket_id,
                    "event_schedule_id": face.event_schedule_id,
                    "similarity": float(similarity),
                }

        threshold = 0.4
        if best_match and max_similarity > threshold:
            return ResponseSchema.make_response(200, "Face match found.", best_match), 200
        else:
            return ResponseSchema.make_response(206, "No matching face found."), 206


@api.route("/<int:ticket_id>")
class DeleteFace(Resource):
    @api.response(200, "Face deleted successfully", response_model)
    @api.response(404, "Face not found", response_model)
    def delete(self, ticket_id):
        face = Face.query.filter_by(ticket_id=ticket_id).first()

        if not face:
            return ResponseSchema.make_response(404, "Face not found."), 404

        delete_file_from_s3(face.face_img)
        db.session.delete(face)
        db.session.commit()

        return ResponseSchema.make_response(200, "Face deleted successfully."), 200


@api.route("/set-relationship")
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
            return ResponseSchema.make_response(400, "모든 필드를 입력해야 합니다."), 400

        # 데이터베이스에서 Face 엔트리 조회
        face = Face.query.filter_by(face_id=face_id).first()

        # URL 파싱
        parsed_url = urlparse(face_img_url)
        # 기본 URL 추출
        parsed_face_url = parsed_url.scheme + "://" + parsed_url.netloc + parsed_url.path

        if not face:
            return ResponseSchema.make_response(404, f"Face ID {face_id}에 해당하는 데이터가 없습니다."), 404
        if face.face_img != parsed_face_url:
            return ResponseSchema.make_response(409, f"Face Img가 request와 일치하지 않습니다."), 409
        if face.event_schedule_id != event_schedule_id:
            return ResponseSchema.make_response(409, f"Event Schedule Id가 request와 일치하지 않습니다."), 409

        face.ticket_id = ticket_id
        try:
            db.session.commit()
            return ResponseSchema.make_response(200, "관계가 성공적으로 설정되었습니다.", {
                "faceId": face.face_id,
                "faceImgUrl": face.face_img,
                "ticketId": face.ticket_id,
                "eventScheduleId": face.event_schedule_id
            }), 200
        except Exception as e:
            db.session.rollback()
            return ResponseSchema.make_response(500, "데이터베이스 업데이트 중 오류가 발생했습니다.", {"error": str(e)}), 500


@api.route("/presigned-url/<int:face_id>")
class GetFileFromS3(Resource):
    @api.response(200, "Presigned URL 생성 성공", response_model)
    @api.response(404, "Face를 찾을 수 없음", response_model)
    @api.response(500, "Presigned URL 생성 중 오류 발생", response_model)
    def get(self, face_id):
        """Face ID를 사용하여 S3 객체에 대한 서명된 URL 생성"""
        try:
            # 데이터베이스에서 Face 엔트리 조회
            face = Face.query.filter_by(face_id=face_id).first()

            if not face:
                return ResponseSchema.make_response(404, "Face를 찾을 수 없습니다."), 404

            # Presigned URL 생성
            try:
                presigned_url = generate_presigned_url(face.face_img)
                return ResponseSchema.make_response(200, "Presigned URL 생성 성공", {
                    "faceId": face.face_id,
                    "presignedUrl": presigned_url,
                }), 200
            except Exception as e:
                return ResponseSchema.make_response(500, "Presigned URL 생성 중 오류 발생", {"error": str(e)}), 500

        except Exception as e:
            return ResponseSchema.make_response(500, "처리 중 오류 발생", {"error": str(e)}), 500


@api.route("/delete-face/<int:face_id>")
class DeleteImageByFaceId(Resource):
    @api.response(200, "Face 삭제 성공", response_model)
    @api.response(404, "Face를 찾을 수 없음", response_model)
    @api.response(500, "Face 삭제 중 오류 발생", response_model)
    def delete(self, face_id):
        """Face ID로 S3에서 파일 삭제"""
        try:
            # 데이터베이스에서 Face 엔트리 조회
            face = Face.query.filter_by(face_id=face_id).first()

            if not face:
                return ResponseSchema.make_response(404, "Face를 찾을 수 없습니다."), 404

            # S3에서 파일 삭제
            try:
                delete_file_from_s3(face.face_img)

                # 데이터베이스에서 Face 삭제
                db.session.delete(face)
                db.session.commit()

                return ResponseSchema.make_response(200, "Face 삭제 성공"), 200
            except Exception as e:
                return ResponseSchema.make_response(500, "S3에서 파일 삭제 중 오류 발생", {"error": str(e)}), 500

        except Exception as e:
            return ResponseSchema.make_response(500, "처리 중 오류 발생", {"error": str(e)}), 500
