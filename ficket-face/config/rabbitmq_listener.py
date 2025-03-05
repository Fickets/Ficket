import pika
import threading
import os


def start_rabbitmq_listener_thread(app):
    listener_thread = threading.Thread(target=start_rabbitmq_listener, args=(app,))
    listener_thread.daemon = True
    listener_thread.start()


def start_rabbitmq_listener(app):
    # RabbitMQ 설정 로드
    RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
    RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", 5672))
    RABBITMQ_USERNAME = os.getenv("RABBITMQ_USERNAME", "guest")
    RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD", "guest")
    QUEUE_NAME = 'springCloudBus'

    credentials = pika.PlainCredentials(username=RABBITMQ_USERNAME, password=RABBITMQ_PASSWORD)
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=RABBITMQ_HOST, port=RABBITMQ_PORT, credentials=credentials))
    channel = connection.channel()

    # Exchange 및 Queue 선언
    channel.exchange_declare(exchange=QUEUE_NAME, exchange_type='topic', passive=True)
    channel.queue_declare(queue='springCloudBusByFlask')
    channel.queue_bind(exchange=QUEUE_NAME, queue='springCloudBusByFlask', routing_key='#')

    def callback(ch, method, properties, body):
        db_url = os.getenv("MYSQL_URL")
        db_password = os.getenv("MYSQL_PASSWORD")
        if db_url and db_password:
            app.config['SQLALCHEMY_DATABASE_URI'] = db_url.replace("{password}", db_password)

    channel.basic_consume(queue='springCloudBusByFlask', on_message_callback=callback, auto_ack=True)
    channel.start_consuming()
