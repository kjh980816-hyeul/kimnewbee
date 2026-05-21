export interface AttendanceStatus {
  checkedInToday: boolean;
  currentStreak: number;
  pointsAwardedToday: number;
  monthDates: string[];
  today: string;
}

export interface CheckInResult {
  alreadyCheckedIn: boolean;
  date: string;
  currentStreak: number;
  pointsAwarded: number;
}
