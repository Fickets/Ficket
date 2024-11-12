import py_eureka_client.eureka_client as eureka_client
import pika
import threading
import numpy as np
from flask import Flask
from config import load_config_from_server

app = Flask(__name__)

# 초기 설정 로드
config = load_config_from_server()

# RabbitMQ 메시지 수신 설정
def start_rabbitmq_listener():
    # RabbitMQ 설정 로드
    RABBITMQ_HOST = config["rabbitmq"].get("host", "localhost")
    RABBITMQ_PORT = config["rabbitmq"].get("port", 5672)
    RABBITMQ_USERNAME = config["rabbitmq"].get("username", "guest")
    RABBITMQ_PASSWORD = config["rabbitmq"].get("password", "guest")
    QUEUE_NAME = 'springCloudBus'

    # RabbitMQ 인증 정보 설정
    credentials = pika.PlainCredentials(
        username=RABBITMQ_USERNAME,
        password=RABBITMQ_PASSWORD
    )

    # RabbitMQ 연결 및 채널 설정
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=RABBITMQ_HOST, port=RABBITMQ_PORT, credentials=credentials)
    )
    channel = connection.channel()

    # Exchange 및 Queue 설정
    channel.exchange_declare(exchange=QUEUE_NAME, exchange_type='topic', passive=True)
    channel.queue_declare(queue='springCloudBusByFlask')
    channel.queue_bind(exchange=QUEUE_NAME, queue='springCloudBusByFlask', routing_key='#')

    # 메시지 수신 콜백 함수
    def callback(ch, method, properties, body):
        print("Received configuration update message from RabbitMQ")
        # 최신 설정 다시 로드
        global config, db_url, db_password
        config = load_config_from_server()
        db_url = config["mysql"].get("url")
        db_password = config["mysql"].get("password")
        # 데이터베이스 URI 업데이트
        if db_url and db_password:
            app.config['SQLALCHEMY_DATABASE_URI'] = db_url.replace("{password}", db_password)

    # 메시지 소비 시작
    channel.basic_consume(queue='springCloudBusByFlask', on_message_callback=callback, auto_ack=True)
    print("Waiting for configuration updates...")
    channel.start_consuming()

# Eureka Client 초기화
eureka_client.init(eureka_server="http://localhost:8761/eureka",
                   app_name="face-service",
                   instance_host="localhost",
                   instance_port=5000
                   )

@app.route('/api/v1/faces/test', methods=['GET'])
def test():
    return "test 성공"

if __name__ == '__main__':
    # Flask 앱 실행 시에만 RabbitMQ 리스너 스레드를 시작
    listener_thread = threading.Thread(target=start_rabbitmq_listener)
    listener_thread.daemon = True
    listener_thread.start()

    app.run(host='127.0.0.1', port=5000, debug=True)
