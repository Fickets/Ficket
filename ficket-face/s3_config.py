import boto3
from config import load_config_from_server

config = load_config_from_server()

s3 = boto3.client(
    "s3",
    aws_access_key_id=config["aws"]["accesskey"],
    aws_secret_access_key=config["aws"]["secretkey"],
    region_name=config["aws"]["region"]
)

bucket_name = config["aws"]["bucketname"]