import React from 'react';

import SelectDateComponent from '../../components/ticketing/SelectDateComponent';
import TicketingHeader from '../../components/ticketing/TicketingHeader';
import BackgroundImg from '../../assets/ticketing/FicketingBg.png';
const AdminLogin: React.FC = () => {


    return (
        <div className="w-[900px] h-[600px] min-w-[900px] min-h-[600px] "
            style={{
                backgroundImage: `url(${BackgroundImg})`,
            }}
        >
            <div className='pb-3.5'>
                <TicketingHeader step={"1"} />
            </div>
            <div className='flex w-full h-full justify-between overflow-hidden flex-grow'>
                {/* 왼쪽 컴포넌트 */}
                <div className="w-[600px] h-[450px] bg-orange-200  ml-[30px] mr-[10px]">
                    <SelectDateComponent />
                </div>
                {/* 오른쪽 컴포넌트 */}
                <div className="w-[260px] h-[475px]  bg-red-400">

                </div>

            </div>
        </div>
    )
};

export default AdminLogin;