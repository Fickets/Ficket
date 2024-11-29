import { RightPanelProps } from '../../types/selectseat';

const RightPanel = ({
  gradeColors,
  eventTitle,
  eventStage,
  eventDate,
  evnetTime,
  posterPcUrl,
  seatGradeInfoList,
  seatCntGrade,
  selectedSeats,
}: RightPanelProps) => {
  return (
    <div className="w-[240px] h-[380px] flex flex-col mt-4 p-4 space-y-4">
      {/* 포스터와 정보 영역 */}
      <div className="flex">
        {/* 이미지 영역 */}
        <div className="w-[40%] flex items-center justify-center bg-gray-100">
          <img src={posterPcUrl} alt="Event Poster" className="w-full h-auto" />
        </div>

        {/* 행사 정보 영역 */}
        <div className="w-[60%] pl-2">
          <h3 className="text-white font-bold text-sm p-1 -mt-2 break-words">
            {eventTitle}
          </h3>
          <div className="text-xs text-gray-700 mt-2">
            <p>일시: {eventDate}</p>
            <p>시간: {evnetTime}</p>
            <p>{eventStage}</p>
          </div>
        </div>
      </div>

      {/* 좌석 정보 영역 */}
      <div>
        <h4 className="text-black font-bold text-sm mb-2">
          좌석 정보 / 잔여석
        </h4>
        <ul className="text-sm text-gray-700">
          {seatCntGrade.map((grade, index) => {
            const matchingGrade = seatGradeInfoList.find(
              (info) => info.grade === grade.partitionName
            );

            return (
              <li
                key={index}
                className="flex justify-between items-center py-1 border-b border-gray-300 bg-[#FFFFFF]"
              >
                {/* 등급 이름 (왼쪽) */}
                <span className="flex items-center">
                  <span
                    className="w-4 h-4 rounded-full mr-2"
                    style={{
                      backgroundColor: gradeColors[grade.partitionName], // 고유 색상 매핑
                    }}
                  ></span>
                  {grade.partitionName}
                </span>

                {/* 잔여석 */}
                <span className="text-red-500">{grade.count}석</span>

                {/* 가격 */}
                <span>
                  {matchingGrade ? matchingGrade.price.toLocaleString() : '-'}원
                </span>
              </li>
            );
          })}
        </ul>
      </div>

      {/* 선택 좌석 테이블 (스크롤 가능) */}
      <div>
        <h4 className="text-black font-bold text-sm mb-1">선택 좌석</h4>{' '}
        <div className="overflow-y-auto max-h-[80px] border border-gray-300">
          {' '}
          <table className="table-auto w-full text-left text-xs text-gray-700">
            {' '}
            <thead>
              <tr className="bg-gray-200">
                <th className="px-1 py-1 border-b border-gray-300">등급</th>{' '}
                <th className="px-1 py-1 border-b border-gray-300">
                  좌석 번호
                </th>
              </tr>
            </thead>
            <tbody>
              {selectedSeats.length > 0 ? (
                selectedSeats.map((seat, index) => (
                  <tr key={index} className="border-b">
                    <td className="px-1 py-1">{seat.grade}</td>{' '}
                    <td className="px-1 py-1">
                      {seat.row}열-{seat.col}번
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td
                    colSpan={2}
                    className="px-1 py-1 text-gray-500 text-center"
                  >
                    선택된 좌석이 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default RightPanel;
