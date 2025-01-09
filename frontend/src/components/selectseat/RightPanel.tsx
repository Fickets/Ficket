import { RightPanelProps } from "../../types/selectseat";

const RightPanel = ({
  gradeColors,
  eventTitle,
  eventStage,
  eventDate,
  eventTime,
  posterMobileUrl,
  seatGradeInfoList,
  seatCntGrade,
  selectedSeats,
}: RightPanelProps) => {
  return (
    <div className="w-[250px] h-[380px] flex flex-col mt-2 p-4 space-y-4">
      {/* 포스터와 정보 영역 */}
      <div className="flex -mt-6">
        {/* 이미지 영역 */}
        <div className="flex items-center justify-center bg-gray-100">
          <img
            src={posterMobileUrl}
            alt="Event Poster"
            className="w-full h-auto"
          />
        </div>

        {/* 행사 정보 영역 */}
        <div className="w-[60%] pl-2 mt-2">
          <h3 className="text-white font-bold text-sm p-1 break-words">
            {eventTitle.length > 15
              ? `${eventTitle.substring(0, 15)}...`
              : eventTitle}
          </h3>
          <div className="text-xs text-gray-700 mt-2">
            <p>일시: {eventDate}</p>
            <p>시간: {eventTime}</p>
            <p>{eventStage}</p>
          </div>
        </div>
      </div>

      {/* 좌석 정보 영역 */}
      <div>
        <h4 className="text-black font-bold text-sm mb-2">
          좌석 정보 / 잔여석
        </h4>
        <div className="overflow-y-auto max-h-[80px] border border-gray-300">
          <table className="table-auto w-full text-left text-xs text-gray-700">
            <thead>
              <tr className="bg-gray-200">
                <th className="px-2 py-1 border-b border-gray-300">등급</th>
                <th className="px-2 py-1 border-b border-gray-300">잔여석</th>
                <th className="px-2 py-1 border-b border-gray-300">가격</th>
              </tr>
            </thead>
            <tbody>
              {seatCntGrade.map((grade, index) => {
                const matchingGrade = seatGradeInfoList.find(
                  (info) => info.grade === grade.partitionName,
                );

                return (
                  <tr key={index} className="border-b">
                    {/* 등급 이름 */}
                    <td className="px-2 py-1 flex items-center">
                      <span
                        className="w-4 h-4 rounded-full mr-2"
                        style={{
                          backgroundColor: gradeColors[grade.partitionName], // 등급 색상 매핑
                        }}
                      ></span>
                      {grade.partitionName}
                    </td>

                    {/* 잔여석 */}
                    <td className="px-2 py-1 text-red-500">{grade.count}석</td>

                    {/* 가격 */}
                    <td className="px-2 py-1">
                      {matchingGrade
                        ? matchingGrade.price.toLocaleString()
                        : "-"}
                      원
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* 선택 좌석 테이블 (스크롤 가능) */}
      <div>
        <h4 className="text-black font-bold text-sm mb-1">선택 좌석</h4>
        <div className="overflow-y-auto max-h-[80px] border border-gray-300">
          <table className="table-auto w-full text-left text-xs text-gray-700">
            <thead>
              <tr className="bg-gray-200">
                <th className="px-1 py-1 border-b border-gray-300">등급</th>
                <th className="px-1 py-1 border-b border-gray-300">
                  좌석 번호
                </th>
              </tr>
            </thead>
            <tbody>
              {selectedSeats.length > 0 ? (
                selectedSeats.map((seat, index) => (
                  <tr key={index} className="border-b">
                    <td className="px-1 py-1">{seat.grade}</td>
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
