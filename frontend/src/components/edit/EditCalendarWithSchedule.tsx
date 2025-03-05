import { useState } from "react";
import { EventDate, CalendarWithScheduleProps } from "../../types/edit";

const EditCalendarWithSchedule = ({
  initialData = [], // 기본값 추가
  onChange,
}: CalendarWithScheduleProps) => {
  // 선택된 날짜 상태
  const [selectedDate, setSelectedDate] = useState<string | null>(null);

  // 회차 스케줄 상태
  const [schedule, setSchedule] = useState<{
    [key: string]: { round: number; time: string }[];
  }>(() => {
    // 초기 데이터 설정 시 방어 코드 추가
    if (!Array.isArray(initialData)) return {};
    return initialData.reduce(
      (acc, event) => {
        acc[event.date] = event.sessions;
        return acc;
      },
      {} as { [key: string]: { round: number; time: string }[] },
    );
  });

  // 시간 및 분 상태
  const [hour, setHour] = useState<string>("00");
  const [minute, setMinute] = useState<string>("00");

  // 달력 상태
  const [currentMonth, setCurrentMonth] = useState<number>(
    new Date().getMonth() + 1,
  );
  const [currentYear, setCurrentYear] = useState<number>(
    new Date().getFullYear(),
  );

  const today = new Date();

  // 날짜 클릭 처리
  const handleDateClick = (day: string) => {
    const clickedDate = new Date(currentYear, currentMonth - 1, parseInt(day));
    if (clickedDate < today) {
      return; // 지나간 날짜는 선택할 수 없음
    }

    setSelectedDate(
      `${currentYear}-${String(currentMonth).padStart(2, "0")}-${day.padStart(2, "0")}`,
    );
  };

  // 회차 추가 처리
  const handleAddTime = () => {
    if (selectedDate) {
      const timeString = `${hour}:${minute}`;

      setSchedule((prev) => {
        const existingSessions = prev[selectedDate] || [];

        // 중복 시간 체크
        const isDuplicate = existingSessions.some(
          (session) => session.time === timeString,
        );
        if (isDuplicate) {
          alert("같은 시간에 회차를 추가할 수 없습니다.");
          return prev;
        }

        const updatedSessions = [
          ...existingSessions,
          { round: 0, time: timeString },
        ]
          .sort((a, b) => (a.time > b.time ? 1 : -1))
          .map((session, index) => ({ ...session, round: index + 1 }));

        const updatedSchedule = { ...prev };
        if (updatedSessions.length === 0) {
          delete updatedSchedule[selectedDate]; // 빈 배열이면 키 삭제
        } else {
          updatedSchedule[selectedDate] = updatedSessions; // 업데이트된 세션 할당
        }

        // 부모로 전달할 데이터 변환
        const eventDate: EventDate[] = Object.entries(updatedSchedule).map(
          ([date, sessions]) => ({
            date,
            sessions,
          }),
        );

        onChange({ eventDate }); // 부모로 전달

        return updatedSchedule;
      });
    }
  };

  // 회차 삭제 처리
  const handleRemoveTime = (date: string, round: number) => {
    setSchedule((prev) => {
      const updatedSessions = prev[date]
        .filter((session) => session.round !== round)
        .map((session, index) => ({ ...session, round: index + 1 }));

      const updatedSchedule = {
        ...prev,
        [date]: updatedSessions,
      };

      if (updatedSessions.length === 0) {
        delete updatedSchedule[date];
      }

      const eventDate: EventDate[] = Object.entries(updatedSchedule).map(
        ([date, sessions]) => ({
          date,
          sessions,
        }),
      );

      onChange({ eventDate });

      return updatedSchedule;
    });
  };

  // 월 변경 처리
  const handleMonthChange = (direction: "prev" | "next") => {
    if (direction === "prev") {
      if (currentMonth === 1) {
        setCurrentMonth(12);
        setCurrentYear((prev) => prev - 1);
      } else {
        setCurrentMonth((prev) => prev - 1);
      }
    } else {
      if (currentMonth === 12) {
        setCurrentMonth(1);
        setCurrentYear((prev) => prev + 1);
      } else {
        setCurrentMonth((prev) => prev + 1);
      }
    }
  };

  const daysInMonth = new Date(currentYear, currentMonth, 0).getDate();
  const firstDayOfMonth = new Date(currentYear, currentMonth - 1, 1).getDay();

  return (
    <div className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
      <h3 className="text-xl font-semibold mb-4">공연 기간 및 회차 선택</h3>

      <div className="flex gap-4">
        {/* 달력 */}
        <div className="w-[50%] bg-gray-100 rounded p-4">
          <div className="flex justify-between items-center mb-4">
            <button
              onClick={() => handleMonthChange("prev")}
              className="bg-gray-300 text-black px-2 py-1 rounded"
            >
              이전 달
            </button>
            <h4 className="text-lg font-bold">
              {currentYear}년 {currentMonth}월
            </h4>
            <button
              onClick={() => handleMonthChange("next")}
              className="bg-gray-300 text-black px-2 py-1 rounded"
            >
              다음 달
            </button>
          </div>
          <div className="grid grid-cols-7 gap-2 text-center">
            {["일", "월", "화", "수", "목", "금", "토"].map((day) => (
              <div key={day} className="font-semibold">
                {day}
              </div>
            ))}
            {Array.from({ length: firstDayOfMonth }).map((_, index) => (
              <div key={`empty-${index}`} className="p-2"></div>
            ))}
            {[...Array(daysInMonth)].map((_, index) => {
              const day = (index + 1).toString();
              const dateString = `${currentYear}-${String(currentMonth).padStart(2, "0")}-${day.padStart(2, "0")}`;
              const isSelected = selectedDate?.endsWith(
                `-${day.padStart(2, "0")}`,
              );
              const hasSchedule = schedule[dateString]?.length > 0;
              const isPast =
                new Date(currentYear, currentMonth - 1, index + 1) < today;

              return (
                <div
                  key={day}
                  className={`p-2 rounded cursor-pointer ${
                    isPast
                      ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                      : isSelected
                        ? "bg-red-400 text-white"
                        : hasSchedule
                          ? "bg-yellow-300 text-black"
                          : "bg-white hover:bg-gray-200"
                  }`}
                  onClick={() => !isPast && handleDateClick(day)}
                >
                  {day}
                </div>
              );
            })}
          </div>
        </div>

        {/* 회차 추가 및 삭제 */}
        <div className="w-[50%] bg-white rounded p-4 border border-gray-300">
          <div className="flex items-center mb-4">
            <select
              value={hour}
              onChange={(e) => setHour(e.target.value)}
              className="border border-gray-300 rounded px-2 py-1 mr-2"
            >
              {Array.from({ length: 24 }, (_, i) => i).map((h) => (
                <option key={h} value={String(h).padStart(2, "0")}>
                  {String(h).padStart(2, "0")}시
                </option>
              ))}
            </select>
            <select
              value={minute}
              onChange={(e) => setMinute(e.target.value)}
              className="border border-gray-300 rounded px-2 py-1 mr-2"
            >
              {["00", "30"].map((m) => (
                <option key={m} value={m}>
                  {m}분
                </option>
              ))}
            </select>
            <button
              onClick={handleAddTime}
              className="bg-blue-500 text-white px-4 py-2 rounded-md"
            >
              회차 추가
            </button>
          </div>

          <div className="border-t border-gray-300 pt-4">
            <h4 className="text-lg font-semibold mb-2">추가된 회차</h4>
            {selectedDate && schedule[selectedDate]?.length > 0 ? (
              <ul className="list-disc pl-5 space-y-1">
                {schedule[selectedDate].map((session) => (
                  <li key={session.round} className="flex justify-between">
                    <span>
                      {session.round}회차 - {session.time}
                    </span>
                    <button
                      onClick={() =>
                        handleRemoveTime(selectedDate, session.round)
                      }
                      className="text-red-500 text-sm ml-4"
                    >
                      삭제
                    </button>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-gray-500">회차가 없습니다.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default EditCalendarWithSchedule;
