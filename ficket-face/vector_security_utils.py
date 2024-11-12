import base64
import numpy as np
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad
from config import load_config_from_server

# Config 서버에서 설정 로드
config = load_config_from_server()
secret_key = config["encryption"].get("secret_key")

# secret_key를 바이트로 변환
if secret_key:
    secret_key = secret_key.encode("utf-8")  # 문자열을 바이트로 변환
else:
    raise ValueError("Failed to load secret key from Config server.")

def encrypt_vector(embedding):
    # 벡터를 바이트 형태로 변환
    data = embedding.tobytes()
    cipher = AES.new(secret_key, AES.MODE_CBC)
    ct_bytes = cipher.encrypt(pad(data, AES.block_size))
    iv = base64.b64encode(cipher.iv).decode('utf-8')
    ct = base64.b64encode(ct_bytes).decode('utf-8')
    return iv + ct  # IV와 암호문을 결합하여 저장

def decrypt_vector(encrypted_data):
    iv = base64.b64decode(encrypted_data[:24])  # IV 추출
    ct = base64.b64decode(encrypted_data[24:])  # 암호문 추출
    cipher = AES.new(secret_key, AES.MODE_CBC, iv)
    decrypted_data = unpad(cipher.decrypt(ct), AES.block_size)
    return np.frombuffer(decrypted_data, dtype=np.float32)