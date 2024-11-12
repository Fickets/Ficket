import py_eureka_client.eureka_client as eureka_client

def initialize_eureka_client():
    eureka_client.init(
        eureka_server="http://localhost:8761/eureka",
        app_name="face-service",
        instance_host="localhost",
        instance_port=5000
    )
