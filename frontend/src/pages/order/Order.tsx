import { useLocation } from 'react-router-dom';

function Order() {
  const location = useLocation();
  const { faceImg, event_schedule_id } = location.state || {}; // 데이터 가져오기

  console.log('넘겨받은 데이터:', { faceImg, event_schedule_id });

  return (
    <div>
      <h1>다음 단계</h1>
      {/* 데이터를 활용 */}
    </div>
  );
}

export default Order;
