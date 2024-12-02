import React, { useEffect, useState } from 'react';

import { useCookies } from 'react-cookie';
import { useNavigate } from 'react-router';
import { useStore } from 'zustand';

import HeadImg1 from '../../assets/ticketing/ticketingHeader1.png'
import HeadImg2 from '../../assets/ticketing/ticketingHeader2.png'
import HeadImg3 from '../../assets/ticketing/ticketingHeader3.png'
import HeadImg4 from '../../assets/ticketing/ticketingHeader4.png'
import Logo from '../../assets/logo.png';
const TicketingHeader = ({ step }: { step: string }) => {

    const images: { [key: string]: string } = {
        1: HeadImg1,
        2: HeadImg2,
        3: HeadImg3,
        4: HeadImg4,
    };

    return (
        <div className='flex flex-col items-center'>
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
    )
}

export default TicketingHeader;