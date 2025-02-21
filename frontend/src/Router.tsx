import { Routes, Route } from "react-router-dom";
import HomePage from "./pages/home/HomePage";
import UserLogin from "./pages/login/UserLogin.tsx";
import RegisterEvent from "./pages/register/RegisterEvent";
import RegisterFace from "./pages/registerface/RegisterFace.tsx";
import Order from "./pages/order/Order.tsx";
import NotFound from "./pages/errorpage/NotFound.tsx";
import UserAdditionalInfo from "./pages/login/UserAdditionalInfo.tsx";
import AdminLogin from "./pages/login/AdminLogin.tsx";
import SelectSeat from "./pages/selectseat/SelectSeat.tsx";
import SeleteDate from "./pages/ticketing/SelectDate.tsx";
import EventDetailPage from "./pages/event/EventDetail.tsx";
import AdminEventList from "./pages/eventlist/AdminEventList.tsx";
import Search from "./pages/search/Search.tsx";
import AdminEventDetail from "./pages/adminEventDetail/AdminEventDetail.tsx";
import MyTicket from "./pages/myTicket/MyTicket.tsx";
import ReservationRateRanking from "./pages/reservationRateRanking/ReservationRateRanking.tsx";
import Queue from "./pages/queue/Queue.tsx";
import SuspendedUserPage from "./pages/login/Suspended.tsx";
import UserManagePage from "./pages/admin/Customers.tsx";
import EventScheduledOpen from "./pages/eventScheduledOpen/EventScheduledOpen.tsx";
import SettlementManagePage from "./pages/admin/Settlement.tsx";
import GenreChoice from "./pages/event/GenreChoice.tsx";
import FaceDetectionPage from "./pages/ticketCheck/customerCheck.tsx";
import EventCheckPage from "./pages/ticketCheck/eventCheck.tsx";
import ManagerCheckPage from "./pages/ticketCheck/managerCheck.tsx";
import OrderComplete from "./pages/order/OrderComplete.tsx";

export default function Router() {
  return (
    <Routes location={location} key={location.pathname}>
      {/* ERROR PATH */}
      <Route path="*" element={<NotFound />} />
      {/* MAIN HOME */}
      <Route path="/" element={<HomePage />} />
      <Route path="/contents/search" element={<Search />} />
      {/* USER LOGIN */}
      <Route path="/users/login" element={<UserLogin />} />
      <Route path="/users/addition-info" element={<UserAdditionalInfo />} />
      <Route path="/users/suspended" element={<SuspendedUserPage />} />
      {/* ADMIN LOGIN*/}
      <Route path="/admin/login" element={<AdminLogin />} />
      {/* FICKET ADMIN */}
      <Route path="/admin/event-list" element={<AdminEventList />} />
      <Route path="/admin/register-event" element={<RegisterEvent />} />
      <Route
        path="/admin/event-detail/:eventId"
        element={<AdminEventDetail />}
      />
      <Route path="/admin/customers" element={<UserManagePage />} />
      <Route path="/admin/settlements" element={<SettlementManagePage />} />

      {/* FICKET USER TICKETING*/}
      <Route path="ticketing/queue/:eventId" element={<Queue />} />
      <Route path="ticketing/select-date" element={<SeleteDate />} />
      <Route path="ticketing/select-seat" element={<SelectSeat />} />
      <Route path="ticketing/register-face" element={<RegisterFace />} />
      <Route path="ticketing/order" element={<Order />} />
      <Route path="ticketing/order-complete" element={<OrderComplete />} />

      {/* FICKET USER EVENT  */}
      <Route path="events/detail/:eventId" element={<EventDetailPage />} />
      <Route path="contents/ranking" element={<ReservationRateRanking />} />
      <Route path="contents/scheduled-open" element={<EventScheduledOpen />} />
      <Route path="/events/genre-choice" element={<GenreChoice />} />

      {/* FICKET USER TICKETING*/}
      <Route path="/my-ticket" element={<MyTicket />} />
      <Route path="/events/:eventId/access" element={<EventCheckPage />} />
      <Route path="/events/:eventId/customer" element={<FaceDetectionPage />} />
      <Route path="/events/:eventId/manager" element={<ManagerCheckPage />} />
    </Routes>
  );
}
