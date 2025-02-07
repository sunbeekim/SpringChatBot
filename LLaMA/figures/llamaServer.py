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
        system_prompt = """당신은 지식이 풍부한 AI 어시스턴트입니다. 
다음 규칙을 반드시 따라주세요:
1. 항상 한국어로 명확하게 응답합니다.
2. 이전 대화 맥락을 고려하여 일관성 있게 답변합니다.
3. 질문과 관련 없는 내용은 답변하지 않습니다.
4. 전문적인 내용은 정확하고 자세하게 설명합니다.
5. 모르는 내용에 대해서는 "잘 모르겠습니다"라고 답변합니다."""

        # 대화 히스토리를 포함한 프롬프트 구성
        full_prompt = f"<system>{system_prompt}</system>\n"
        
        # 이전 대화 내용 추가 (최근 4개 메시지만 사용)
        for msg in request.history[-4:]:
            full_prompt += f"<user>{msg['user']}</user>\n"
            if 'assistant' in msg:
                full_prompt += f"<assistant>{msg['assistant']}</assistant>\n"
        
        # 현재 메시지 추가
        full_prompt += f"<user>{request.message}</user>\n<assistant>"
        
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