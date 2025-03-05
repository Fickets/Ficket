from database import db


class Face(db.Model):
    face_id = db.Column(db.BigInteger, primary_key=True, autoincrement=True, nullable=False)
    vector = db.Column(db.Text, nullable=False)  # 암호화된 문자열로 저장
    face_img = db.Column(db.String(255), nullable=False)
    ticket_id = db.Column(db.BigInteger, nullable=True)
    event_schedule_id = db.Column(db.BigInteger, nullable=False)
