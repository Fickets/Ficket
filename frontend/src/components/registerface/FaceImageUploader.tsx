import React, { useState } from "react";

type ImageUploaderProps = {
  aspectRatio: number;
  onChange: (file: File | null) => void;
};

function FaceImageUploader({ aspectRatio, onChange }: ImageUploaderProps) {
  const [selectedImage, setSelectedImage] = useState<string | null>(null);

  const handleImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      const file = event.target.files[0];
      const reader = new FileReader();

      reader.onload = () => {
        if (reader.readyState === 2) {
          setSelectedImage(reader.result as string);
          onChange(file); // 부모로 이미지 파일 전달
        }
      };

      reader.readAsDataURL(file);
    }
  };

  const handleRemoveImage = () => {
    setSelectedImage(null);
    onChange(null); // 이미지 삭제 시 부모에게 알림
  };

  return (
    <div
      className="relative w-[150px] h-[210px] border border-gray-500 flex items-center justify-center"
      style={{
        aspectRatio: `${aspectRatio}`,
      }}
    >
      {selectedImage ? (
        <div className="relative w-full h-full">
          <img
            src={selectedImage}
            alt="Uploaded Preview"
            className="object-cover w-full h-full"
          />
          <div className="absolute bottom-2 ml-5 flex flex-row space-x-2">
            <label className="bg-blue-500 text-white px-3 py-1 rounded-md cursor-pointer text-sm">
              수정
              <input
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                className="hidden"
              />
            </label>
            <button
              onClick={handleRemoveImage}
              className="bg-red-500 text-white px-3 py-1 rounded-md text-sm"
            >
              삭제
            </button>
          </div>
        </div>
      ) : (
        <label className="flex flex-col items-center justify-center h-full w-full cursor-pointer">
          <span className="bg-[#A8A8A8] text-white px-4 py-2">파일 선택</span>
          <input
            type="file"
            accept="image/*"
            onChange={handleImageChange}
            className="hidden"
          />
        </label>
      )}
    </div>
  );
}

export default FaceImageUploader;
