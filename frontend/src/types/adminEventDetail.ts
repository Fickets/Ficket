export interface DailyRevenueResponse {
  date: Date;
  revenue: number;
}

export interface DayCountMap {
  dayCountMap: {
    Monday: number;
    Tuesday: number;
    Wednesday: number;
    Thursday: number;
    Friday: number;
    Saturday: number;
    Sunday: number;
  };
}
