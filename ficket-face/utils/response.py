def make_response(status, message, data=None):
    """응답 생성 헬퍼 함수"""
    response_body = {"status": status, "message": message}
    if data is not None:
        response_body["data"] = data
    return response_body, status
