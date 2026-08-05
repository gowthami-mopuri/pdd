import sys, time
sys.stdout.reconfigure(encoding='utf-8')
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

opts = Options()
opts.add_argument('--headless')
opts.add_argument('--disable-gpu')
opts.add_argument('--no-sandbox')

driver = webdriver.Chrome(options=opts)

try:
    print("Navigating to homepage...")
    driver.get("https://pdd-zfqq.onrender.com/")
    time.sleep(3)
    
    # Click Clinical Staff card
    print("Clicking Clinical Staff card...")
    driver.find_element(By.CSS_SELECTOR, ".clinical-card").click()
    time.sleep(3)
    
    # Enter credentials
    print("Logging in...")
    driver.find_element(By.CSS_SELECTOR, "input[type='text']").send_keys("teststaff")
    driver.find_element(By.CSS_SELECTOR, "input[type='password']").send_keys("Password123!")
    driver.find_element(By.CSS_SELECTOR, ".login-btn").click()
    time.sleep(6)
    
    print("Logged in. Current URL:", driver.current_url)
    
    def click_nav_and_check(label):
        print(f"\n--- Clicking nav item with text '{label}' ---")
        links = driver.find_elements(By.CSS_SELECTOR, "a.nav-item, .sidebar a, nav a")
        target = None
        for l in links:
            if label.lower() in l.text.lower():
                target = l
                break
        if target:
            print(f"Found link: text='{target.text}' href='{target.get_attribute('href')}'")
            target.click()
            time.sleep(4)
            print("Current URL after click:", driver.current_url)
            print("Body text snippet:")
            print(driver.find_element(By.TAG_NAME, "body").text[:500])
        else:
            print(f"Could not find link with text '{label}'")
            
    # Click Patients
    click_nav_and_check("Patients")
    
    # Click AI Analysis
    click_nav_and_check("AI Analysis")
    
    # Click Reports
    click_nav_and_check("Reports")
    
finally:
    driver.quit()
