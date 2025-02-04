import requests
from Crypto.PublicKey import RSA
import base64
from hashlib import pbkdf2_hmac
from Crypto.Cipher import AES, PKCS1_v1_5
import os
from urllib.parse import urlparse
from dotenv import load_dotenv

# .env 파일 로드 (로컬 환경용)
load_dotenv()

# 배포 환경에서는 Dockerfile의 ENV가 우선 적용됨!
CONFIG_SERVER_URL = os.getenv("CONFIG_SERVER_URL", "http://localhost:8888/face-service/local")

def load_private_key(path):
    if not os.path.exists(path):
        raise FileNotFoundError(f"Private key file not found at: {path}")
    with open(path, 'r') as f:
        return RSA.importKey(f.read())


def decrypt_value(value, private_key, salt='deadbeef'):
    """
    Config 서버의 암호화 데이터를 복호화합니다.

    :param value: 암호화된 데이터 (Base64 인코딩 및 {cipher} 접두어 포함)
    :param private_key: RSA 개인 키 객체
    :param salt: AES 암호화에 사용된 salt 값 (기본: deadbeef)
    :return: 복호화된 평문
    """
    cipher_prefix = "{cipher}"
    if not value.startswith(cipher_prefix):
        return value

    value = value[len(cipher_prefix):]  # {cipher} 접두어 제거
    data = base64.b64decode(value.encode("UTF-8"))  # Base64 디코딩
    return decrypt(data, private_key, salt)


def unpad(s):
    return s[:-ord(s[len(s) - 1:])]


def decrypt(data, private_key, salt):
    """
    암호화 데이터를 RSA 및 AES로 복호화합니다.

    :param data: Base64 디코딩된 암호화 데이터
    :param private_key: RSA 개인 키 객체
    :param salt: AES 키 생성 시 사용된 salt 값
    :return: 복호화된 평문
    """
    # 길이와 랜덤 세션 키 추출
    length = int.from_bytes(data[0:2], byteorder="big")  # 길이 정보 (2 바이트)
    random = data[2:length + 2]  # 랜덤 세션 키 (256 바이트)

    # RSA로 세션 키 복호화 (Java는 RSA/ECB/PKCS1Padding 사용)
    cipher = PKCS1_v1_5.new(private_key)
    iv = cipher.decrypt(ciphertext=random, sentinel=None)
    password = iv.hex()

    # AES 키 파생 (PBKDF2WithHmacSHA1, iteration=1024, keyLength=256)
    aes_secret_key = pbkdf2_hmac('sha1', password.encode("utf-8"), bytearray.fromhex(salt), 1024, 32)

    # AES-CBC 모드 복호화
    cipher_aes = AES.new(aes_secret_key, AES.MODE_CBC, iv)
    aes_encrypted = cipher_aes.decrypt(data[2 + len(random):])

    # 패딩 제거 후 데이터 반환
    unpadded = unpad(aes_encrypted)
    encrypted_bytes = unpadded[len(iv):]  # AES에서 생성된 IV 제거
    return encrypted_bytes.decode("utf-8")


# 초기 설정 로드 함수
def load_config_from_server():
    """Config 서버에서 설정을 가져와 환경 변수에 직접 저장"""
    PRIVATE_KEY_PATH = os.path.join(os.path.dirname(__file__), "../private.pem")

    try:
        private_key = load_private_key(PRIVATE_KEY_PATH)
        if private_key is None:
            raise ValueError("Private key could not be loaded")

        response = requests.get(CONFIG_SERVER_URL)
        response.raise_for_status()
        config_data = response.json()

        property_sources = config_data.get("propertySources", [])

        for source in property_sources:
            source_data = source["source"]

            # Eureka 설정
            if "eureka.client.service-url.defaultZone" in source_data:
                os.environ["EUREKA_URL"] = source_data["eureka.client.service-url.defaultZone"]

            # MySQL 설정
            if "flask.mysql.url" in source_data:
                os.environ["MYSQL_URL"] = source_data["flask.mysql.url"]
                print(os.getenv("MYSQL_URL"))
            if "flask.mysql.password" in source_data:
                os.environ["MYSQL_PASSWORD"] = decrypt_value(source_data["flask.mysql.password"], private_key)

            # Encryption Key 설정
            if "encryption.secret_key" in source_data:
                os.environ["ENCRYPTION_SECRET_KEY"] = decrypt_value(source_data["encryption.secret_key"], private_key)

            # RabbitMQ 설정
            if "spring.rabbitmq.host" in source_data:
                os.environ["RABBITMQ_HOST"] = source_data["spring.rabbitmq.host"]
            if "spring.rabbitmq.port" in source_data:
                os.environ["RABBITMQ_PORT"] = str(source_data["spring.rabbitmq.port"])
            if "spring.rabbitmq.username" in source_data:
                os.environ["RABBITMQ_USERNAME"] = source_data["spring.rabbitmq.username"]
            if "spring.rabbitmq.password" in source_data:
                os.environ["RABBITMQ_PASSWORD"] = source_data["spring.rabbitmq.password"]

            # S3 설정
            if "s3.aws.accesskey" in source_data:
                os.environ["AWS_ACCESSKEY"] = decrypt_value(source_data["s3.aws.accesskey"], private_key)
            if "s3.aws.secretkey" in source_data:
                os.environ["AWS_SECRETKEY"] = decrypt_value(source_data["s3.aws.secretkey"], private_key)
            if "s3.aws.bucketname" in source_data:
                os.environ["AWS_BUCKETNAME"] = source_data["s3.aws.bucketname"]
            if "s3.aws.region" in source_data:
                os.environ["AWS_REGION"] = source_data["s3.aws.region"]
            if "s3.kms" in source_data:
                os.environ["AWS_KMS"] = decrypt_value(source_data["s3.kms"], private_key)

            # Zipkin 설정
            if "spring.zipkin.base-url" in source_data:
                parsed_url = urlparse(source_data["spring.zipkin.base-url"])
                os.environ["ZIPKIN_HOST"] = parsed_url.hostname
                os.environ["ZIPKIN_PORT"] = str(parsed_url.port)

    except requests.RequestException as e:
        RuntimeError("⚠️ Config Server에서 설정을 불러오지 못했습니다:", e)
