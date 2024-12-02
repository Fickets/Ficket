import React, { useEffect, useState } from 'react';
import {
    StyledCalendarWrapper,
    StyledCalendar,
    StyledDate,
    StyledToday,
    StyledDot,
} from "./Calender"; // 기본 스타일링

import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router';
import { useStore } from 'zustand';
import moment from "moment";

const SelectDateComponent: React.FC = () => {
    type ValuePiece = Date | null;
    type Value = ValuePiece | [ValuePiece, ValuePiece];
    const today = new Date();
    const [date, setDate] = useState<Value>(today);
    const [activeStartDate, setActiveStartDate] = useState<Date | null>(
        new Date()
    );
    const attendDay = ["2023-12-03", "2023-12-13"]; // 출석한 날짜 예시

    const handleDateChange = (newDate: Value) => {
        setDate(newDate);
    };

    const handleTodayClick = () => {
        const today = new Date();
        setActiveStartDate(today);
        setDate(today);
    };
    return (
        <div className="w-[600px] h-[450px] min-w-[600px] min-h-[450px] p-[15px]"

        >
            {/* 상단 3개 컴포넌트 - 가로로 배치 */}
            <div className="flex ">
                {/* 관람일 선택 */}
                <div className="w-[250px]  bg-white  shadow-md">
                    <h3 className="text-center font-semibold">관람일 선택</h3>
                    <StyledCalendarWrapper>
                        <StyledCalendar
                            value={date}
                            onChange={handleDateChange}
                            formatDay={(locale, date) => moment(date).format("D")}
                            formatYear={(locale, date) => moment(date).format("YYYY")}
                            formatMonthYear={(locale, date) => moment(date).format("YYYY. MM")}
                            calendarType="gregory"
                            showNeighboringMonth={false}
                            next2Label={null}
                            prev2Label={null}
                            minDetail="year"
                            // 오늘 날짜로 돌아오는 기능을 위해 필요한 옵션 설정
                            activeStartDate={
                                activeStartDate === null ? undefined : activeStartDate
                            }
                            onActiveStartDateChange={({ activeStartDate }) =>
                                setActiveStartDate(activeStartDate)
                            }
                            // 오늘 날짜에 '오늘' 텍스트 삽입하고 출석한 날짜에 점 표시를 위한 설정
                            tileContent={({ date, view }) => {
                                let html = [];
                                if (
                                    view === "month" &&
                                    date.getMonth() === today.getMonth() &&
                                    date.getDate() === today.getDate()
                                ) {
                                    html.push(<StyledToday key={"today"}>오늘</StyledToday>);
                                }
                                if (
                                    attendDay.find((x) => x === moment(date).format("YYYY-MM-DD"))
                                ) {
                                    html.push(<StyledDot key={moment(date).format("YYYY-MM-DD")} />);
                                }
                                return <>{html}</>;
                            }}
                        />
        // 오늘 버튼 추가
                        <StyledDate onClick={handleTodayClick}>오늘</StyledDate>
                    </StyledCalendarWrapper>

                </div>

                {/* 회차 */}
                <div className="w-[130px]  bg-white rounded-lg shadow-md">
                    <h3 className="text-center font-semibold">회차</h3>
                    <div className="mt-2">/* 여기에 회차 선택 컴포넌트 */</div>
                </div>

                {/* 좌석등급 */}
                <div className="w-[190px]  bg-white rounded-lg shadow-md">
                    <h3 className="text-center font-semibold">좌석등급</h3>
                    <div className="mt-2">/* 여기에 좌석등급 선택 컴포넌트 */</div>
                </div>
            </div>

            {/* 하단 유의사항 */}
            <div className="mt-5 p-4 bg-white rounded-lg shadow-md">
                <h3 className="font-semibold">유의사항</h3>
                <ul className="list-disc pl-5">
                    <li>관람일 전일 오후 5시까지 취소 가능하며, 당일 관람 상품 예매 시에는 취소 불가합니다.</li>
                    <li>입장 마감 시간은 관람일 오후 5시, 토요일은 오전 11시입니다.</li>
                    <li>관람일 변경은 예매자 본인만 가능합니다.</li>
                    <li>기타 문의 사항은 고객센터로 문의해주세요.</li>
                </ul>
            </div>


            {/* 모바일 화면 */}
            <div className="block md:hidden bg-white shadow-lg rounded-lg p-4">
                <h2 className="text-lg font-bold mb-4 text-center">LOVE IN SEOUL - 엔플라잉</h2>
                <div className="border p-4 rounded-md mb-4">
                    <h3 className="text-center text-lg font-semibold mb-2">2024년 12월</h3>
                    <table className="w-full text-center text-gray-700">
                        <thead>
                            <tr>
                                <th>일</th>
                                <th>월</th>
                                <th>화</th>
                                <th>수</th>
                                <th>목</th>
                                <th>금</th>
                                <th>토</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td className="text-red-500">1</td>
                                <td>2</td>
                                <td>3</td>
                                <td>4</td>
                                <td>5</td>
                                <td>6</td>
                                <td className="text-blue-500">7</td>
                            </tr>
                            {/* 반복 추가 */}
                        </tbody>
                    </table>
                </div>
                <div>
                    <h3 className="text-lg font-semibold mb-2">시간 선택</h3>
                    <ul>
                        <li className="flex justify-between items-center border p-2 rounded-md mb-2">
                            <span>오후 2:00</span>
                            <button className="bg-red-500 text-white px-4 py-1 rounded-md">선택</button>
                        </li>
                        <li className="flex justify-between items-center border p-2 rounded-md">
                            <span>오후 4:00</span>
                            <button className="bg-red-500 text-white px-4 py-1 rounded-md">선택</button>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    )
}

export default SelectDateComponent;