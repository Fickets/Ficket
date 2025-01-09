export interface SocketMessage {
  seatLoc: string[];
  name: string;
  birth: number;
  data: FaceInfo;
}

export interface FaceInfo {
  face_id: number;
  face_img: string;
  ticket_id: number;
  eventScheduleId: number;
  similarity: number;
}
