from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict
import uvicorn
from fastapi.middleware.cors import CORSMiddleware
from llama_cpp import Llama
import logging
from datetime import datetime

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(f'deepseek_server_{datetime.now().strftime("%Y%m%d")}.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

app = FastAPI()

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

logger.info("모델 로딩 중...")
try:
    model_path = "C:/java/deepseek-llm-7b-chat.Q4_K_M.gguf/deepseek-llm-7b-chat-q4_k_m-imat.gguf"
    llm = Llama(
        model_path=model_path,
        n_ctx=4096,
        n_gpu_layers=-1,
        n_batch=512,
        n_threads=8,
        use_mmap=True,
        verbose=True
    )
    logger.info("모델 로딩 완료!")
except Exception as e:
    logger.error(f"모델 로딩 실패: {str(e)}")
    raise

SYSTEM_PROMPT = """You are Claude, an AI assistant created by Anthropic. Always be helpful, harmless, and honest. Respond in the same language as the user's question. If you're unsure about something, say so. Avoid harmful or inappropriate content."""

class ChatRequest(BaseModel):
    message: str
    history: List[Dict[str, str]] = []

@app.post("/chat")
async def chat(request: ChatRequest):
    try:
        logger.info(f"채팅 요청 받음: {request}")
        
        # 대화 히스토리를 포함한 프롬프트 구성
        messages = [{"role": "system", "content": SYSTEM_PROMPT}]
        
        # 이전 대화 내용 추가 - 형식 수정
        if request.history:
            for msg in request.history:
                # 로그로 각 메시지 확인
                logger.info(f"처리중인 히스토리 메시지: {msg}")
                
                # 딕셔너리 키 검사 및 값이 비어있지 않은지 확인
                if "user" in msg and msg["user"].strip():
                    messages.append({
                        "role": "user",
                        "content": msg["user"].strip()
                    })
                if "assistant" in msg and msg["assistant"].strip():
                    messages.append({
                        "role": "assistant",
                        "content": msg["assistant"].strip()
                    })

        # 현재 메시지가 비어있지 않은지 확인
        if not request.message.strip():
            raise HTTPException(status_code=400, detail="메시지가 비어있습니다")
            
        # 현재 메시지 추가
        messages.append({
            "role": "user",
            "content": request.message.strip()
        })

        logger.info(f"최종 프롬프트 메시지: {messages}")

        # 응답 생성 - 파라미터 수정
        response = llm.create_chat_completion(
            messages=messages,
            temperature=0.7,
            top_p=0.95,
            max_tokens=2048,
            repeat_penalty=1.15,
            stream=False
        )

        
        logger.info(f"Raw 응답: {response}")
        
        if (not response or 
            'choices' not in response or 
            not response['choices'] or 
            'message' not in response['choices'][0] or 
            'content' not in response['choices'][0]['message']):
            logger.error("잘못된 응답 형식")
            raise HTTPException(status_code=500, detail="모델이 올바른 형식의 응답을 생성하지 못했습니다")
            
        assistant_response = response['choices'][0]['message']['content']
        logger.info(f"응답 생성 완료: {assistant_response}")
        
        if not assistant_response or assistant_response.strip() == "":
            logger.error("빈 응답이 생성되었습니다")
            raise HTTPException(status_code=500, detail="빈 응답이 생성되었습니다")
        
        return {
            "status": "success",
            "response": assistant_response,
            "messages": messages  # 전체 대화 컨텍스트 반환
        }

    except Exception as e:
        logger.error(f"에러 발생: {str(e)}")
        logger.exception("상세 에러:")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    logger.info("서버 시작")
    uvicorn.run(app, host="0.0.0.0", port=8000)