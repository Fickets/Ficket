import uuid
from config.s3_config import s3, bucket_name, kms_key_id

def upload_file_to_s3(file, folder="faces"):
    """S3에 파일 업로드 (putObject 사용)"""
    file_extension = file.filename.split('.')[-1]
    unique_filename = f"{folder}/{uuid.uuid4().hex}.{file_extension}"

    # 파일 내용을 바이트 스트림으로 읽음
    file_content = file.read()

    # put_object를 사용해 업로드
    s3.put_object(
        Bucket=bucket_name,
        Key=unique_filename,
        Body=file_content,
        ContentType=file.content_type,
        ServerSideEncryption="aws:kms",  # SSE-KMS 암호화
        SSEKMSKeyId=kms_key_id           # KMS 키 ARN
    )

    file_url = f"https://{bucket_name}.s3.amazonaws.com/{unique_filename}"
    return file_url


def delete_file_from_s3(file_url):
    """S3에서 파일 삭제"""

    # 파일 경로 추출
    file_key = file_url.split(f"https://{bucket_name}.s3.amazonaws.com/")[-1]

    # S3에서 파일 삭제
    s3.delete_object(Bucket=bucket_name, Key=file_key)

def generate_presigned_url(file_url, expiration=300):
    """Presigned URL 생성"""
    # 파일 경로 추출
    file_key = file_url.split(f"https://{bucket_name}.s3.amazonaws.com/")[-1]

    try:
        url = s3.generate_presigned_url(
            'get_object',
            Params={
                'Bucket': bucket_name,
                'Key': file_key
            },
            ExpiresIn=expiration
        )
        return url
    except Exception as e:
        raise Exception(f"Error generating presigned URL: {str(e)}")
