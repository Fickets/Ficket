import React, { useEffect, useState } from 'react';
import Calendar from 'react-calendar';
import { useStore } from 'zustand';
import 'react-calendar/dist/Calendar.css';
import './CustomCalendar.css'; // 스타일 커스텀 파일 추가
import { eventDetailStore } from '../../stores/EventStore';
import { Value } from 'react-calendar/dist/esm/shared/types.js';


interface CustomCalendarProps {
    onDateSelect: (date: string) => void;  // 부모에서 전달받을 함수
}


const CustomCalendar: React.FC<CustomCalendarProps> = ({ onDateSelect }) => {

    const eventDetail = useStore(eventDetailStore);
    const [selectedDate, setSelectedDate] = useState<Date | null>(null);
    const eventDates = Object.keys(eventDetail.scheduleMap);

    // 예매 가능 날짜 설정
    const availableDates = eventDates.map((dateString) => {
        const [year, month, day] = dateString.split('-').map(Number);
        return new Date(year, month - 1, day); // month는 0부터 시작하므로 -1 필요
    });

    // 처음 값으로 날짜를 미리 클릭 해 놓음
    useEffect(() => {
        let firstEventDate = availableDates[availableDates.length - 1];
        // 상태 업데이트 전에 날짜를 조정하므로 중복된 상태 업데이트 방지
        setSelectedDate(firstEventDate);
        // 포맷팅: YYYY-MM-DD 형태로 변경
        const formattedDate = firstEventDate.toLocaleDateString("en-CA"); // ISO 형식인 'YYYY-MM-DD'를 반환
        eventDetail.setChoiceDate(formattedDate);
        onDateSelect(formattedDate);
        const scheduleData = eventDetail.scheduleMap[formattedDate][1];
        eventDetail.setScheduleId(scheduleData.eventScheduleId);
        // setEventDate(formattedDate);
    }, []);


    // 날짜 클릭 핸들러
    const handleDateClick = (value: Value | [Date, Date] | null, _event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
        // value가 배열(날짜 범위)인지 확인 후 첫 번째 날짜 선택
        const selectedValue = Array.isArray(value) ? value[0] : value;

        if (selectedValue && availableDates.some((d: Date) => d.toDateString() === selectedValue.toDateString())) {
            setSelectedDate(selectedValue);
            const formattedDate = selectedValue.toLocaleDateString("en-CA");
            onDateSelect(formattedDate);
            eventDetail.setChoiceDate(formattedDate);
            const scheduleData = eventDetail.scheduleMap[formattedDate][1];
            eventDetail.setScheduleId(scheduleData.eventScheduleId);
        }
    };

    // 날짜별 스타일 적용
    const tileClassName = ({ date, view }: { date: Date; view: string }) => {
        if (view === 'month') {
            if (selectedDate && date.toDateString() === selectedDate.toDateString()) {
                return 'selected-date'; // 선택된 날짜 스타일
            }
            if (availableDates.some(d => d.toDateString() === date.toDateString())) {
                return 'available-date'; // 예매 가능 날짜 스타일
            }
        }
        return null;
    };

    const tileDisabled = ({ date, view }: { date: Date; view: string }) => {
        if (view === 'month') {
            // `availableDates`에 포함되지 않은 날짜는 클릭 비활성화
            return !availableDates.some((d) => d.toDateString() === date.toDateString());
        }
        return false;
    };

    const initialDate = availableDates[0];

    return (
        <div>
            {/* pc */}
            <div className='hidden md:block style'>

                <Calendar
                    className='customCalendar'
                    onChange={handleDateClick}
                    value={initialDate} // 초기 날짜 설정 (예매 가능 첫 날짜)
                    // formatDay={(date) => moment(date).format("D")}
                    formatDay={(_locale, date) => {
                        const dateObj = new Date(date); // date가 string일 수 있기 때문에 Date 객체로 변환
                        return dateObj.getDate().toString(); // 숫자를 문자열로 변환하여 반환
                    }}
                    tileClassName={tileClassName}
                    tileDisabled={tileDisabled} // 클릭 비활성화 로직 추가
                    locale="ko-KR" // 한국어 설정
                    next2Label={null} // 다음 달 화살표 숨기기
                    prev2Label={null} // 이전 달 화살표 숨기기
                    showNeighboringMonth={false} // 이전/다음 달 날짜 숨기기
                />
            </div>
            <div className='block md:hidden'>
                <Calendar
                    className='customCalendar'
                    onChange={handleDateClick}
                    value={initialDate} // 초기 날짜 설정 (예매 가능 첫 날짜)
                    formatDay={(_locale, date) => {
                        const dateObj = new Date(date); // date가 string일 수 있기 때문에 Date 객체로 변환
                        return dateObj.getDate().toString(); // 숫자를 문자열로 변환하여 반환
                    }}
                    tileClassName={tileClassName}
                    tileDisabled={tileDisabled} // 클릭 비활성화 로직 추가
                    locale="ko-KR" // 한국어 설정
                    next2Label={null} // 다음 달 화살표 숨기기
                    prev2Label={null} // 이전 달 화살표 숨기기
                // showNeighboringMonth={false} // 이전/다음 달 날짜 숨기기
                />
            </div>
        </div>
    );
};

export default CustomCalendar;
