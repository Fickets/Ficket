import React, { useState } from 'react';
import { ImageUploaderProps } from '../../types/edit';

const ImageUploader = ({
  title,
  aspectRatio,
  onChange,
  initialImage,
}: ImageUploaderProps) => {
  const [selectedImage, setSelectedImage] = useState<string | null>(
    initialImage
  );

  const handleImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files[0]) {
      const file = event.target.files[0];
      const reader = new FileReader();

      // FileReader를 사용하여 이미지 파일을 읽어오기
      reader.onload = () => {
        if (reader.readyState === 2) {
          setSelectedImage(reader.result as string); // Base64 URL로 변환하여 상태에 저장
          onChange(file); // 선택된 파일을 상위 컴포넌트로 전달
        }
      };

      reader.readAsDataURL(file);
    }
  };

  const handleRemoveImage = () => {
    setSelectedImage(null);
    onChange(null); // 이미지 삭제 시 상위 컴포넌트에 null 전달
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-md border border-gray-200 flex flex-col items-center font-sans">
      <h3 className="text-xl font-semibold mb-4 self-start">{title}</h3>
      <div
        className="flex items-center justify-center rounded overflow-hidden relative w-full"
        style={{
          maxWidth: '400px', // 너비를 좀 더 넓게 설정해 박스가 여백을 덜 남기도록
          height: 'auto',
          aspectRatio: `${aspectRatio}`,
        }}
      >
        {selectedImage ? (
          <div className="relative w-full h-full">
            {/* 이미지 미리보기 섹션 */}
            <img
              src={selectedImage}
              alt="Uploaded Preview"
              className="object-cover w-full h-full" // 이미지가 박스를 꽉 채우도록 설정
            />
            {/* 수정 및 삭제 버튼 */}
            <div className="absolute bottom-2 left-1/2 transform -translate-x-1/2 flex space-x-2">
              <label className="bg-blue-500 text-white px-2 py-1 rounded-md cursor-pointer text-sm">
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
                className="bg-red-500 text-white px-2 py-1 rounded-md text-sm"
              >
                삭제
              </button>
            </div>
          </div>
        ) : (
          // 이미지 업로드 버튼 섹션
          <label className="flex flex-col items-center justify-center h-full w-full cursor-pointer">
            <span className="bg-blue-500 text-white px-4 py-2 rounded-md">
              이미지 업로드 버튼
            </span>
            <input
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              className="hidden"
            />
          </label>
        )}
      </div>
    </div>
  );
};

export default ImageUploader;
