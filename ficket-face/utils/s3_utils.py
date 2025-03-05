import boto3
import os
import uuid

def get_s3_client():
    """환경 변수에서 AWS 자격 증명을 동적으로 가져와 S3 클라이언트 생성"""
    AWS_ACCESSKEY = os.getenv("AWS_ACCESSKEY")
    AWS_SECRETKEY = os.getenv("AWS_SECRETKEY")
    AWS_REGION = os.getenv("AWS_REGION")

    if not AWS_ACCESSKEY or not AWS_SECRETKEY or not AWS_REGION:
        raise ValueError("AWS credentials are missing! Please check environment variables.")

    return boto3.client(
        "s3",
        aws_access_key_id=AWS_ACCESSKEY,
        aws_secret_access_key=AWS_SECRETKEY,
        region_name=AWS_REGION
    )


def upload_file_to_s3(file, folder="faces"):
    """S3에 파일 업로드 (putObject 사용)"""

    s3 = get_s3_client()

    BUCKET_NAME = os.getenv("AWS_BUCKETNAME")
    KMS_KEY_ID = os.getenv("AWS_KMS")

    file_extension = file.filename.split('.')[-1]
    unique_filename = f"{folder}/{uuid.uuid4().hex}.{file_extension}"

    # 파일 내용을 바이트 스트림으로 읽음
    file_content = file.read()

    # put_object를 사용해 업로드
    s3.put_object(
        Bucket=BUCKET_NAME,
        Key=unique_filename,
        Body=file_content,
        ContentType=file.content_type,
        ServerSideEncryption="aws:kms",  # SSE-KMS 암호화
        SSEKMSKeyId=KMS_KEY_ID  # KMS 키 ARN
    )

    file_url = f"https://{BUCKET_NAME}.s3.amazonaws.com/{unique_filename}"
    return file_url


def delete_file_from_s3(file_url):
    """S3에서 파일 삭제"""
    s3 = get_s3_client()

    BUCKET_NAME = os.getenv("AWS_BUCKETNAME")

    # 파일 경로 추출
    file_key = file_url.split(f"https://{BUCKET_NAME}.s3.amazonaws.com/")[-1]

    # S3에서 파일 삭제
    s3.delete_object(Bucket=BUCKET_NAME, Key=file_key)


def generate_presigned_url(file_url, expiration=300):
    """Presigned URL 생성"""
    s3 = get_s3_client()

    BUCKET_NAME = os.getenv("AWS_BUCKETNAME")

    # 파일 경로 추출
    file_key = file_url.split(f"https://{BUCKET_NAME}.s3.amazonaws.com/")[-1]

    try:
        url = s3.generate_presigned_url(
            'get_object',
            Params={
                'Bucket': BUCKET_NAME,
                'Key': file_key
            },
            ExpiresIn=expiration
        )
        return url
    except Exception as e:
        raise Exception(f"Error generating presigned URL: {str(e)}")
