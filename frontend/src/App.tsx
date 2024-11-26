import { RouterProvider, createBrowserRouter } from 'react-router-dom';
import HomePage from './pages/home/HomePage';
import RegisterEvent from './pages/register/RegisterEvent';
import EditEvent from './pages/edit/EditEvent';
import NotFound from './pages/errorpage/NotFound';
import RegisterFace from './pages/registerface/RegisterFace';
import Order from './pages/order/Order';

const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />,
  },
  {
    path: '/admin/register-event',
    element: <RegisterEvent />,
  },
  {
    path: '/admin/edit-event/:eventId',
    element: <EditEvent />,
  },
  {
    path: 'ticketing/register-face/:eventScheduleId',
    element: <RegisterFace />,
  },
  {
    path: 'ticketing/order',
    element: <Order />,
  },
  { path: '*', element: <NotFound /> }, // 모든 잘못된 경로 처리
]);

function App() {
  return (
    <>
      <RouterProvider router={router} />
    </>
  );
}

export default App;
