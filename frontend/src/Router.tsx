import { Routes, Route, useNavigate } from 'react-router-dom';
import { useStore } from 'zustand';
import { useEffect } from 'react';

import HomePage from './pages/home/HomePage';
import UserLogin from './pages/user/UserLogin';
import RegisterEvent from './pages/register/RegisterEvent';
import EditEvent from './pages/edit/EditEvent.tsx';
import RegisterFace from './pages/registerface/RegisterFace.tsx';
import Order from './pages/order/Order.tsx';
import NotFound from './pages/errorpage/NotFound.tsx';

import { userStore } from './stores/UserStore';
import SelectSession from './pages/selectsession/SelectSession.tsx';
import SelectSeat from './pages/selectseat/SelectSeat.tsx';

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
      <Route path="/login" element={<UserLogin />} />

      {/* FICKET ADMIN */}
      <Route path="/admin/register-event" element={<RegisterEvent />} />
      <Route path="/admin/edit-event/:eventId" element={<EditEvent />} />

      {/* FICKET USER TICKETING*/}
      <Route
        path="ticketing/select-session/:eventId"
        element={<SelectSession />}
      />
      <Route path="ticketing/select-seat" element={<SelectSeat />} />
      <Route path="ticketing/register-face" element={<RegisterFace />} />
      <Route path="ticketing/order" element={<Order />} />

      {/* */}
      {/* */}
    </Routes>
  );
}
