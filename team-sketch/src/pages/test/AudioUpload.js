import React, { useState } from "react";
import axios from "axios";

const AudioUpload = () => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [uploadStatus, setUploadStatus] = useState("");

  // 파일 선택 핸들러
  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file && file.type.startsWith("audio/")) {
      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file)); // 오디오 미리듣기 설정
    } else {
      setUploadStatus("오디오 파일만 업로드할 수 있습니다.");
    }
  };

  // 파일 업로드 핸들러
  const handleUpload = async () => {
    if (!selectedFile) {
      setUploadStatus("파일을 선택해주세요.");
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);

    try {
      const response = await axios.post("http://localhost:8080/upload/audio", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      setUploadStatus(`업로드 성공: ${response.data.audioUrl}`);
    } catch (error) {
      console.error("업로드 실패:", error);
      setUploadStatus("업로드 실패");
    }
  };

  return (
    <div>
      <h2>음성 파일 업로드</h2>
      <input type="file" accept="audio/*" onChange={handleFileChange} />
      {previewUrl && (
        <audio controls>
          <source src={previewUrl} type={selectedFile?.type} />
          브라우저가 오디오 태그를 지원하지 않습니다.
        </audio>
      )}
      <button onClick={handleUpload}>업로드</button>
      {uploadStatus && <p>{uploadStatus}</p>}
    </div>
  );
}

export default AudioUpload;
