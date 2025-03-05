from flask_sqlalchemy import SQLAlchemy
import os
from config import logger

db = SQLAlchemy()


def initialize_database(app):
    if app:
        DB_URL = os.getenv("MYSQL_URL")
        DB_PASSWORD = os.getenv("MYSQL_PASSWORD")

        if DB_URL and DB_PASSWORD:
            app.config['SQLALCHEMY_DATABASE_URI'] = DB_URL.replace("{password}", DB_PASSWORD)
            db.init_app(app)  # SQLAlchemy 앱 초기화
            logger.info("✅ 데이터베이스가 성공적으로 초기화되었습니다.")
        else:
            logger.error("❌ 데이터베이스 설정이 누락되었거나 잘못되었습니다.")
            raise RuntimeError("Database configuration is missing or incorrect")
