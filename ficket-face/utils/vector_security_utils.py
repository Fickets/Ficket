import base64
import numpy as np
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad
import os


def get_secret_key():
    """환경 변수에서 AES 키를 가져오고 검증"""
    secret_key_env = os.getenv("ENCRYPTION_SECRET_KEY")

    if secret_key_env is None:
        raise ValueError("ENCRYPTION_SECRET_KEY is not set. Please check environment variables.")

    secret_key = secret_key_env.encode()  # 바이트 변환

    if len(secret_key) not in [16, 24, 32]:
        raise ValueError("Invalid AES key length! Must be 16, 24, or 32 bytes.")

    return secret_key


def encrypt_vector(embedding):
    secret_key = get_secret_key()
    data = embedding.tobytes()
    cipher = AES.new(secret_key, AES.MODE_CBC)
    ct_bytes = cipher.encrypt(pad(data, AES.block_size))
    iv = base64.b64encode(cipher.iv).decode('utf-8')
    ct = base64.b64encode(ct_bytes).decode('utf-8')
    return iv + ct


def decrypt_vector(encrypted_data):
    secret_key = get_secret_key()
    iv = base64.b64decode(encrypted_data[:24])
    ct = base64.b64decode(encrypted_data[24:])
    cipher = AES.new(secret_key, AES.MODE_CBC, iv)
    decrypted_data = unpad(cipher.decrypt(ct), AES.block_size)
    return np.frombuffer(decrypted_data, dtype=np.float32)
