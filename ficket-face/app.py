from flask import Flask
from flask_cors import CORS
from flask_migrate import Migrate
from config import config, start_rabbitmq_listener_thread, initialize_eureka_client
from database import initialize_database, db
from utils import setup_metrics
from face_app.apis.api import api_blueprint

def create_app():
    app = Flask(__name__)
    CORS(app)

    # 데이터베이스 초기화
    initialize_database(app, config)
    Migrate(app, db)

    # 블루프린트 등록
    app.register_blueprint(api_blueprint, url_prefix="/api/v1/faces")

    # Prometheus 메트릭 설정
    setup_metrics(app)

    return app

def initialize_services(app):
    # RabbitMQ와 에루카 초기화
    start_rabbitmq_listener_thread(config, app)
    initialize_eureka_client()

app = create_app()
initialize_services(app)
