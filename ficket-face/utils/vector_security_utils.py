import base64
import numpy as np
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad
import os

SECRET_KEY = os.getenv("ENCRYPTION_SECRET_KEY")


def encrypt_vector(embedding):
    data = embedding.tobytes()
    cipher = AES.new(SECRET_KEY, AES.MODE_CBC)
    ct_bytes = cipher.encrypt(pad(data, AES.block_size))
    iv = base64.b64encode(cipher.iv).decode('utf-8')
    ct = base64.b64encode(ct_bytes).decode('utf-8')
    return iv + ct


def decrypt_vector(encrypted_data):
    iv = base64.b64decode(encrypted_data[:24])
    ct = base64.b64decode(encrypted_data[24:])
    cipher = AES.new(SECRET_KEY, AES.MODE_CBC, iv)
    decrypted_data = unpad(cipher.decrypt(ct), AES.block_size)
    return np.frombuffer(decrypted_data, dtype=np.float32)
