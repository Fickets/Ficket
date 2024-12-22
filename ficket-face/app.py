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
from prometheus_client import Counter, Histogram, generate_latest, CONTENT_TYPE_LATEST
from py_zipkin.zipkin import zipkin_span, ZipkinAttrs
from py_zipkin.transport import SimpleHTTPTransport
import uuid

# Flask application
app = Flask(__name__)
CORS(app)

# Initial configuration
config = load_config_from_server()

# Database initialization
initialize_database(app)

# Prometheus metrics
REQUEST_COUNT = Counter("http_requests_total", "Total HTTP requests", ["method", "endpoint", "http_status"])
REQUEST_LATENCY = Histogram("http_request_latency_seconds", "Request latency in seconds", ["endpoint"])

# Zipkin transport handler
def zipkin_http_transport(encoded_span):
    """Send spans to Zipkin server"""
    transport = SimpleHTTPTransport("localhost", 9411)
    transport.send(encoded_span)

def create_zipkin_attrs():
    """Create Zipkin attributes for tracing"""
    return ZipkinAttrs(
        trace_id=str(uuid.uuid4()).replace("-", "")[:16],  # 16자리의 고유 trace ID
        span_id=str(uuid.uuid4()).replace("-", "")[:16],  # 16자리의 고유 span ID
        parent_span_id=None,          # Replace with actual parent span ID if available
        flags="1",
        is_sampled=True,
    )

# API setup
api = Api(
    app,
    title="FACE SERVICE API",
    version="v1",
    description="Face Service API documentation",
    openapi="3.0.0",
)

# Namespace setup
face_ns = api.namespace("api/v1/faces", description="Face Operations")


@app.route('/actuator/prometheus')
def prometheus_metrics():
    """Expose Prometheus metrics."""
    return generate_latest(), 200, {'Content-Type': CONTENT_TYPE_LATEST}

@app.before_request
def before_request():
    """Start timing and create Zipkin trace."""
    request.timer_context = REQUEST_LATENCY.labels(endpoint=request.path).time()
    request.timer_context.__enter__()  # Manually start the timer context
    request.zipkin_attrs = create_zipkin_attrs()

@app.after_request
def after_request(response):
    """Stop timing, record Prometheus metrics, and send Zipkin span."""
    if hasattr(request, "timer_context"):
        request.timer_context.__exit__(None, None, None)  # Manually stop the timer context

    # Increment request count
    REQUEST_COUNT.labels(
        method=request.method, endpoint=request.path, http_status=response.status_code
    ).inc()

    # Zipkin: Record the span for this request
    with zipkin_span(
        service_name="face-service",
        span_name=request.path,
        transport_handler=zipkin_http_transport,
        zipkin_attrs=request.zipkin_attrs,
    ):
        pass  # Zipkin span sent here

    return response

# API models
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

file_upload_parser = api.parser()
file_upload_parser.add_argument("file", location="files", type=FileStorage, required=True, help="Face image file")
file_upload_parser.add_argument("event_schedule_id", location="form", type=int, required=True, help="Event schedule ID")

match_parser = api.parser()
match_parser.add_argument("file", location="files", type=FileStorage, required=True, help="Face image file")
match_parser.add_argument("event_schedule_id", location="form", type=int, required=True, help="Event schedule ID")

relationship_model = api.model(
    "SetRelationship", {
        "faceId": fields.Integer(required=True, description="Face ID"),
        "faceImgUrl": fields.String(required=True, description="Face image URL"),
        "ticketId": fields.Integer(required=True, description="Ticket ID"),
        "eventScheduleId": fields.Integer(required=True, description="Event Schedule ID"),
    }
)

# API resources
@face_ns.route("/upload")
class UploadFace(Resource):
    @api.expect(file_upload_parser)
    def post(self):
        args = file_upload_parser.parse_args()
        file = request.files.get("file")
        event_schedule_id = args.get("event_schedule_id")

        if not file:
            return make_response(400, "File not provided.")

        image_data = file.read()
        embedding = get_face_embedding(image_data)
        if embedding is None:
            return make_response(400, "No face detected.")

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

        return make_response(200, "Face uploaded successfully.", {
            "faceId": new_face.face_id,
            "faceUrl": new_face.face_img
        })

@face_ns.route("/match")
class MatchFace(Resource):
    @api.expect(match_parser)
    def post(self):
        args = match_parser.parse_args()
        file = request.files.get("file")
        event_schedule_id = args.get("event_schedule_id")

        if not file or not event_schedule_id:
            return make_response(400, "File or event_schedule_id missing.")

        image_data = file.read()
        embedding = get_face_embedding(image_data)
        if embedding is None:
            return make_response(400, "No face detected.")

        faces = Face.query.filter_by(event_schedule_id=event_schedule_id).all()
        if not faces:
            return make_response(404, "No faces found for the event schedule.")

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

        threshold = 0.4
        if best_match and max_similarity > threshold:
            return make_response(200, "Face match found.", best_match)
        else:
            return make_response(404, "No matching face found.")

# Register namespace
api.add_namespace(face_ns)

if __name__ == "__main__":
    start_rabbitmq_listener_thread(config, app)
    initialize_eureka_client()
    with app.app_context():
        db.create_all()
    app.run(host="127.0.0.1", port=5000, debug=True)
