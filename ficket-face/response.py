from flask import jsonify

def make_response(status=200, message="", data=None):
    """공통 JSON 응답 형식"""
    return jsonify({
        "status": status,
        "message": message,
        "data": data
    }), status