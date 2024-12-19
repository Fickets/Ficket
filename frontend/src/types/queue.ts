export enum QueueStatus {
  WAITING = "Waiting",
  IN_PROGRESS = "In Progress",
  ALMOST_DONE = "Almost Done",
  COMPLETED = "Completed",
  CANCELLED = "Cancelled",
}

export interface MyQueueStatusResponse {
  userId: string;
  eventId: string;
  myWaitingNumber: number;
  totalWaitingNumber: number;
  queueStatus: QueueStatus;
}
