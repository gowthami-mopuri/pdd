import sys, time
sys.stdout.reconfigure(encoding='utf-8')
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By

opts = Options()
opts.add_argument('--headless')
opts.add_argument('--disable-gpu')
opts.add_argument('--no-sandbox')

driver = webdriver.Chrome(options=opts)
try:
    print("Loading homepage...")
    driver.get("https://pdd-zfqq.onrender.com/")
    time.sleep(5)
    print("Initial URL:", driver.current_url)
    print("Body text contains 'Clinical Staff':", "Clinical Staff" in driver.find_element(By.TAG_NAME, "body").text)
    
    print("\nExecuting history pushState to /login...")
    driver.execute_script("window.history.pushState(null, '', '/login'); window.dispatchEvent(new PopStateEvent('popstate'));")
    time.sleep(4)
    print("URL after pushState:", driver.current_url)
    print("Body text contains 'Doctor Portal':", "Doctor Portal" in driver.find_element(By.TAG_NAME, "body").text)
    print("Body text contains 'Username':", "Username" in driver.find_element(By.TAG_NAME, "body").text)
    
    print("\nExecuting history pushState to /patient-login...")
    driver.execute_script("window.history.pushState(null, '', '/patient-login'); window.dispatchEvent(new PopStateEvent('popstate'));")
    time.sleep(4)
    print("URL after pushState:", driver.current_url)
    print("Body text contains 'Patient Portal':", "Patient" in driver.find_element(By.TAG_NAME, "body").text)
finally:
    driver.quit()
