// src/api/testAPI.js
import axios from 'axios';

// Spring API Base URL 설정
const SPRING_API_BASE_URL = 'http://sunbee.world:8081/api';

export const CrawlRequest = async (url, word, deep, state, taskId) => {
  const requestData = { url, word, deep, state, taskId };
  //const headers = { Authorization: `Bearer ${state?.token}` };
  //const requestUrl = `${SPRING_API_BASE_URL}/crawling`;

  //   try {
  //     const response = await axios.post(requestUrl, requestData, { headers });

  //     return response;
  try {
    const response = await axios.post(`${SPRING_API_BASE_URL}/crawling`, requestData);
    return response;
  } catch (error) {
    throw error;
  }
};

export const CrawlStop = async (taskId) => {
  const requestData = { taskId };
  const requestUrl = `${SPRING_API_BASE_URL}/stopTask`;

  try {
    const response = await axios.post(requestUrl, requestData);

    return response;
  } catch (error) {
    throw error;
  }
};

export const TxtDownload = async (filePath) => {
  const requestData = { filePath };
  const requestUrl = `${SPRING_API_BASE_URL}/files/download`;

  try {
    const response = await axios.post(requestUrl, requestData, {
      responseType: 'blob' // 바이너리 데이터 처리
    });

    // 응답 확인
    console.log('File Download Response:', response);

    // Blob 생성 확인
    const blob = new Blob([response.data], { type: 'text/plain' }); // 텍스트 파일로 명시
    console.log('Blob 객체:', blob);

    // 파일 다운로드 처리
    const downloadLink = document.createElement('a');
    downloadLink.href = URL.createObjectURL(blob);
    downloadLink.download = filePath.split('/').pop(); // 파일 이름 추출
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);

    return response.data; // 반환된 데이터
  } catch (error) {
    throw error;
  }
};
