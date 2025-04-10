import { Editor } from '@tinymce/tinymce-react';
import { BlobInfo, TinyEditorProps } from '../../types/register';
import { uploadImage } from '../../service/register/api';

const handleImageUpload = (
  blobInfo: BlobInfo,
  progress: (percent: number) => void
): Promise<string> => {
  return new Promise((resolve, reject) => {
    const formData = new FormData();
    formData.append('image', blobInfo.blob(), blobInfo.filename());

    uploadImage(formData, (event) => {
      if (event.total) {
        progress(Math.round((event.loaded / event.total) * 100)); // 업로드 진행률
      }
    })
      .then(resolve)
      .catch((error) => {
        console.error('Image upload failed:', error);
        reject('Image upload failed');
      });
  });
};

const TinyEditor = ({ onChange }: TinyEditorProps) => {
  const apiKey = import.meta.env.VITE_TINYMCE_API_KEY;
  if (!apiKey) {
    throw new Error('TinyMCE API Key is not defined');
  }

  return (
    <>
      <h3 className="text-xl font-semibold text-gray-800 mb-6">
        공연 상세 정보 입력
      </h3>
      <Editor
        apiKey={apiKey}
        onEditorChange={onChange} // 부모로 변경된 내용 전달
        init={{
          placeholder: '행사에 대해 소개해주세요.',
          height: 700,
          menubar: true,
          plugins: 'image link code table lists',
          paste_data_images: true,
          automatic_uploads: true,
          images_upload_handler: handleImageUpload,
          toolbar:
            'undo redo | image | styleselect | bold italic | alignleft aligncenter alignright alignjustify | outdent indent',
          content_style: `
            body {
              font-family: 'Noto Sans KR', sans-serif;
              line-height: 1.5;
            }
            img { max-width: 100%; height: auto; }
          `,
          resize: false,
          images_file_types: 'jpg,jpeg,png',
          block_unsupported_drop: true,
        }}
      />
    </>
  );
};

export default TinyEditor;
