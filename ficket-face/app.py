from flask import Flask
from flask_cors import CORS
from flask_migrate import Migrate
from config import config, start_rabbitmq_listener_thread, initialize_eureka_client
from database import initialize_database, db
from utils import setup_metrics
from face_app.apis.api import api_blueprint

app = Flask(__name__)
CORS(app)

# Database Initialization
initialize_database(app, config)

migrate = Migrate(app, db)

# Register Blueprints
app.register_blueprint(api_blueprint, url_prefix="/api/v1/faces")

# Prometheus metrics
setup_metrics(app)

if __name__ == "__main__":
    start_rabbitmq_listener_thread(config, app)
    initialize_eureka_client()
    app.run(host="127.0.0.1", port=5000, debug=True)
