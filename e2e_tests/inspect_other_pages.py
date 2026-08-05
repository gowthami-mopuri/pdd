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

def go_client(path):
    driver.execute_script(f"window.history.pushState(null, '', '{path}'); window.dispatchEvent(new PopStateEvent('popstate'));")
    time.sleep(4)

try:
    print("Navigating to homepage...")
    driver.get("https://pdd-zfqq.onrender.com/")
    time.sleep(3)
    
    print("Navigating to login page...")
    go_client("/login")
    
    # Enter credentials
    print("Logging in...")
    driver.find_element(By.CSS_SELECTOR, "input[type='text']").send_keys("teststaff")
    driver.find_element(By.CSS_SELECTOR, "input[type='password']").send_keys("Password123!")
    driver.find_element(By.CSS_SELECTOR, ".login-btn").click()
    time.sleep(6)
    
    # 1. Inspect AI Analysis
    print("\nNavigating to /ai-analysis...")
    go_client("/ai-analysis")
    print("URL:", driver.current_url)
    print("Body text (first 1000):")
    print(driver.find_element(By.TAG_NAME, "body").text[:1000])
    
    print("Inputs on AI Analysis page:")
    for inp in driver.find_elements(By.TAG_NAME, "input"):
        print(f"  type={inp.get_attribute('type')} name={inp.get_attribute('name')} class={inp.get_attribute('class')}")
        
    print("Buttons on AI Analysis page:")
    for btn in driver.find_elements(By.TAG_NAME, "button"):
        print(f"  text='{btn.text}' class='{btn.get_attribute('class')}'")

    # 2. Inspect Implant Survival
    print("\nNavigating to /implant-survival...")
    go_client("/implant-survival")
    print("URL:", driver.current_url)
    print("Body text (first 1000):")
    print(driver.find_element(By.TAG_NAME, "body").text[:1000])
    
    print("Inputs/Selects/Buttons on Implant Survival page:")
    for tag in ["input", "select", "button"]:
        for el in driver.find_elements(By.TAG_NAME, tag):
            print(f"  [{tag}] name={el.get_attribute('name')} class={el.get_attribute('class')} text='{el.text}'")

    # 3. Inspect Settings
    print("\nNavigating to /settings...")
    go_client("/settings")
    print("URL:", driver.current_url)
    print("Body text (first 1000):")
    print(driver.find_element(By.TAG_NAME, "body").text[:1000])
    
    print("Inputs on Settings page:")
    for inp in driver.find_elements(By.TAG_NAME, "input"):
        print(f"  type={inp.get_attribute('type')} name={inp.get_attribute('name')} value={inp.get_attribute('value')} class={inp.get_attribute('class')}")
    print("Buttons on Settings page:")
    for btn in driver.find_elements(By.TAG_NAME, "button"):
        print(f"  text='{btn.text}' class='{btn.get_attribute('class')}'")

finally:
    driver.quit()
