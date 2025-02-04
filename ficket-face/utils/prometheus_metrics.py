from prometheus_client import Counter, Histogram, generate_latest, CONTENT_TYPE_LATEST
from py_zipkin.zipkin import zipkin_span
from .tracing import zipkin_http_transport, create_zipkin_attrs
from flask import request

# HTTP 요청 카운트 메트릭
REQUEST_COUNT = Counter(
    "http_requests_total", "Total HTTP requests", ["method", "endpoint", "http_status"]
)

# HTTP 요청 대기 시간 메트릭
REQUEST_LATENCY = Histogram(
    "http_request_latency_seconds", "Request latency in seconds", ["endpoint"]
)


def setup_metrics(app):
    """Flask 앱에 Prometheus 엔드포인트 추가"""

    @app.route('/actuator/prometheus')
    def prometheus_metrics():
        """Prometheus 메트릭 노출"""
        return generate_latest(), 200, {'Content-Type': CONTENT_TYPE_LATEST}

    @app.before_request
    def before_request():
        """요청 처리 전에 Prometheus 메트릭 설정"""
        request.timer_context = REQUEST_LATENCY.labels(endpoint=request.path).time()
        request.timer_context.__enter__()
        request.zipkin_attrs = create_zipkin_attrs()

    @app.after_request
    def after_request(response):
        """요청 처리 후 메트릭 업데이트"""
        """Stop timing, record Prometheus metrics, and send Zipkin span."""
        if hasattr(request, "timer_context"):
            request.timer_context.__exit__(None, None, None)

        REQUEST_COUNT.labels(
            method=request.method, endpoint=request.path, http_status=response.status_code
        ).inc()

        with zipkin_span(
                service_name="face_app-service",
                span_name=request.path,
                transport_handler=zipkin_http_transport,
                zipkin_attrs=request.zipkin_attrs,
        ):
            pass

        return response
