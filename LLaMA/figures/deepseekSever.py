from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForCausalLM
import torch
from typing import List, Dict
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
# 로컬 모델 경로 사용
model_path = "C:/java/DeepSeek"
tokenizer = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True)
model = AutoModelForCausalLM.from_pretrained(
    model_path,
    torch_dtype=torch.float16,
    device_map="auto",
    trust_remote_code=True
)
print("모델 로딩 완료!")

class ChatRequest(BaseModel):
    messages: List[Dict[str, str]]

@app.post("/v1/chat/completions")
async def chat(request: ChatRequest):
    try:
        # 입력 메시지 처리
        conversation = ""
        for msg in request.messages:
            role = msg["role"]
            content = msg["content"]
            if role == "user":
                conversation += f"Human: {content}\nAssistant: "
            elif role == "assistant":
                conversation += f"{content}\n"

        # 입력 토큰화
        inputs = tokenizer(conversation, return_tensors="pt").to(model.device)
        
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
        
        # 응답 디코딩
        response = tokenizer.decode(outputs[0], skip_special_tokens=True)
        
        # 마지막 Assistant 응답만 추출
        response = response.split("Assistant: ")[-1].strip()
        
        return {
            "choices": [{
                "message": {
                    "role": "assistant",
                    "content": response
                }
            }]
        }
    except Exception as e:
        print(f"에러 발생: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)