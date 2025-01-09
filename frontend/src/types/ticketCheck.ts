

export interface SocketMessage {
    seatLoc: string[];
    name: string;
    birth: number;
    data: FaceInfo
}

export interface FaceInfo {
    faceId: number;
    faceImg: string;
    ticketId: number;
    eventScheduleId: number;
    similarity: number;
}