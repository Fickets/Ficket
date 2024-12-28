import uuid
from config.s3_config import s3, bucket_name


def upload_file_to_s3(file, folder="faces"):
    """S3에 파일 업로드"""
    file_extension = file.filename.split('.')[-1]
    unique_filename = f"{folder}/{uuid.uuid4().hex}.{file_extension}"

    # SSE-C 설정이 필요할 경우 아래 주석 부분을 활성화하세요
    # 현재는 HTTPS 설정이 되어 있지 않아 테스트가 불가능합니다.
    # extra_args = {
    #     "ContentType": file.content_type,
    #     "SSECustomerAlgorithm": "AES256",
    #     "SSECustomerKey": "<your-base64-encoded-key>",
    #     "SSECustomerKeyMD5": "<your-base64-encoded-md5>"
    # }

    s3.upload_fileobj(
        file,
        bucket_name,
        unique_filename,
        ExtraArgs={"ContentType": file.content_type}
    )
    file_url = f"https://{bucket_name}.s3.amazonaws.com/{unique_filename}"
    return file_url


def delete_file_from_s3(file_url):
    """S3에서 파일 삭제"""

    # 파일 경로 추출
    file_key = file_url.split(f"https://{bucket_name}.s3.amazonaws.com/")[-1]

    # S3에서 파일 삭제
    s3.delete_object(Bucket=bucket_name, Key=file_key)
