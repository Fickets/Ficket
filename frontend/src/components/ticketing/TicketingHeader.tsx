import React, { useEffect, useState } from 'react';

import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router';
import { useStore } from 'zustand';
import { eventDetailStore } from '../../stores/EventStore';
import HeadImg1 from '../../assets/ticketing/ticketingHeader1.png'
import HeadImg2 from '../../assets/ticketing/ticketingHeader2.png'
import HeadImg3 from '../../assets/ticketing/ticketingHeader3.png'
import HeadImg4 from '../../assets/ticketing/ticketingHeader4.png'
import Logo from '../../assets/logo.png';
const TicketingHeader = ({ step }: { step: number }) => {
    const eventDetail = useStore(eventDetailStore);
    const images: { [key: number]: string } = {
        1: HeadImg1,
        2: HeadImg2,
        3: HeadImg3,
        4: HeadImg4,
    };

    return (
        <div className='flex flex-col items-center '>
            {/* pc 버전 */}
            <div className='hidden md:block'>

                <div className='flex mt-2 items-center justify-start w-full '>
                    <img src={Logo}
                        className='w-14 ml-[30px]'
                        alt="" />
                    <p
                        className='text-1xl text-white'
                    >Ficket 티켓예매</p>
                </div>
                <img src={images[step]} alt="" className='mt-[5px]' />
            </div>
            {/* 모바일 */}
            <div className="block md:hidden w-screen flex flex-col bg-white overflow-hidden">
                {/* Step Indicator */}
                <div className="flex justify-center items-center bg-[#EAEAEA]">
                    {Array(4)
                        .fill(0)
                        .map((_, index) => (
                            <div
                                key={index}
                                className={`w-3 h-3 mx-1 mt-[10px] rounded-full ${step === index + 1 ? "bg-[#E94343]" : "bg-white"
                                    }`}
                            ></div>
                        ))}
                </div>

                {/* 공연 제목 */}
                <div className="text-center py-2 bg-[#EAEAEA]">
                    <h3 className="text-lg font-bold text-pink-800">{eventDetail.title}</h3>
                    <p className="text-sm text-gray-500">{eventDetail.stageName}</p>
                </div>
            </div>
        </div >
    )
}

export default TicketingHeader;