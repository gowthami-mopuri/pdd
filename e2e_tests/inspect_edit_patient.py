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
    
    view_btn = driver.find_elements(By.CSS_SELECTOR, "button[data-tip='View Patient']")
    if view_btn:
        driver.execute_script("arguments[0].click();", view_btn[0])
        time.sleep(6)
        
        # Find Edit Patient button and click it
        edit_btn = driver.find_elements(By.CSS_SELECTOR, "button[data-tip='Edit Patient']")
        print(f"Edit Patient buttons found: {len(edit_btn)}")
        if edit_btn:
            driver.execute_script("arguments[0].click();", edit_btn[0])
            time.sleep(3)
            print("--- Body text in Edit Patient modal ---")
            print(driver.find_element(By.TAG_NAME, "body").text[:1500])
            print("--- Buttons in Edit Patient modal ---")
            for b in driver.find_elements(By.TAG_NAME, "button"):
                print(f"text='{b.text}' class='{b.get_attribute('class')}'")
finally:
    driver.quit()
