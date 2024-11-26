import FaceImageUploader from './FaceImageUploader';

type PictureBoxProps = {
  onChange: (file: File | null) => void;
};

function PictureBox({ onChange }: PictureBoxProps) {
  return (
    <div className="w-full max-w-[400px] h-[400px] flex flex-col items-center border border-white bg-[#FFFFFF] p-4">
      <h3 className="font-bold text-lg text-gray-800">안내사항 및 주의사항</h3>
      <p className="mt-2 text-sm text-gray-700">
        <ol className="list-decimal list-inside">
          <li>사진은 정면을 바라보며 자연스러운 표정을 유지해주세요.</li>
          <li>지나친 보정이나 필터 사용은 지양해 주세요.</li>
          <li>얼굴이 가리지 않도록 하며, 지나치게 어두운 사진은 피해주세요.</li>
        </ol>
      </p>
      <div className="flex justify-between mt-4 w-full">
        <div className="relative w-[150px] h-[210px] border border-gray-500">
          <h2 className="absolute top-2 left-1/2 transform -translate-x-1/2 text-white font-bold text-sm bg-black bg-opacity-50 px-2 py-1 rounded">
            사진 가이드
          </h2>
          <img
            src="https://ficket-event-content.s3.ap-northeast-2.amazonaws.com/faces/face.png"
            alt="사진 가이드"
            className="w-full h-full object-cover"
          />
        </div>
        <FaceImageUploader aspectRatio={150 / 210} onChange={onChange} />
      </div>
    </div>
  );
}

export default PictureBox;
