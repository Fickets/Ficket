import { useState, useEffect, useRef } from "react";
import { fetchStageSeats } from "../../service/register/api";
import { SeatInfo, SeatSettingProps, SeatGrade } from "../../types/edit";

const getRandomColor = () => {
  const letters = "0123456789ABCDEF";
  return `#${Array.from({ length: 6 })
    .map(() => letters[Math.floor(Math.random() * 16)])
    .join("")}`;
};

const EditSeatSetting = ({
  stageId,
  stageImg,
  initialData = [],
  onChange,
}: SeatSettingProps) => {
  const [seatCoordinates, setSeatCoordinates] = useState<SeatInfo[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<SeatInfo[]>([]);
  const [grades, setGrades] = useState<
    { grade: SeatGrade; color: string; seats: SeatInfo[] }[]
  >([]);
  const [currentGrade, setCurrentGrade] = useState("");
  const [currentPrice, setCurrentPrice] = useState("");
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState<{ x: number; y: number } | null>(
    null,
  );
  const [dragEnd, setDragEnd] = useState<{ x: number; y: number } | null>(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [containerSize, setContainerSize] = useState({ width: 0, height: 0 });

  // 초기화 및 데이터 로드
  useEffect(() => {
    const loadSeats = async () => {
      if (!stageId) return;

      setLoading(true);
      setError("");

      try {
        const seats = await fetchStageSeats(stageId);
        setSeatCoordinates(seats);

        const mappedGrades = initialData.map((grade) => {
          const matchingSeats = seats.filter((seat) =>
            grade.seats.includes(seat.seatId),
          );
          return {
            grade: { ...grade },
            color: getRandomColor(),
            seats: matchingSeats,
          };
        });
        setGrades(mappedGrades);
      } catch (err) {
        setError("좌석 데이터를 불러오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    loadSeats();
  }, [stageId, initialData]);

  // 컨테이너 크기 업데이트
  useEffect(() => {
    const updateContainerSize = () => {
      if (containerRef.current) {
        setContainerSize({
          width: containerRef.current.offsetWidth,
          height: containerRef.current.offsetHeight,
        });
      }
    };

    updateContainerSize();
    window.addEventListener("resize", updateContainerSize);
    return () => window.removeEventListener("resize", updateContainerSize);
  }, []);

  const toggleSeatSelection = (seat: SeatInfo) => {
    if (getSeatGrade(seat)) return;

    setSelectedSeats((prev) =>
      prev.some((selected) => selected.seatId === seat.seatId)
        ? prev.filter((selected) => selected.seatId !== seat.seatId)
        : [...prev, seat],
    );
  };

  const calculateOriginalSize = (seats: SeatInfo[]) => {
    const maxX = Math.max(...seats.map((seat) => seat.x));
    const maxY = Math.max(...seats.map((seat) => seat.y));
    return { width: maxX, height: maxY };
  };

  const { width: originalWidth, height: originalHeight } =
    calculateOriginalSize(seatCoordinates);

  const calculatePosition = (x: number, y: number) => {
    // 좌표를 컨테이너 크기에 맞게 스케일링
    const scaledX = (x / originalWidth) * containerSize.width * 0.91;
    const scaledY = (y / originalHeight) * containerSize.height * 0.92;
    return { scaledX, scaledY };
  };

  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    const { offsetX, offsetY } = e.nativeEvent;
    setDragStart({ x: offsetX, y: offsetY });
    setDragEnd(null);
  };

  const handleMouseUp = () => {
    setIsDragging(false);
    setDragStart(null);
    setDragEnd(null);
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging || !dragStart || !containerRef.current) return;

    const { offsetX, offsetY } = e.nativeEvent;
    setDragEnd({ x: offsetX, y: offsetY });

    const xStart = Math.min(dragStart.x, offsetX);
    const yStart = Math.min(dragStart.y, offsetY);
    const xEnd = Math.max(dragStart.x, offsetX);
    const yEnd = Math.max(dragStart.y, offsetY);

    const selected = seatCoordinates.filter((seat) => {
      const { scaledX, scaledY } = calculatePosition(seat.x, seat.y);
      return (
        scaledX >= xStart &&
        scaledX <= xEnd &&
        scaledY >= yStart &&
        scaledY <= yEnd
      );
    });

    setSelectedSeats((prev) => [...new Set([...prev, ...selected])]);
  };

  const handleAddGrade = () => {
    if (!currentGrade || !currentPrice || selectedSeats.length === 0) {
      alert("등급명, 가격을 입력하고 좌석을 선택하세요.");
      return;
    }

    const newGrade = {
      grade: {
        grade: currentGrade,
        price: parseFloat(currentPrice),
        seats: selectedSeats.map((seat) => seat.seatId),
      },
      color: getRandomColor(),
      seats: selectedSeats,
    };

    setGrades((prev) => [...prev, newGrade]);
    setSelectedSeats([]);
    setCurrentGrade("");
    setCurrentPrice("");
  };

  const handleRemoveGrade = (grade: string) => {
    setGrades((prev) => prev.filter((g) => g.grade.grade !== grade));
  };

  const handleSaveGrades = () => {
    const formattedGrades = grades.map(({ grade }) => grade);
    onChange({ seats: formattedGrades });

    alert("좌석 설정이 저장되었습니다.");
  };

  const getSeatGrade = (seat: SeatInfo) =>
    grades.find((grade) => grade.seats.some((s) => s.seatId === seat.seatId));

  if (loading) return <p>좌석 데이터를 불러오는 중입니다...</p>;
  if (error) return <p className="text-red-500">{error}</p>;

  return (
    <div className="flex flex-col p-4 bg-white rounded-xl shadow-md">
      <h2 className="text-lg font-semibold mb-4">좌석 설정</h2>
      {stageImg ? (
        <div className="flex gap-4">
          <div
            ref={containerRef}
            className="relative border border-gray-300 h-[600px] w-[700px]"
            onMouseDown={handleMouseDown}
            onMouseUp={handleMouseUp}
            onMouseMove={handleMouseMove}
            onDragStart={(e) => e.preventDefault()}
          >
            <img
              src={stageImg}
              alt="Stage Map"
              className="top-0 left-0 w-full h-full object-fill no-drag pointer-events-none"
            />
            {seatCoordinates.map((seat) => {
              const seatGrade = getSeatGrade(seat);
              const isSelected = selectedSeats.some(
                (selected) => selected.seatId === seat.seatId,
              );
              const { scaledX, scaledY } = calculatePosition(seat.x, seat.y);

              return (
                <button
                  key={seat.seatId}
                  className={`absolute w-[10px] h-[10px] border ${
                    isSelected ? "bg-red-500" : seatGrade ? "" : "bg-white"
                  }`}
                  style={{
                    backgroundColor: seatGrade?.color || "",
                    borderColor: seatGrade?.color || "gray",
                    top: `${scaledY}px`,
                    left: `${scaledX}px`,
                    transform: "translate(-50%, -50%)",
                  }}
                  onClick={() => toggleSeatSelection(seat)}
                  disabled={!!seatGrade}
                />
              );
            })}
            {isDragging && dragStart && dragEnd && (
              <div
                className="absolute bg-blue-200 opacity-50 border border-blue-500"
                style={{
                  top: `${Math.min(dragStart.y, dragEnd.y)}px`,
                  left: `${Math.min(dragStart.x, dragEnd.x)}px`,
                  width: `${Math.abs(dragEnd.x - dragStart.x)}px`,
                  height: `${Math.abs(dragEnd.y - dragStart.y)}px`,
                  pointerEvents: "none",
                }}
              ></div>
            )}
          </div>
          <div className="w-[250px] p-4 border-l border-gray-300">
            <h3 className="text-md font-semibold mb-4">등급 설정</h3>
            <div className="mb-4">
              <input
                type="text"
                placeholder="등급명 입력"
                value={currentGrade}
                onChange={(e) => setCurrentGrade(e.target.value)}
                className="w-full mb-2 border border-gray-300 rounded px-2 py-1"
              />
              <input
                type="number"
                placeholder="가격 입력"
                value={currentPrice}
                onChange={(e) => setCurrentPrice(e.target.value)}
                className="w-full border border-gray-300 rounded px-2 py-1"
              />
              <button
                onClick={handleAddGrade}
                className="w-full mt-2 bg-blue-500 text-white px-4 py-2 rounded"
              >
                설정
              </button>
            </div>
            <div className="mb-4 overflow-y-auto">
              <h4 className="text-sm font-semibold mb-2">
                선택(열-행) {selectedSeats.length}석
              </h4>
              <ul className="h-24 overflow-y-auto border border-gray-300 rounded p-1">
                {selectedSeats.map((seat) => (
                  <li key={seat.seatId} className="text-sm">
                    {` ${seat.seatRow}-${seat.seatCol}`}
                  </li>
                ))}
              </ul>
            </div>
            <div>
              <h4 className="text-[15px] font-semibold mb-2">등급 목록</h4>
              {grades.map(({ grade, color }) => (
                <div
                  key={grade.grade}
                  className="flex items-center justify-between border border-gray-300 rounded p-1 mb-1"
                >
                  <div className="flex items-center gap-1">
                    <span
                      className="w-2.5 h-2.5 rounded-full"
                      style={{ backgroundColor: color }}
                    ></span>
                    <span className="text-[13px]">{`${grade.grade} ${grade.seats.length}석`}</span>
                  </div>
                  <span className="text-[13px]">{`${grade.price.toLocaleString()}원`}</span>
                  <button
                    onClick={() => handleRemoveGrade(grade.grade)}
                    className="ml-2 text-red-500 text-[10px]"
                  >
                    삭제
                  </button>
                </div>
              ))}
            </div>
            <button
              onClick={handleSaveGrades}
              className="w-full mt-4 bg-green-500 text-white px-4 py-2 rounded"
            >
              저장
            </button>
          </div>
        </div>
      ) : (
        <p className="text-gray-500">공연장을 선택하면 좌석 맵이 표시됩니다.</p>
      )}
    </div>
  );
};

export default EditSeatSetting;
