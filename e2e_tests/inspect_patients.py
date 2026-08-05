import sys, time
sys.stdout.reconfigure(encoding='utf-8')
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By

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
    
    driver.find_element(By.CSS_SELECTOR, "input[type='text']").send_keys("clinicaldoc")
    driver.find_element(By.CSS_SELECTOR, "input[type='password']").send_keys("ClinicalPass123!")
    driver.find_element(By.CSS_SELECTOR, ".login-btn").click()
    time.sleep(6)
    
    go_client("/patients")
    
    # Dump table HTML
    table = driver.find_elements(By.TAG_NAME, "table")
    if table:
        print("=== Table HTML (first 5000 chars) ===")
        print(table[0].get_attribute("innerHTML")[:5000])
        
    print("\n=== Elements inside tbody ===")
    rows = driver.find_elements(By.CSS_SELECTOR, "table tbody tr")
    for idx, r in enumerate(rows):
        print(f"Row {idx}: text='{r.text}'")
        cells = r.find_elements(By.TAG_NAME, "td")
        for cidx, cell in enumerate(cells):
            print(f"  Cell {cidx}: text='{cell.text}' HTML='{cell.get_attribute('innerHTML')}'")
            
finally:
    driver.quit()
