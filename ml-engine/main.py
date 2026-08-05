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

        # Run YOLOv8 detections for features
        yolo_findings = []
        try:
            if implant_model:
                r1 = implant_model.predict(image, verbose=False)
                for res in r1:
                    for b in res.boxes:
                        yolo_findings.append(f"Implant/Structure: {res.names[int(b.cls[0])]} (Conf: {float(b.conf[0]):.2f})")
            if sinus_model:
                r2 = sinus_model.predict(image, verbose=False)
                for res in r2:
                    for b in res.boxes:
                        yolo_findings.append(f"Maxillary Sinus Region: {res.names[int(b.cls[0])]} (Conf: {float(b.conf[0]):.2f})")
        except Exception as ye:
            print(f"YOLO detection warning: {ye}")

        findings_text = "\n".join(yolo_findings) if yolo_findings else "No abnormal bone or sinus anomalies detected by YOLO vision detectors."

        prompt = f"""
        You are an expert AI dental radiologist and implant specialist.
        First, inspect the uploaded image carefully. Determine if the image is a valid dental scan (CBCT 3D scan, Panoramic Radiograph, Periapical X-Ray, or 3D Jaw/Teeth scan).
        
        IF THE IMAGE IS NOT A DENTAL SCAN OR TEETH/JAW RADIOGRAPH (for example: a phone screenshot, chat dialog, photo of a person/car/building/document/object):
        Output ONLY this JSON object:
        {{
            "is_valid_dental_scan": false,
            "error": "Not a valid dental scan"
        }}

        IF THE IMAGE IS A VALID DENTAL SCAN:
        Analyze this scan alongside the patient profile:
        Patient Data: {json.dumps(patient_json, indent=2)}
        YOLO Vision Model Detections: {findings_text}

        CRITICAL REQUIREMENT: Generate UNIQUE, DYNAMIC, PATIENT-SPECIFIC and SCAN-SPECIFIC data. DO NOT output fixed numbers like 84% or static text for every patient.
        
        1. Calculate `survival_probability` (an integer between 65 and 98) dynamically based on patient age, smoking status, medical history, and visible bone density/structures in this specific image.
        2. Set `failure_risk` = (100 - `survival_probability`).
        3. Set `confidence` = (an integer between 89 and 97 based on image resolution and clarity).
        4. Generate 3 to 4 custom `risk_factors` tailored to this specific patient (e.g. Alveolar Ridge Height, Bone Density, Periodontal Status, Sinus Clearance) with dynamic percentage numbers.
        5. Generate 2 to 3 `success_factors` (positive clinical indicators for this patient).
        6. Generate 3 to 4 custom `action_items` with priority levels (HIGH, MEDIUM, LOW).
        7. Generate a comprehensive 3-paragraph `narrative` summarizing the radiologist visual findings for THIS specific patient and image.

        Output ONLY a JSON object matching this schema:
        {{
            "is_valid_dental_scan": true,
            "survival_probability": 92, 
            "failure_risk": 8, 
            "confidence": 94, 
            "risk_factors": [
                {{"label": "Sub-Sinus Bone Height", "risk": "18%", "level": "LOW", "color": "success"}},
                {{"label": "Trabecular Density", "risk": "25%", "level": "MEDIUM", "color": "warning"}}
            ],
            "success_factors": [
                {{"factor": "Favorable Age Profile", "impact": "+15%", "pos": true}},
                {{"factor": "Dense Cortical Plate", "impact": "+12%", "pos": true}}
            ],
            "action_items": [
                {{"text": "Evaluate sub-sinus ridge height at Tooth #14 position prior to fixture placement.", "level": "HIGH", "type": "danger"}},
                {{"text": "Maintain 6-month clinical and radiographic follow-up to evaluate osteointegration.", "level": "MEDIUM", "type": "warning"}}
            ],
            "narrative": [
                "Detailed radiologist report for this patient's scan...",
                "Anatomical assessment of bone quality and cortical thickness...",
                "Recommendations for optimal implant fixture dimensions and placement angle."
            ]
        }}
        Do not include markdown code blocks. Output pure JSON only.
        """

        response = gemini_client.models.generate_content(
            model='gemini-2.5-flash',
            contents=[image, prompt],
            config=types.GenerateContentConfig(
                temperature=0.2,
                top_k=5,
                top_p=0.3
            )
        )

        raw_text = response.text.replace("```json", "").replace("```", "").strip()
        parsed_data = json.loads(raw_text)
        if isinstance(parsed_data, dict) and not parsed_data.get("is_valid_dental_scan", True):
            return {"status": "invalid_image", "message": "The uploaded image is not a valid CBCT or Panoramic Dental X-Ray scan."}

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
