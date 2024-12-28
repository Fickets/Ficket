from database import db
#
# class Face(db.Model):
#     __tablename__ = "face_app"
#     face_id = db.Column(db.BigInteger, primary_key=True, autoincrement=True, nullable=False)
#     vector = db.Column(db.Text, nullable=False)  # 암호화된 문자열로 저장
#     face_img = db.Column(db.String(255), nullable=False)
#     ticket_id = db.Column(db.BigInteger, nullable=True)
#     event_schedule_id = db.Column(db.BigInteger, nullable=False)
#
#     def __init__(self, vector, face_img, ticket_id, event_schedule_id):
#         self.vector = vector
#         self.face_img = face_img
#         self.ticket_id = ticket_id
#         self.event_schedule_id = event_schedule_id

class Face(db.Model):
    face_id = db.Column(db.BigInteger, primary_key=True, autoincrement=True, nullable=False)
    vector = db.Column(db.Text, nullable=False)  # 암호화된 문자열로 저장
    face_img = db.Column(db.String(255), nullable=False)
    ticket_id = db.Column(db.BigInteger, nullable=True)
    event_schedule_id = db.Column(db.BigInteger, nullable=False)
