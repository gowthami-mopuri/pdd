import sys, time
sys.stdout.reconfigure(encoding='utf-8')
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

opts = Options()
opts.add_argument('--headless')
opts.add_argument('--no-sandbox')
opts.add_argument('--disable-gpu')
driver = webdriver.Chrome(options=opts)
BASE = 'https://pdd-zfqq.onrender.com'

def go_client(path):
    driver.execute_script(f"window.history.pushState(null, '', '{path}'); window.dispatchEvent(new PopStateEvent('popstate'));")
    time.sleep(4)

try:
    driver.get(BASE + '/')
    time.sleep(3)
    go_client('/login')
    
    # Enter credentials
    driver.find_element(By.CSS_SELECTOR, "input[type='text']").send_keys("clinicaldoc")
    driver.find_element(By.CSS_SELECTOR, "input[type='password']").send_keys("ClinicalPass123!")
    driver.find_element(By.CSS_SELECTOR, ".login-btn").click()
    time.sleep(6)
    print("=== LOGGED IN ===")
    
    # 1. Patients Page
    print("\n--- Patients Page ---")
    go_client("/patients")
    print(driver.find_element(By.TAG_NAME, "body").text[:1500])
    
    # Click first patient
    print("\n--- Clicking Patient Detail ---")
    links = driver.find_elements(By.TAG_NAME, "a")
    patient_links = [l for l in links if l.get_attribute("href") and "/patients/" in l.get_attribute("href") and "add" not in l.get_attribute("href")]
    print(f"Found {len(patient_links)} patient links.")
    if patient_links:
        patient_url = patient_links[0].get_attribute("href")
        print("Detail URL:", patient_url)
        driver.get(patient_url)
        time.sleep(6)
        print("Patient Detail Body Text:")
        print(driver.find_element(By.TAG_NAME, "body").text[:2000])
        print("Buttons/Inputs on Patient Detail:")
        for tag in ["button", "input", "a"]:
            for el in driver.find_elements(By.TAG_NAME, tag):
                print(f"  [{tag}] class='{el.get_attribute('class')}' text='{el.text[:50]}'")
    
    # 3. Nonexistent Page
    print("\n--- Nonexistent Page ---")
    go_client("/nonexistent-page-xyz-999")
    print("URL:", driver.current_url)
    print(driver.find_element(By.TAG_NAME, "body").text[:1500])
    
finally:
    driver.quit()
