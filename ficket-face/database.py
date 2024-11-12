from flask_sqlalchemy import SQLAlchemy
from config import load_config_from_server

db = SQLAlchemy()

def initialize_database(app):
    config = load_config_from_server()
    db_url = config["mysql"].get("url")
    db_password = config["mysql"].get("password")
    
    if db_url and db_password:
        app.config['SQLALCHEMY_DATABASE_URI'] = db_url.replace("{password}", db_password)
        db.init_app(app)  # SQLAlchemy 앱 초기화
    else:
        raise RuntimeError("Database configuration is missing or incorrect")
