from py_zipkin.zipkin import ZipkinAttrs
from py_zipkin.transport import SimpleHTTPTransport
import uuid
import os


def zipkin_http_transport(encoded_span):
    """Zipkin 서버로 트레이싱 데이터 전송"""
    ZIPKIN_HOST = os.getenv("ZIPKIN_HOST", "localhost")
    ZIPKIN_PORT = os.getenv("ZIPKIN_PORT", 9411)

    transport = SimpleHTTPTransport(ZIPKIN_HOST, ZIPKIN_PORT)
    transport.send(encoded_span)


def create_zipkin_attrs():
    """Zipkin 트레이스 속성 생성"""
    return ZipkinAttrs(
        trace_id=str(uuid.uuid4()).replace("-", "")[:16],
        span_id=str(uuid.uuid4()).replace("-", "")[:16],
        parent_span_id=None,
        flags="1",
        is_sampled=True,
    )
