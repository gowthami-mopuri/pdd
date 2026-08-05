import requests
import json
import os

url = 'http://127.0.0.1:8000/analyze/gemini-survival'

# Create a dummy image
from PIL import Image
img = Image.new('RGB', (100, 100), color = 'red')
img.save('test.jpg')

patient_data = {
    "name": "Test Patient",
    "age": 45,
    "medical_history": "Smoking, Diabetes"
}


data = {'patient_data': json.dumps(patient_data)}

print("Sending request to", url, "...")
try:
    with open('test.jpg', 'rb') as f:
        files = {'file': f}
        response = requests.post(url, files=files, data=data)
        print("Status Code:", response.status_code)
        print("Response Body:")
        print(json.dumps(response.json(), indent=2))
except Exception as e:
    print("Error:", e)

if os.path.exists('test.jpg'):
    os.remove('test.jpg')
