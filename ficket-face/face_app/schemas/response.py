from marshmallow import Schema, fields
from flask import jsonify

class ResponseSchema(Schema):
    status = fields.Int(required=True, description="HTTP status code")
    message = fields.Str(required=True, description="Response message")
    data = fields.Dict(required=False, description="Optional data")


    @staticmethod
    def make_response(status, message, data=None):
        """
        통일된 응답 생성 함수.

        Args:
            status (int): HTTP 상태 코드
            message (str): 응답 메시지
            data (dict, optional): 응답 데이터

        Returns:
            Response: Flask JSON 응답 객체
        """
        schema = ResponseSchema()
        response = {
            "status": status,
            "message": message,
            "data": data,
        }
        return schema.dump(response)
