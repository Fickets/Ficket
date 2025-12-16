import py_eureka_client.eureka_client as eureka_client
import os
import socket
import uuid

# local
def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        return s.getsockname()[0]
    finally:
        s.close()


def initialize_eureka_client():
    eureka_url = os.getenv("EUREKA_URL", "http://localhost:8761/eureka")
    instance_host = os.getenv("EUREKA_INSTANCE_HOST", get_local_ip())
    instance_port = int(os.getenv("EUREKA_INSTANCE_PORT", 5000))
    instance_id = os.getenv(
        "EUREKA_INSTANCE_ID",
        f"face-service:{uuid.uuid4()}"
    )

    eureka_client.init(
        eureka_server=eureka_url,
        app_name="face-service",
        instance_id=instance_id,
        instance_host=instance_host,
        instance_port=instance_port
    )

# prod

# def initialize_eureka_client():
#     eureka_url = os.getenv("EUREKA_URL", "http://localhost:8761/eureka")
#     instance_host = os.getenv("EUREKA_INSTANCE_HOST", socket.gethostname())
#     instance_port = int(os.getenv("EUREKA_INSTANCE_PORT", 5000))
#
#     eureka_client.init(
#         eureka_server=eureka_url,
#         app_name="face-service",
#         instance_host=instance_host,
#         instance_port=instance_port
#     )

