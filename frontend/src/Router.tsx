import { Routes, Route, useNavigate } from 'react-router-dom';
import { useStore } from 'zustand';
import { useEffect } from 'react';

import HomePage from './pages/home/HomePage';
import UserLogin from "./pages/login/UserLogin.tsx"
import RegisterEvent from './pages/register/RegisterEvent';
import EditEvent from "./pages/edit/EditEvent.tsx";
import RegisterFace from "./pages/registerface/RegisterFace.tsx";
import Order from "./pages/order/Order.tsx";
import NotFound from "./pages/errorpage/NotFound.tsx";
import UserAdditionalInfo from './pages/login/UserAdditionalInfo.tsx';
import AdminLogin from "./pages/login/AdminLogin.tsx";
import { userStore } from "./stores/UserStore";

export default function Router() {

    const user = useStore(userStore);
    const navi = useNavigate();

    // useEffect(() => {
    //     if (!user.isLogin && !location.pathname.includes('/attend')) {
    //         navi('/login');
    //     }
    // }, [location.pathname, navi])

    return (

        <Routes location={location} key={location.pathname}>
            {/* ERROR PATH */}
            <Route path="*" element={<NotFound />} />
            {/* MAIN HOME */}
            <Route path="/" element={<HomePage />} />
            {/* USER LOGIN */}
            <Route path="/users/login" element={<UserLogin />} />
            <Route path="/users/addition-info" element={<UserAdditionalInfo />} />
            {/* ADMIN LOGIN*/}
            <Route path="/admin/login" element={<AdminLogin />} />
            {/* FICKET ADMIN */}
            <Route path="/admin/register-event" element={<RegisterEvent />} />
            <Route path="/admin/edit-event/:eventId" element={<EditEvent />} />


            {/* FICKET USER TICKETING*/}
            <Route path="ticketing/register-face/:eventScheduleId" element={<RegisterFace />} />
            <Route path="ticketing/order" element={<Order />} />



            {/* */}
            {/* */}



        </Routes>
    );

}

