from fastapi import FastAPI, File, UploadFile, HTTPException, Form
from fastapi.middleware.cors import CORSMiddleware
from ultralytics import YOLO
import uvicorn
from PIL import Image
import io
import os
import json
from dotenv import load_dotenv
from google import genai
from google.genai import types

load_dotenv()

app = FastAPI(title="Dental AI Backend")

# Enable CORS for React frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize Gemini Client if API key is found
gemini_client = None
if os.getenv("GEMINI_API_KEY"):
    gemini_client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

# Load Models
models_dir = os.path.join(os.path.dirname(__file__), 'models')

print("⏳ Loading AI Models...")
try:
    implant_model = YOLO(os.path.join(models_dir, 'implant_detection.pt'))
    panoramic_model = YOLO(os.path.join(models_dir, 'panoramic_caries.pt'))
    mandibular_model = YOLO(os.path.join(models_dir, 'mandibular_canal.pt'))
    sinus_model = YOLO(os.path.join(models_dir, 'maxillary_sinus.pt'))
    print("✅ All AI models loaded successfully!")
except Exception as e:
    print(f"⚠️ Error loading models: {e}")

@app.get("/")
def read_root():
    return {"status": "online", "message": "Dental AI Backend is running"}

@app.post("/analyze/panoramic")
async def analyze_panoramic(file: UploadFile = File(...)):
    return await _process_image(file, panoramic_model)

@app.post("/analyze/implant")
async def analyze_implant(file: UploadFile = File(...)):
    return await _process_image(file, implant_model)

@app.post("/analyze/mandibular")
async def analyze_mandibular(file: UploadFile = File(...)):
    return await _process_image(file, mandibular_model)

@app.post("/analyze/sinus")
async def analyze_sinus(file: UploadFile = File(...)):
    return await _process_image(file, sinus_model)

async def _process_image(file: UploadFile, model):
    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
        
        # Run inference
        results = model.predict(image)
        
        # Format results
        detections = []
        for result in results:
            boxes = result.boxes
            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                conf = float(box.conf[0])
                cls = int(box.cls[0])
                name = result.names[cls]
                
                detections.append({
                    "class": name,
                    "confidence": conf,
                    "bbox": [round(x1, 2), round(y1, 2), round(x2, 2), round(y2, 2)]
                })
                
        return {"status": "success", "detections": detections}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/analyze/gemini-survival")
async def analyze_gemini_survival(
    file: UploadFile = File(...),
    patient_data: str = Form(...)
):
    if not gemini_client:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY not configured on backend server.")

    try:
        contents = await file.read()
        image = Image.open(io.BytesIO(contents)).convert("RGB")
        patient_json = json.loads(patient_data)

        prompt = f"""
        You are an expert AI dental radiologist and implant specialist.
        Analyze this dental scan alongside the following patient data:
        {json.dumps(patient_json, indent=2)}

        Provide a realistic, dynamic Implant Survival Prediction based on anatomical factors in the image and the exact details in the patient's medical history. 
        CRITICAL RULE: DO NOT invent or hallucinate risk factors. Only list risk factors and success factors that apply directly to THIS patient's actual provided data or the image. 

        To achieve 100% accuracy, you MUST calculate the `survival_probability` using this strict formula:
        1. Start with a Base Survival of 98%.
        2. If the patient has a history of smoking, subtract 12%.
        3. If the patient has diabetes, subtract 8%.
        4. If the patient has poor bone density visible in the scan or history, subtract 10%.
        5. The `failure_risk` must mathematically equal exactly (100 - `survival_probability`).
        
        Output ONLY a JSON object matching this schema exactly (use appropriate labels based on real data):
        {{
            "survival_probability": 84, 
            "failure_risk": 16, 
            "confidence": 91, 
            "risk_factors": [
                {{"label": "Actual Risk Factor 1", "risk": "20%", "level": "LOW", "color": "success"}}
            ],
            "success_factors": [
                {{"factor": "Actual Success Factor 1", "impact": "+18%", "pos": true}}
            ],
            "action_items": [
                {{"text": "Relevant Action Item", "level": "MEDIUM", "type": "warning"}}
            ],
            "narrative": [
                "Survival probability is estimated strictly based on visible bone quality and exact medical history.",
                "Recommend strict follow-up."
            ]
        }}
        Do not include markdown code blocks. Output pure JSON only.
        """

        response = gemini_client.models.generate_content(
            model='gemini-2.5-flash',
            contents=[image, prompt],
            config=types.GenerateContentConfig(
                temperature=0.0,
                top_k=1,
                top_p=0.1
            )
        )

        # Parse JSON safely
        raw_text = response.text.replace("```json", "").replace("```", "").strip()
        parsed_data = json.loads(raw_text)
        return {"status": "success", "data": parsed_data}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

from pydantic import BaseModel
from typing import List, Dict, Any, Optional

class ChatMessage(BaseModel):
    role: str
    content: str

class ChatRequest(BaseModel):
    patient_data: Dict[str, Any]
    messages: List[ChatMessage]

@app.post("/chat/personalized")
async def chat_personalized(request: ChatRequest):
    if not gemini_client:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY not configured on backend server.")
        
    try:
        system_instruction = f"""
You are an expert AI Dental Assistant. You are currently looking at the file of this specific patient:
{json.dumps(request.patient_data, indent=2)}

Your primary job is to answer questions about this patient context. However, you are also an expert in dentistry and radiology, so you can freely answer general dental questions (like "What is CBCT?") even if they are not in the patient's record.
Keep your answers concise, helpful, and professional.
"""
        
        conversation = ""
        for msg in request.messages:
            conversation += f"{msg.role.capitalize()}: {msg.content}\n"
            
        prompt = system_instruction + "\n\n" + "Conversation History:\n" + conversation + "\nAssistant:"

        response = gemini_client.models.generate_content(
            model='gemini-2.5-flash',
            contents=prompt
        )

        return {"status": "success", "reply": response.text.strip()}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8000))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
