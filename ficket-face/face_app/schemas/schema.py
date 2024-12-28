from marshmallow import Schema, fields

class FaceSchema(Schema):
    id = fields.Int(dump_only=True)
    ticket_id = fields.Int(required=True)
    event_schedule_id = fields.Int(required=True)
    face_img = fields.Str(required=True)
    vector = fields.Str(dump_only=True)
    created_at = fields.DateTime(dump_only=True)
