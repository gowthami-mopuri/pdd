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
    
    row = driver.find_element(By.CSS_SELECTOR, "table tbody tr")
    ellipsis = row.find_element(By.CSS_SELECTOR, "button.icon-btn:not([data-tip])")
    print("Clicking ellipsis...")
    driver.execute_script("arguments[0].click();", ellipsis)
    time.sleep(2)
    
    # Print elements that contain text "delete" case-insensitive
    print("=== Elements containing 'delete' ===")
    els = driver.find_elements(By.XPATH, "//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delete')]")
    print(f"Found {len(els)} elements")
    for idx, el in enumerate(els):
        print(f"[{idx}] tag='{el.tag_name}' class='{el.get_attribute('class')}' text='{el.text}'")
        
    print("\n=== Inner HTML of action buttons container ===")
    print(row.find_element(By.CSS_SELECTOR, ".action-buttons").get_attribute("outerHTML"))
    
finally:
    driver.quit()
