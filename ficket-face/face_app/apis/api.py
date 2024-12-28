from flask import request, Blueprint
from flask_restx import Api, Resource, fields
from werkzeug.datastructures import FileStorage
from face_app.models.model import Face
from utils import get_face_embedding, cosine_similarity, encrypt_vector, decrypt_vector, upload_file_to_s3
from database import db
from config import config
from face_app.schemas.response import ResponseSchema

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

secret_key = config["encryption"].get("secret_key")
if secret_key:
    secret_key = secret_key.encode("utf-8")
else:
    raise ValueError("Failed to load secret key from Config server.")


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
            return ResponseSchema.make_response(400, "File not provided.")

        image_data = file.read()
        embedding = get_face_embedding(image_data)
        if embedding is None:
            return ResponseSchema.make_response(400, "No face detected.")

        encrypted_embedding = encrypt_vector(embedding, secret_key)
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
            "faceUrl": new_face.face_img
        })


@api.route("/match")
class MatchFace(Resource):
    @api.expect(match_parser)
    @api.response(200, "Face match found", response_model)
    @api.response(404, "No matching face found", response_model)
    @api.response(400, "Bad Request", response_model)
    def post(self):
        args = match_parser.parse_args()
        file = request.files.get("file")
        event_schedule_id = args.get("event_schedule_id")

        if not file or not event_schedule_id:
            return ResponseSchema.make_response(400, "File or event_schedule_id missing.")

        image_data = file.read()
        embedding = get_face_embedding(image_data)
        if embedding is None:
            return ResponseSchema.make_response(400, "No face detected.")

        faces = Face.query.filter_by(event_schedule_id=event_schedule_id).all()
        if not faces:
            return ResponseSchema.make_response(404, "No faces found for the event schedule.")

        max_similarity = -1
        best_match = None
        for face in faces:
            decrypted_embedding = decrypt_vector(face.vector, secret_key)
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

        threshold = 0.4
        if best_match and max_similarity > threshold:
            return ResponseSchema.make_response(200, "Face match found.", best_match)
        else:
            return ResponseSchema.make_response(404, "No matching face found.")




@api.route("/<int:ticket_id>")
class DeleteFace(Resource):
    @api.response(200, "Face deleted successfully", response_model)
    @api.response(404, "Face not found", response_model)
    def delete(self, ticket_id):
        face = Face.query.filter_by(ticket_id=ticket_id).first()

        if not face:
            return ResponseSchema.make_response(404, "Face not found.")

        db.session.delete(face)
        db.session.commit()

        return ResponseSchema.make_response(200, "Face deleted successfully.")


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
            return ResponseSchema.make_response(400, "모든 필드를 입력해야 합니다.")

        # 데이터베이스에서 Face 엔트리 조회
        face = Face.query.filter_by(face_id=face_id).first()
        if not face:
            return ResponseSchema.make_response(404, f"Face ID {face_id}에 해당하는 데이터가 없습니다.")
        if face.face_img != face_img_url:
            return ResponseSchema.make_response(409, f"Face Img가 request와 일치하지 않습니다.")
        if face.event_schedule_id != event_schedule_id:
            return ResponseSchema.make_response(409, f"Event Schedule Id가 request와 일치하지 않습니다.")

        face.ticket_id = ticket_id
        try:
            db.session.commit()
            return ResponseSchema.make_response(200, "관계가 성공적으로 설정되었습니다.", {
                "faceId": face.face_id,
                "faceImgUrl": face.face_img,
                "ticketId": face.ticket_id,
                "eventScheduleId": face.event_schedule_id
            })
        except Exception as e:
            db.session.rollback()
            return ResponseSchema.make_response(500, "데이터베이스 업데이트 중 오류가 발생했습니다.", {"error": str(e)})

