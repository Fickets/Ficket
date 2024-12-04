import React, { useEffect, useState } from 'react';
import { Calendar } from 'react-calendar'
import CustomCalendar from './CustomCalendar';
import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router';
import { useStore } from 'zustand';
import moment from "moment";
import { useEventStore } from '../../types/StoreType/EventState';
const SelectDateComponent: React.FC = () => {
    const {
        setEventId,
        setEventScheduleId,
        setEventDate,
        setEventTime
    } = useEventStore();


    return (
        <div className="w-[600px] h-[450px] min-w-[600px] min-h-[450px] p-[15px]"

        >
            {/* 상단 3개 컴포넌트 - 가로로 배치 */}
            <div className="flex ">
                {/* 관람일 선택 */}
                <div className="w-[250px]  bg-white  shadow-md">
                    <h3 className="ml-[10px] text-gray-700 font-semibold text-[15px] mt-[10px]">관람일 선택</h3>
                    <hr className="ml-[10px] mr-[10px] border-t-2 border-black" />
                    <CustomCalendar />

                    <div className="flex gap-2 bg-[#F3F3F3] h-[50px] border-y-[1px] border-gray-950 mb-[15px]">
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

                {/* 회차 */}
                <div>
                    <div className="w-[130px] flex bg-white shadow-md">
                        <h3 className="ml-[10px] text-gray-700 font-semibold text-[15px] mt-[10px]">회차</h3>
                        <p className='text-[13px] mt-[12px]'>&nbsp;(관람선택)</p>
                    </div>
                    <div className='flex flex-col bg-white w-[130px] h-[222px]'>
                        <hr className="ml-[10px] mr-[10px] border-t-2 border-black" />
                        // 여기에 이제 api 호출해서 해당 요일에 몆개의
                    </div>
                </div>

                {/* 좌석등급 */}
                <div className="w-[190px]  bg-white rounded-lg shadow-md">
                    <h3 className="text-center font-semibold">좌석등급</h3>
                    <div className="mt-2">/* 여기에 좌석등급 선택 컴포넌트 */</div>
                </div>


            </div>
            {/* 하단 유의사항 */}
            <div className="bg-white shadow-md border border-black">
                <h3 className="font-semibold mt-[5px]  ml-[20px] text-[15px]">유의사항</h3>
                <hr className="ml-[20px] mr-[20px] border-t-1 border-[#666666]" />

                <ul className="list-disc pl-10 mt-[20px] mb-[10px]">
                    <li className='text-[10px] text-[#666666]'>관람일 전일 아래시간까지만 취소 가능하며, 당일 관람 상품 예매 시에는 취소 불가합니다.</li>
                    <p className='text-[10px] text-[#666666]'>- 공연전일 평일/일요일/공휴일 오후 5시, 토요일 오전 11시 (단 토요일이 공휴일인 경우는 오전 11시)</p>
                    <p className='text-[10px] text-[#666666]'>- 당일관람 상품예매시는 취소불가합니다.</p>
                    <p className='text-[10px] text-[#666666]'>- 취소수수료와 취소가능일자는 상품별로 다르니, 오른쪽 하단 나의티켓을 확인해주시기 바랍니다.</p>
                    <li className='text-[10px] text-[#666666]'>입장 마감 시간은 관람일 오후 5시, 토요일은 오전 11시입니다.</li>
                    <li className='text-[10px] text-[#666666]'>관람일 변경은 예매자 본인만 가능합니다.</li>
                    <li className='text-[10px] text-[#666666]'>기타 문의 사항은 고객센터로 문의해주세요. / 고객센터 : 031-123-4567</li>
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