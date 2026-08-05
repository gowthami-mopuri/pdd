import sys
sys.stdout.reconfigure(encoding='utf-8')
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
import time

opts = Options()
opts.add_argument('--headless')
opts.add_argument('--disable-gpu')
opts.add_argument('--no-sandbox')
opts.set_capability('goog:loggingPrefs', {'browser': 'ALL'})

driver = webdriver.Chrome(options=opts)
try:
    print("Navigating to homepage...")
    driver.get("https://pdd-zfqq.onrender.com/")
    time.sleep(5)
    print("Console logs at homepage:")
    for log in driver.get_log('browser'):
        print(log)
        
    print("\nNavigating to login page...")
    driver.get("https://pdd-zfqq.onrender.com/login")
    time.sleep(5)
    print("Console logs at login page:")
    for log in driver.get_log('browser'):
        print(log)
finally:
    driver.quit()
