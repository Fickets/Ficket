from flask import Flask
from flask_cors import CORS
from flask_migrate import Migrate
from config import load_config_from_server, start_rabbitmq_listener_thread, initialize_eureka_client
from database import initialize_database, db
from utils import setup_metrics, delete_file_from_s3
from face_app.apis.api import api_blueprint
from utils import get_face_analyzer
from config import logger
from apscheduler.schedulers.background import BackgroundScheduler
from face_app.models.model import Face

def create_app():
    app = Flask(__name__)
    CORS(app)

    # 환경 변수 설정
    load_config_from_server()

    # 데이터베이스 초기화
    initialize_database(app)
    Migrate(app, db)

    # 블루프린트 등록
    app.register_blueprint(api_blueprint, url_prefix="/api/v1/faces")

    # Prometheus 메트릭 설정
    setup_metrics(app)

    return app

def delete_faces_without_ticket():
    """ticket_id가 NULL인 Face 데이터 삭제"""
    with app.app_context():
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


def initialize_services(app):
    # RabbitMQ와 에루카 초기화
    start_rabbitmq_listener_thread(app)
    initialize_eureka_client()

    # FaceAnalysis Lazy Loading 적용
    get_face_analyzer()  # 앱 시작 시 한 번 로드하여 대기

    # APScheduler 설정 (매일 0시 실행)
    scheduler = BackgroundScheduler()
    scheduler.add_job(delete_faces_without_ticket, 'cron', hour=0, minute=0)
    scheduler.start()


app = create_app()
initialize_services(app)
