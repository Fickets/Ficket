from flask import Flask, request, jsonify
from config import load_config_from_server
from rabbitmq_listener import start_rabbitmq_listener_thread
from eureka_client_setup import initialize_eureka_client
from face_utils import get_face_embedding, cosine_similarity
from vector_security_utils import encrypt_vector, decrypt_vector
from database import db, initialize_database
from models import Face
from response import make_response
from s3_utils import upload_file_to_s3 

app = Flask(__name__)

# 초기 설정 로드
config = load_config_from_server()

# 데이터베이스 초기화
initialize_database(app)

@app.route('/api/v1/faces/test', methods=['GET'])
def test():
    return "test 성공"

# 얼굴 벡터를 DB에 저장하는 엔드포인트
@app.route('/api/v1/faces/upload', methods=['POST'])
def upload_face():
    file = request.files.get('file')
    ticket_id = request.form.get('ticket_id')
    event_date_id = request.form.get('event_date_id')

    if not file:
        return jsonify({"error": "No file part in the request"}), 400

    image_data = file.read()
    embedding = get_face_embedding(image_data)
    if embedding is None:
        return jsonify({"error": "No face detected"}), 400

    encrypted_embedding = encrypt_vector(embedding)

    # S3에 파일 업로드 및 URL 생성
    file.seek(0)  # 파일 포인터를 처음으로 리셋
    file_url = upload_file_to_s3(file)

    new_face = Face(vector=encrypted_embedding, face_img=file_url, ticket_id=ticket_id, event_date_id=event_date_id)
    db.session.add(new_face)
    db.session.commit()

    return make_response(200, "얼굴 등록에 성공했습니다.")

# 입력 얼굴과 DB 얼굴 벡터 비교 엔드포인트
@app.route('/api/v1/faces/match', methods=['POST'])
def match_face():
    file = request.files.get('file')
    event_date_id = request.form.get('event_date_id')

    if not file or not event_date_id:
        return make_response(400, "파일 또는 event_date_id가 누락되었습니다.")

    image_data = file.read()
    embedding = get_face_embedding(image_data)
    if embedding is None:
        return make_response(400, "얼굴을 감지하지 못했습니다.")

    faces = Face.query.filter_by(event_date_id=event_date_id).all()

    if not faces:
        return make_response(404, "해당 event_date_id에 대한 얼굴이 없습니다.")

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
                "event_date_id": face.event_date_id,
                "similarity": float(similarity)
            }

    threshold = 0.4
    if best_match and max_similarity > threshold:
        return make_response(200, "얼굴이 일치합니다.", best_match)
    else:
        return make_response(404, "일치하는 얼굴을 찾을 수 없습니다.")

if __name__ == '__main__':
    start_rabbitmq_listener_thread(config, app)
    initialize_eureka_client()
    app.run(host='127.0.0.1', port=5000, debug=True)
