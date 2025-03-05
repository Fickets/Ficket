import py_eureka_client.eureka_client as eureka_client
import os
import socket


def initialize_eureka_client():
    eureka_url = os.getenv("EUREKA_URL", "http://localhost:8761/eureka")
    instance_host = os.getenv("EUREKA_INSTANCE_HOST", socket.gethostname())
    instance_port = int(os.getenv("EUREKA_INSTANCE_PORT", 5000))

    eureka_client.init(
        eureka_server=eureka_url,
        app_name="face-service",
        instance_host=instance_host,
        instance_port=instance_port
    )
