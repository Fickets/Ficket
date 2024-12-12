import { useState } from 'react';
import { EventDate, CalendarWithScheduleProps } from '../../types/register';

const CalendarWithSchedule = ({ onChange }: CalendarWithScheduleProps) => {
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [schedule, setSchedule] = useState<{
    [key: string]: { round: number; time: string }[];
  }>({});

  const [hour, setHour] = useState<string>('00');
  const [minute, setMinute] = useState<string>('00');
  const [currentMonth, setCurrentMonth] = useState<number>(
    new Date().getMonth() + 1
  );
  const [currentYear, setCurrentYear] = useState<number>(
    new Date().getFullYear()
  );

  const today = new Date();

  const handleDateClick = (day: string) => {
    const clickedDate = new Date(currentYear, currentMonth - 1, parseInt(day));
    if (clickedDate < today) {
      return; // 지나간 날짜는 선택하지 못하게 막기
    }
    setSelectedDate(
      `${currentYear}-${String(currentMonth).padStart(2, '0')}-${day.padStart(2, '0')}`
    );
  };

  const handleAddTime = () => {
    if (selectedDate) {
      const timeString = `${hour}:${minute}`;

      setSchedule((prev) => {
        const existingSessions = prev[selectedDate] || [];

        // 중복 시간 체크
        const isDuplicate = existingSessions.some(
          (session) => session.time === timeString
        );
        if (isDuplicate) {
          alert('같은 시간에 회차를 추가할 수 없습니다.');
          return prev; // 중복 시간일 경우 기존 상태 반환
        }

        // 새로운 회차 추가
        const updatedSessions = [
          ...existingSessions,
          { round: existingSessions.length + 1, time: timeString },
        ];

        // 시간 순서대로 정렬
        updatedSessions.sort((a, b) => {
          const [aHour, aMinute] = a.time.split(':').map(Number);
          const [bHour, bMinute] = b.time.split(':').map(Number);
          return aHour - bHour || aMinute - bMinute;
        });

        // 회차 번호 재할당
        updatedSessions.forEach((session, index) => {
          session.round = index + 1;
        });

        const updatedSchedule = {
          ...prev,
          [selectedDate]: updatedSessions,
        };

        // 부모 컴포넌트에 전달할 데이터 변환
        const eventDate: EventDate[] = Object.entries(updatedSchedule).map(
          ([date, sessions]) => ({
            date,
            sessions,
          })
        );

        onChange({ eventDate }); // 부모로 전달

        return updatedSchedule;
      });
    }
  };

  const handleDeleteTime = (time: string) => {
    if (selectedDate) {
      setSchedule((prev) => {
        const existingSessions = prev[selectedDate] || [];

        // 삭제된 시간 제외
        const updatedSessions = existingSessions.filter(
          (session) => session.time !== time
        );

        // 회차 번호 재할당
        updatedSessions.forEach((session, index) => {
          session.round = index + 1;
        });

        const updatedSchedule = {
          ...prev,
          [selectedDate]: updatedSessions,
        };

        // 부모 컴포넌트에 전달할 데이터 변환
        const eventDate: EventDate[] = Object.entries(updatedSchedule).map(
          ([date, sessions]) => ({
            date,
            sessions,
          })
        );

        onChange({ eventDate }); // 부모로 전달

        return updatedSchedule;
      });
    }
  };

  const handleMonthChange = (direction: 'prev' | 'next') => {
    if (direction === 'prev') {
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
      <h3 className="text-xl font-semibold mb-4">공연 기간 선택</h3>

      <div className="flex space-x-4 mb-4">
        {/* 달력 */}
        <div className="w-1/2 bg-gray-100 rounded p-4">
          <div className="flex justify-between items-center mb-4">
            <button
              onClick={() => handleMonthChange('prev')}
              className="bg-gray-300 text-black px-2 py-1 rounded"
            >
              이전 달
            </button>
            <h4 className="text-lg font-bold">
              {currentYear}년 {currentMonth}월
            </h4>
            <button
              onClick={() => handleMonthChange('next')}
              className="bg-gray-300 text-black px-2 py-1 rounded"
            >
              다음 달
            </button>
          </div>
          <div className="grid grid-cols-7 gap-2 text-center">
            {['일', '월', '화', '수', '목', '금', '토'].map((day) => (
              <div key={day} className="font-semibold">
                {day}
              </div>
            ))}
            {Array.from({ length: firstDayOfMonth }).map((_, index) => (
              <div key={`empty-${index}`} className="p-2"></div>
            ))}
            {[...Array(daysInMonth)].map((_, index) => {
              const day = (index + 1).toString();
              const dateString = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${day.padStart(2, '0')}`;
              const isSelected = selectedDate?.endsWith(
                `-${day.padStart(2, '0')}`
              );
              const hasSchedule = schedule[dateString]?.length > 0;
              const isPast =
                new Date(currentYear, currentMonth - 1, index + 1) < today;

              return (
                <div
                  key={day}
                  className={`p-2 rounded cursor-pointer ${
                    isPast
                      ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                      : isSelected
                        ? 'bg-red-400 text-white'
                        : hasSchedule
                          ? 'bg-yellow-300 text-black'
                          : 'bg-white hover:bg-gray-200'
                  }`}
                  onClick={() => !isPast && handleDateClick(day)}
                >
                  {day}
                </div>
              );
            })}
          </div>
        </div>

        {/* 회차 추가 */}
        <div className="w-1/2 bg-white rounded p-4 border border-gray-300">
          <div className="flex items-center mb-4">
            <select
              value={hour}
              onChange={(e) => setHour(e.target.value)}
              className="border border-gray-300 rounded px-2 py-1 mr-2"
            >
              {Array.from({ length: 24 }, (_, i) => i).map((h) => (
                <option key={h} value={String(h).padStart(2, '0')}>
                  {String(h).padStart(2, '0')}시
                </option>
              ))}
            </select>
            <select
              value={minute}
              onChange={(e) => setMinute(e.target.value)}
              className="border border-gray-300 rounded px-2 py-1 mr-2"
            >
              {['00', '30'].map((m) => (
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
                {schedule[selectedDate].map((session, index) => (
                  <li key={index} className="flex justify-between">
                    <span>
                      {session.round}회차 - {session.time}
                    </span>
                    <button
                      onClick={() => handleDeleteTime(session.time)}
                      className="text-red-500 text-sm"
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

export default CalendarWithSchedule;
