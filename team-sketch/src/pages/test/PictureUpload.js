import React, { useState } from "react";
import { CloudOCR } from "../../api/testAPI";

const PictureUpload = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [uploadStatus, setUploadStatus] = useState({ text: "", status: "" });

  // 파일 선택 핸들러
  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file)); // 이미지 미리보기 설정
    }
  };

  // 파일 업로드 핸들러
  const handleUpload = async () => {
    if (!selectedFile) {
      setUploadStatus({ text: "파일을 선택해주세요.", status: "error" });
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);

    try {
      const response = await CloudOCR(formData);
      console.log("OCR 응답:", response);

      // 응답이 객체인지 확인 후 상태 업데이트
      if (response && typeof response === "object") {
        setUploadStatus({ text: response.text, status: response.status });
      } else {
        setUploadStatus({ text: "OCR 응답 오류", status: "error" });
      }
    } catch (error) {
      console.error("업로드 실패:", error);
      setUploadStatus({ text: "업로드 실패", status: "error" });
    }
  };

  return (
    <div>
      <h2>사진 업로드</h2>
      <input type="file" accept="image/*" onChange={handleFileChange} />
      {previewUrl && <img src={previewUrl} alt="미리보기" style={{ width: "200px", marginTop: "10px" }} />}
      
      <button onClick={handleUpload}>업로드</button>

      {uploadStatus.status && <p><strong>업로드 상태:</strong> {uploadStatus.status}</p>}
      
      {uploadStatus.text && (
        <div>
          <h2>OCR 결과</h2>
          <p><strong>OCR 결과:</strong> {uploadStatus.text}</p>
        </div>
      )}
    </div>
  );
};

export default PictureUpload;
