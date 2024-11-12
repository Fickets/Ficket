import pika
import threading
from config import load_config_from_server

def start_rabbitmq_listener_thread(config, app):
    listener_thread = threading.Thread(target=start_rabbitmq_listener, args=(config, app))
    listener_thread.daemon = True
    listener_thread.start()

def start_rabbitmq_listener(config, app):
    # RabbitMQ 설정 로드
    RABBITMQ_HOST = config["rabbitmq"].get("host", "localhost")
    RABBITMQ_PORT = config["rabbitmq"].get("port", 5672)
    RABBITMQ_USERNAME = config["rabbitmq"].get("username", "guest")
    RABBITMQ_PASSWORD = config["rabbitmq"].get("password", "guest")
    QUEUE_NAME = 'springCloudBus'

    credentials = pika.PlainCredentials(username=RABBITMQ_USERNAME, password=RABBITMQ_PASSWORD)
    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, port=RABBITMQ_PORT, credentials=credentials))
    channel = connection.channel()

    # Exchange 및 Queue 선언
    channel.exchange_declare(exchange=QUEUE_NAME, exchange_type='topic', passive=True)
    channel.queue_declare(queue='springCloudBusByFlask')
    channel.queue_bind(exchange=QUEUE_NAME, queue='springCloudBusByFlask', routing_key='#')

    def callback(ch, method, properties, body):
        print("Received configuration update message from RabbitMQ")
        global config
        config = load_config_from_server()
        db_url = config["mysql"].get("url")
        db_password = config["mysql"].get("password")
        if db_url and db_password:
            app.config['SQLALCHEMY_DATABASE_URI'] = db_url.replace("{password}", db_password)

    channel.basic_consume(queue='springCloudBusByFlask', on_message_callback=callback, auto_ack=True)
    print("Waiting for configuration updates...")
    channel.start_consuming()
