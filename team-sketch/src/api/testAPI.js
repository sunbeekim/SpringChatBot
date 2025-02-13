// src/api/testAPI.js
import axios from 'axios';

// Spring API Base URL 설정
const SPRING_API_BASE_URL = 'http://sunbee.world:8080/api';

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
//===============================================================================================//

// LLaMA 채팅 API
export const LlamaChat = async (message) => {
  try {
    const response = await axios.post(`${SPRING_API_BASE_URL}/llama/chat`, { message });
    return response.data; 
  } catch (error) {
    throw error;
  }
};

// DeepSeek-R1 채팅 API
export const DeepSeekChat = async (message) => {
  try {
    const response = await axios.post(`${SPRING_API_BASE_URL}/deepseek/chat`, { message });
    return response.data; 
  } catch (error) {
    throw error;
  }
};

// ChatBot API
export const ChatBotChat = async (message) => {
  try {
    const response = await axios.post(`${SPRING_API_BASE_URL}/chatbot/chat`, { message });
    return response.data;
  } catch (error) {
    throw error;
  }
};

// Cloud ChatBot API
export const CloudChatBotChat = async (message) => {
  try {
    const response = await axios.post(`${SPRING_API_BASE_URL}/cloudchatbot/chat`, { message });
    return response.data;
  } catch (error) {
    throw error;
  }
};


//===============================================================================================//
// 규칙 목록 조회
export const fetchRulesList = async (roleId, username) => {
  try {
    const response = await axios.get(`${SPRING_API_BASE_URL}/rules`, {
      params: { roleId, username }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 규칙 추가
export const addRule = async (ruleData, roleId, username) => {
  try {
    const response = await axios.post(`${SPRING_API_BASE_URL}/rules`, {
      ...ruleData,
      roleId,
      username
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 규칙 수정
export const updateRule = async (id, ruleData, roleId, username) => {
  try {
    const response = await axios.put(`${SPRING_API_BASE_URL}/rules/${id}`, {
      ...ruleData,
      roleId,
      username,
      lastModifiedBy: username,
      lastModifiedAt: new Date().toISOString()
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 규칙 삭제
export const deleteRule = async (id, roleId, username) => {
  try {
    const response = await axios.delete(`${SPRING_API_BASE_URL}/rules/${id}`, {
      params: { roleId, username }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 규칙 적용
export const applyRule = async (ruleId, username) => {
  try {
    const response = await axios.put(`${SPRING_API_BASE_URL}/rules/${ruleId}/apply`, null, {
      params: { username }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 규칙 적용 해제
export const unapplyRule = async (ruleId, username) => {
    try {
        const response = await axios.put(`${SPRING_API_BASE_URL}/rules/${ruleId}/unapply`, null, {
            params: { username }
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};

// 적용된 규칙 목록 조회
export const getAppliedRules = async () => {
  try {
    const response = await axios.get(`${SPRING_API_BASE_URL}/rules/applied`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const CloudOCR = async (formData) => {
  try {
    const response = await axios.post(`${SPRING_API_BASE_URL}/ocr/process`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};
