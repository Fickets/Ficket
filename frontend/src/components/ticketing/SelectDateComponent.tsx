import React, { useEffect, useState } from 'react';
import { Calendar } from 'react-calendar'
import CustomCalendar from './CustomCalendar';
import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router';
import { useStore, create } from 'zustand';
import moment from "moment";
import { eventDetailStore } from '../../stores/EventStore';
import SelectDateRoundComponent from './SeleteDateRound';
import { partitionDto, eventScheduleDto } from '../../types/StoreType/EventDetailStore';
import { reverse } from 'dns';


interface SelectRoundProps {
    onRoundselected: (round: number) => void; // 선택된 날짜를 받을 prop
}


const SelectDateComponent: React.FC<SelectRoundProps> = ({ onRoundselected }) => {
    const navi = useNavigate();
    const [choiceDate, setChoiceDate] = useState<string | null>(null); // 선택된 날짜 상태
    const [partitionList, setPartitionList] = useState<Record<string, partitionDto>>({});
    const eventDetail = useStore(eventDetailStore);
    //TEST
    const [eventRounds, setEventRounds] = useState<Record<string, eventScheduleDto>>({}); // Map 타입으로 초기화

    // -------------------------------------------
    const [gradeColors, setGradeColors] = useState<{
        [key: string]: string;
    } | null>(null);
    let [seatGrades, setSeatGrades] = useState<string[]>();
    useEffect(() => {


    });

    const generateDistinctColors = (totalGrades: number) => {
        const colors: string[] = [];
        const step = 360 / totalGrades;

        for (let i = 0; i < totalGrades; i++) {
            const hue = Math.round(i * step);
            const color = `hsl(${hue}, 70%, 50%)`;
            colors.push(color);
        }

        return colors;
    };

    // -------------------------------------------



    // 선택된 날짜가 변경될 때마다 실행
    const handleDateSelect = (date: string) => {
        setChoiceDate(date); // 날짜 선택 시 choiceDate 상태 업데이트
        setEventRounds(eventDetail.scheduleMap[date])
        onRoundselected(1); // round를 초기화
    };

    useEffect(() => {
        if (choiceDate != null) {
            // round가 1로 초기화되면 해당 값에 맞는 데이터를 다시 가져와야 함
            const selectedSchedule = eventDetail.scheduleMap[choiceDate]?.[1]; // 1번 round에 해당하는 데이터 찾기

            if (selectedSchedule?.partition) {
                setPartitionList(selectedSchedule.partition);
            } else {
                // partition이 없으면 기본값 설정하거나 예외처리
                console.error("No partition data available for the selected date and round.");
            }
        }
    }, [choiceDate]);

    useEffect(() => {
        if (
            eventDetail?.scheduleMap?.[eventDetail.choiceDate]?.[eventDetail.round]?.partition
        ) {
            const t1 = eventDetail.scheduleMap[eventDetail.choiceDate][eventDetail.round]["partition"];
            setPartitionList(t1);
            //-------
            setSeatGrades(Object.keys(t1).sort().reverse());
            const distinctColors = generateDistinctColors(2);
            const newGradeColors: { [key: string]: string } = {};
            Object.keys(t1).sort().reverse().forEach((grade, index) => {
                newGradeColors[grade] = distinctColors[index];
            })
            // seatGrades.forEach((grade, index) => {
            //     newGradeColors[grade] = distinctColors[index];
            // });
            setGradeColors(newGradeColors);
            onRoundselected(eventDetail.round);
        }

        //-------

    }, [eventDetail.round])

    const formatEventTime = (eventDate) => {
        const dateObj = new Date(eventDate);  // eventDate를 Date 객체로 변환
        let hours = dateObj.getHours();  // 24시간 형식 시간
        let minutes = dateObj.getMinutes();  // 분
        let period = hours >= 12 ? '오후' : '오전';  // 오전/오후 구분

        // 12시간 형식으로 변환
        hours = hours % 12;
        hours = hours ? hours : 12;  // 0시인 경우 12로 변경
        minutes = minutes < 10 ? '0' + minutes : minutes;  // 1자리 분을 두자리로 변경

        return `${period} ${hours}:${minutes}`;
    };

    const roundsClick = (event) => {
        const key = event.target.value;
        eventDetail.setRound(key);
        const date = event.target.getAttribute("date");
        eventDetail.setChoicetime(date)
        navi("/ticketing/select-seat")
    };


    return (
        <div>
            <div className="w-[600px] h-[450px] min-w-[600px] min-h-[450px] p-[15px] hidden md:block"
            >
                {/* 상단 3개 컴포넌트 - 가로로 배치 */}
                <div className="flex ">
                    {/* 관람일 선택 */}
                    <div className="w-[250px]  bg-white  shadow-md">
                        <h3 className="ml-[10px] text-gray-700 font-semibold text-[15px] mt-[10px]">관람일 선택</h3>
                        <hr className="ml-[10px] mr-[10px] border-t-2 border-black" />
                        {/* 커스텀 달력 불러오기 */}
                        <CustomCalendar onDateSelect={handleDateSelect} />

                        <div className="flex gap-2 bg-[#FF3F3] h-[50px] border-y-[1px] border-gray-950 mb-[15px]">
                            {/* 선택 가능 */}
                            <div className="flex items-center gap-2 ml-[5px]">
                                <div className="w-3 h-3 bg-[#EDBF6A] border border-[#C89F53]"></div>
                                <span className="text-[10px] text-[#666666]">예매 가능일</span>
                            </div>

                            {/* 선택된 날짜 */}
                            <div className="flex items-center gap-2">
                                <div className="w-3 h-3 bg-[#DD0808] border border-[#BE0707]"></div>
                                <span className="text-[10px] text-[#666666]">선택한 관람일</span>
                            </div>
                        </div>


                    </div>

                    {/* 회차 선택 */}
                    <div className="flex flex-col w-[130px] bg-white shadow-md">
                        <h3 className="ml-[10px] text-gray-700 font-semibold text-[15px] mt-[10px]">회차</h3>
                        <hr
                            className='ml-[10px] mr-[10px]'
                        />
                        <SelectDateRoundComponent selectedDate={choiceDate} />
                    </div>

                    {/* 좌석등급 */}
                    <div className="w-[190px] h-[270px] bg-white shadow-md border-[1px] border-[#999999]">
                        <div className='h-[30px]'>
                            <h3 className="mt-[10px] ml-[10px] text-[15px] text-[#696969] font-bold">좌석등급 / 잔여석</h3>
                        </div>
                        <hr className='border-t-1 border-gray' />
                        <div>
                            {Object.entries(partitionList).map(([key, value]) => (
                                <div className='flex items-center ml-[10px]'>
                                    <div className="w-3 h-3 bg-[#EDBF6A] border border-[#C89F53]"
                                        style={{ backgroundColor: gradeColors[key] }}></div>
                                    <p key={key}

                                        className='mt-[5px] ml-[5px] text-[12px] font-medium'>
                                        {key} 등급 | {value.remainingSeats}석
                                    </p>
                                </div>
                            ))}
                        </div>
                    </div>


                </div>
                {/* 하단 유의사항 */}
                <div className="bg-white shadow-md border border-black">
                    <h3 className="font-semibold mt-[5px]  ml-[20px] text-[15px]">유의사항</h3>
                    <hr className="ml-[20px] mr-[20px] border-t-1 border-[#666666]" />

                    <ul className="list-disc pl-10 mt-[8px] mb-[8px] h-[100px]">
                        <li className='text-[10px] text-[#666666]'>관람일 전일 아래시간까지만 취소 가능하며, 당일 관람 상품 예매 시에는 취소 불가합니다.</li>
                        <p className='text-[10px] text-[#666666]'>- 공연전일 평일/일요일/공휴일 오후 5시, 토요일 오전 11시 (단 토요일이 공휴일인 경우는 오전 11시)</p>
                        <p className='text-[10px] text-[#666666]'>- 당일관람 상품예매시는 취소불가합니다.</p>
                        <p className='text-[10px] text-[#666666]'>- 취소수수료와 취소가능일자는 상품별로 다르니, 오른쪽 하단 나의티켓을 확인해주시기 바랍니다.</p>
                        <li className='text-[10px] text-[#666666]'>입장 마감 시간은 관람일 오후 5시, 토요일은 오전 11시입니다.</li>
                        <li className='text-[10px] text-[#666666]'>관람일 변경은 예매자 본인만 가능합니다.</li>
                        <li className='text-[10px] text-[#666666]'>기타 문의 사항은 고객센터로 문의해주세요. / 고객센터 : 031-123-4567</li>
                    </ul>
                </div>
            </div>
            {/* 모바일 화면 */}
            <div className='block md:hidden'>
                <CustomCalendar onDateSelect={handleDateSelect} />

                <hr className='w-[350px] mt-[10px] border-t-2 border-gray-300' />
                {Object.entries(eventRounds).map(([key, value]) => (
                    <div className='flex flex-col  ml-[0px] mb-[15px]'>
                        <div className='flex justify-between mt-[5px]'>
                            <p key={key}
                                className='mt-[5px] text-[20px] font-medium text-[#C953E1]'>
                                {formatEventTime(value["eventDate"])}
                            </p>
                            <button
                                className='bg-[#E44444] text-white w-[100px]'
                                value={key}
                                date={value["eventDate"].split("T")[1]}
                                onClick={roundsClick}
                            >버튼</button>
                        </div>
                        <div className='mt-[5px]'>
                            <hr className='w-[350px] mt-[0px] border-t-2 border-[#C953E1]' />
                            <div className='flex flex-wrap justify-between bg-gray-200'>
                                {Object.entries(value.partition).map(([key, partition]) => (
                                    <div className='w-1/2 p-2' key={key}>  {/* 각 항목을 50% 너비로 배치 */}
                                        <div className='flex justify-between'>
                                            <p className='text-[15px]'
                                            >{partition.partitionName} 등급</p>
                                            <p className='text-[#E44444] text-[15px]'
                                            >{partition.remainingSeats}석</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                ))}

            </div>

        </div >
    )
}

export default SelectDateComponent;