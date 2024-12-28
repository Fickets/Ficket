# from flask import Flask
# from flask_sqlalchemy import SQLAlchemy
# from flask_migrate import Migrate
# from face_app.models import db
# from face_app.apis.api import api_blueprint
#
# def create_app():
#     app = Flask(__name__)
#     app.config.from_object("config.Config")
#
#     # 초기화
#     db.init_app(app)
#     Migrate(app, db)
#
#     # 블루프린트 등록
#     app.register_blueprint(views_blueprint)
#     app.register_blueprint(api_blueprint, url_prefix="/api")
#
#     return app