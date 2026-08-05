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
    
    # Find the ellipsis button in the first row
    row = driver.find_element(By.CSS_SELECTOR, "table tbody tr")
    ellipsis = row.find_element(By.CSS_SELECTOR, "button.icon-btn:not([data-tip])")
    print("Clicking ellipsis button...")
    driver.execute_script("arguments[0].click();", ellipsis)
    time.sleep(2)
    
    # Check page text and HTML
    print("--- Page text after clicking ellipsis ---")
    print(driver.find_element(By.TAG_NAME, "body").text[:1500])
    
    print("\n--- Buttons on page after clicking ellipsis ---")
    for b in driver.find_elements(By.TAG_NAME, "button"):
        print(f"text='{b.text}' class='{b.get_attribute('class')}' data-tip='{b.get_attribute('data-tip')}'")
        
finally:
    driver.quit()
