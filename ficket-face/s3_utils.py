import uuid
from s3_config import s3, bucket_name

def upload_file_to_s3(file, folder="faces"):
    """S3에 파일 업로드"""
    file_extension = file.filename.split('.')[-1]
    unique_filename = f"{folder}/{uuid.uuid4().hex}.{file_extension}"
    
    s3.upload_fileobj(
        file,
        bucket_name,
        unique_filename,
        ExtraArgs={"ContentType": file.content_type}
    )
    file_url = f"https://{bucket_name}.s3.amazonaws.com/{unique_filename}"
    return file_url