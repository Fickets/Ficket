from .rabbitmq_listener import start_rabbitmq_listener_thread
from .eureka_client_setup import initialize_eureka_client
from .config import load_config_from_server
import logging

# 전역 로깅 설정
logging.basicConfig(
    level=logging.INFO,  # 로그 레벨 (DEBUG, INFO, WARNING, ERROR, CRITICAL)
    format="%(asctime)s [%(levelname)s] %(message)s",  # 로그 형식
    handlers=[
        logging.StreamHandler(),  # 콘솔 출력
        logging.FileHandler("app.log", encoding="utf-8")  # 로그 파일 저장
    ]
)

# 전역적으로 사용할 로거 생성
logger = logging.getLogger("global_logger")

__all__ = ["load_config_from_server", "start_rabbitmq_listener_thread", "initialize_eureka_client", "logger"]
