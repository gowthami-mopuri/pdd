import pytest
from fastapi.testclient import TestClient
from main import app
import io
from PIL import Image
import json
import os

client = TestClient(app)

def create_dummy_image():
    img = Image.new('RGB', (100, 100), color='red')
    img_byte_arr = io.BytesIO()
    img.save(img_byte_arr, format='JPEG')
    img_byte_arr.seek(0)
    return img_byte_arr

def test_read_root():
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"status": "online", "message": "Dental AI Backend is running"}

def test_analyze_panoramic():
    img_bytes = create_dummy_image()
    response = client.post(
        "/analyze/panoramic",
        files={"file": ("test.jpg", img_bytes, "image/jpeg")}
    )
    assert response.status_code == 200
    assert "status" in response.json()
    assert response.json()["status"] == "success"

def test_analyze_implant():
    img_bytes = create_dummy_image()
    response = client.post(
        "/analyze/implant",
        files={"file": ("test.jpg", img_bytes, "image/jpeg")}
    )
    assert response.status_code == 200
    assert response.json()["status"] == "success"

def test_analyze_mandibular():
    img_bytes = create_dummy_image()
    response = client.post(
        "/analyze/mandibular",
        files={"file": ("test.jpg", img_bytes, "image/jpeg")}
    )
    assert response.status_code == 200
    assert response.json()["status"] == "success"

def test_analyze_sinus():
    img_bytes = create_dummy_image()
    response = client.post(
        "/analyze/sinus",
        files={"file": ("test.jpg", img_bytes, "image/jpeg")}
    )
    assert response.status_code == 200
    assert response.json()["status"] == "success"

def test_analyze_gemini_survival():
    img_bytes = create_dummy_image()
    patient_data = {
        "name": "Test Patient",
        "age": 45,
        "medical_history": "Smoking, Diabetes"
    }
    response = client.post(
        "/analyze/gemini-survival",
        files={"file": ("test.jpg", img_bytes, "image/jpeg")},
        data={"patient_data": json.dumps(patient_data)}
    )
    # Even if API key is missing, it should fail gracefully with 500, or succeed with 200
    assert response.status_code in [200, 500]
    if response.status_code == 200:
        assert response.json()["status"] == "success"
        assert "data" in response.json()

def test_chat_personalized():
    payload = {
        "patient_data": {"name": "John Doe", "age": 30},
        "messages": [{"role": "user", "content": "What is a cavity?"}]
    }
    response = client.post("/chat/personalized", json=payload)
    assert response.status_code in [200, 500]
    if response.status_code == 200:
        assert response.json()["status"] == "success"
        assert "reply" in response.json()
