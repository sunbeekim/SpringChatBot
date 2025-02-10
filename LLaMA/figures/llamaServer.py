from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForCausalLM
import torch
from typing import Dict
import uvicorn
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

print("모델 로딩 중...")
# 오픈소스 모델 사용 (라이센스 제한 없음)
model_name = "TinyLlama/TinyLlama-1.1B-Chat-v1.0"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForCausalLM.from_pretrained(
    model_name,
    torch_dtype=torch.float16,
    device_map="auto",
)
print("모델 로딩 완료!")

class ChatRequest(BaseModel):
    message: str
    history: list = []  # 대화 히스토리 추가

@app.post("/chat")
async def chat(request: ChatRequest) -> Dict[str, str]:
    try:
        # 더 구체적인 시스템 프롬프트 설정
        system_prompt = """You are a knowledgeable AI assistant. 

1. Always respond clearly in Korean.
2. Answer consistently, taking into account the context of the previous conversation.
3. Content unrelated to the question will not be answered.
4. Professional content is explained accurately and in detail.
5. Answer "I don't know" to questions you don't know."""

        # 대화 히스토리를 포함한 프롬프트 구성
        full_prompt = f"<system>{system_prompt}</system>\n"
        
        print("\n=== 대화 히스토리 시작 ===")
        print(f"히스토리 메시지 수: {len(request.history)}")
        
        # 이전 대화 내용 추가 (최근 4개 메시지만 사용)
        for i, msg in enumerate(request.history[-4:]):
            print(f"\n메시지 {i+1}:")
            print(f"사용자: {msg['user']}")
            if 'assistant' in msg:
                print(f"어시스턴트: {msg['assistant']}")
            full_prompt += f"<user>{msg['user']}</user>\n"
            if 'assistant' in msg:
                full_prompt += f"<assistant>{msg['assistant']}</assistant>\n"
        
        # 현재 메시지 추가
        print(f"\n현재 메시지: {request.message}")
        full_prompt += f"<user>{request.message}</user>\n<assistant>"
        
        print("\n=== 최종 프롬프트 ===")
        print(full_prompt)
        print("=====================\n")

        # 입력 메시지 토큰화
        inputs = tokenizer(full_prompt, return_tensors="pt").to(model.device)
        
        # 생성 파라미터 설정
        generation_config = {
            "max_length": 2048,
            "temperature": 0.7,
            "top_p": 0.95,
            "repetition_penalty": 1.15,
            "do_sample": True
        }
        
        # 응답 생성
        outputs = model.generate(
            **inputs,
            **generation_config,
            pad_token_id=tokenizer.eos_token_id
        )
        
        # 응답 디코딩 및 프롬프트 제거
        response = tokenizer.decode(outputs[0], skip_special_tokens=True)
        response = response.split("<assistant>")[-1].strip()
        
        return {"response": response}
    except Exception as e:
        print(f"에러 발생: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001) 