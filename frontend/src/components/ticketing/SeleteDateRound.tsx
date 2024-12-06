import React, { useEffect, useState } from 'react';
import { Calendar } from 'react-calendar'
import CustomCalendar from './CustomCalendar';
import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router';
import { useStore, create } from 'zustand';
import moment from "moment";
import { eventDetailStore } from '../../stores/EventStore';

interface SelectDateRoundComponentProps {
    selectedDate: string | null; // 선택된 날짜를 받을 prop
}

const SelectDateRoundComponent: React.FC<SelectDateRoundComponentProps> = ({ selectedDate }) => {

    const eventDetail = useStore(eventDetailStore);
    // const choiceDate = eventDetailStore((state) => state.choiceDate); // choiceDate 가져오기
    const choiceDate = eventDetailStore((state) => state.choiceDate); // choiceDate 가져오기

    const [roundList, setRoundList] = useState<any[]>([]); // 회차 정보,
    const [remainingSeat, setRamainingSeat] = useState<any[]>([]);
    const [selectedIndex, setSelectedIndex] = useState<number | null>(null); // 클릭된 버튼의 인덱스




    // round 정보를 시/분으로 파싱
    const formatEventDate = (eventDate: string) => {
        const [hour, minute] = eventDate.split(":");
        return `${hour}시 ${minute}분`;
    };


    // 초기에  roundList 설정
    useEffect(() => {
        const tmpp = selectedDate

        if (tmpp != null) {
            const tmp = eventDetail.scheduleMap[tmpp];
            eventDetail.setChoiceDate(tmpp);
            const tt = Object.keys(tmp).map((key) => ({
                round: tmp[key].round,
                eventTime: formatEventDate(tmp[key].eventDate.split("T")[1]),
            }))
            setRoundList(tt);

        }

    }, []);


    // choiceDate 값 변경 감지 | 달력 날짜 클릭시
    useEffect(() => {
        if (choiceDate) {

            handleChoiceDateChange(choiceDate);
        }
    }, [choiceDate]); // choiceDate 변경 시 실행

    // 회차 정보 변경 감지 | 날짜가 바뀌여서 회차 정보 변경 시
    useEffect(() => {
        // 여기서 남은 좌석 데이터 리스트 갱신할 예정 
        setSelectedIndex(0);
        eventDetail.setRound(1);
     
    }, [roundList]); // roundList가 변경될 때마다 실행


    // 날짜 변경 시 회차 데이터 처리
    const handleChoiceDateChange = (newDate: string) => {
        
        const tmp = eventDetail.scheduleMap[newDate];
        const keys = Object.keys(tmp);
        const tt = Object.keys(tmp).map((key) => ({
            round: tmp[key].round,
            eventTime: formatEventDate(tmp[key].eventDate.split("T")[1]),
        }))
        setRoundList(tt);

    };


    const handleRoundClick = (event: React.MouseEvent<HTMLButtonElement>, index: number) => {
        const value = event.currentTarget.value; // 클릭된 버튼의 value 속성 값 가져오기
        eventDetail.setRound(parseInt(value, 10));
        setSelectedIndex(index); // 클릭한 버튼의 인덱스 저장
    };

    return (
        <div
            className=''>
            {roundList.map((roundInfo, index) => (
                <button
                    key={index}
                    value={roundInfo.round} // 버튼에 round 값을 value로 추가
                    onClick={(event) => handleRoundClick(event, index)} // index를 함께 전달
                    className={`mt-2 ml-[10px] mr-[10px] p-2 hover:bg-[#ddd] text-[10px] text-white w-[110px] h-[20px] flex items-center justify-center ${selectedIndex === index ? 'bg-red-500' : 'bg-[#EDBF6A]' // 선택된 버튼 색상 변경
                        }`}
                >
                    {roundInfo.eventTime}
                </button>
            ))}
        </div>
    )
}

export default SelectDateRoundComponent;