import urllib.request
import json
import ssl

url = "https://gamfhzsvticwvzybchae.supabase.co/rest/v1/staff_accounts?select=*"
headers = {
    "apikey": "sb_publishable_CNsi7ICGKmNcqLkGOW7RdQ_xJEHgk7N",
    "Authorization": "Bearer sb_publishable_CNsi7ICGKmNcqLkGOW7RdQ_xJEHgk7N"
}

req = urllib.request.Request(url, headers=headers)
# Ignore SSL certificate verification if needed
context = ssl._create_unverified_context()

try:
    with urllib.request.urlopen(req, context=context) as response:
        data = json.loads(response.read().decode())
        print(json.dumps(data, indent=2))
except Exception as e:
    print("Error fetching:", e)
