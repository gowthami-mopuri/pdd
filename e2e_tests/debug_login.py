import time, sys, os
sys.stdout.reconfigure(encoding="utf-8")
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager

BASE_URL = "https://pdd-zfqq.onrender.com"

opts = Options()
opts.add_argument("--headless")
opts.add_argument("--no-sandbox")
opts.add_argument("--disable-gpu")
driver = webdriver.Chrome(options=opts)

def wait_render(timeout=8):
    try:
        WebDriverWait(driver, timeout).until(
            lambda d: len(d.find_elements(By.CSS_SELECTOR, "#root *")) > 5
        )
    except: pass
    time.sleep(2)

def go(path=""):
    if not path or path == "/":
        driver.get(BASE_URL + "/")
        time.sleep(1)
        wait_render(12)
    else:
        try:
            curr_url = driver.current_url
        except:
            curr_url = ""
        if not curr_url or curr_url == "data:," or "pdd-zfqq.onrender.com" not in curr_url:
            driver.get(BASE_URL + "/")
            time.sleep(1)
            wait_render(12)
        driver.execute_script(f"window.history.pushState(null, '', '{path}'); window.dispatchEvent(new PopStateEvent('popstate'));")
        time.sleep(1)

try:
    print("=== START DEBUG ===")
    
    # TC_011
    print("\n[TC_011] Going to / and clicking Clinical Staff card")
    go("/")
    card = driver.find_element(By.CSS_SELECTOR, ".clinical-card")
    card.click()
    time.sleep(3)
    print("URL after click:", driver.current_url)
    
    # TC_012 to TC_016 check
    print("\nChecking headers and inputs on login page:")
    print("Doctor Portal text present:", "doctor portal" in driver.find_element(By.TAG_NAME, "body").text.lower())
    inps = driver.find_elements(By.CSS_SELECTOR, "input[type='text'], input[placeholder*='username' i]")
    print(f"Text inputs found: {len(inps)}")
    pw_inps = driver.find_elements(By.CSS_SELECTOR, "input[type='password']")
    print(f"Password inputs found: {len(pw_inps)}")
    
    # TC_017
    print("\n[TC_017] Clicking submit with empty inputs")
    btn = driver.find_element(By.CSS_SELECTOR, ".login-btn, button[type='submit']")
    btn.click()
    time.sleep(2)
    print("URL after empty submit:", driver.current_url)
    print("Body text contains required/error:", any(k in driver.find_element(By.TAG_NAME, "body").text.lower() for k in ["required", "invalid", "error", "fill", "username", "password"]))
    
    # TC_018
    print("\n[TC_018] Running go(driver, '/login') and wrong credentials")
    go("/login")
    wait_render(6)
    print("URL after go('/login'):", driver.current_url)
    
    # Find inputs again
    u_inps = driver.find_elements(By.CSS_SELECTOR, "input[type='text']")
    p_inps = driver.find_elements(By.CSS_SELECTOR, "input[type='password']")
    print(f"After go('/login'): Text inputs={len(u_inps)}, Password inputs={len(p_inps)}")
    
    if u_inps and p_inps:
        u_inps[0].clear()
        u_inps[0].send_keys("wronguser")
        p_inps[0].clear()
        p_inps[0].send_keys("wrongpass")
        btn = driver.find_element(By.CSS_SELECTOR, ".login-btn, button[type='submit']")
        btn.click()
        time.sleep(4)
        print("URL after wrong submit:", driver.current_url)
        print("Body text after wrong submit contains error:", any(k in driver.find_element(By.TAG_NAME, "body").text.lower() for k in ["invalid", "incorrect", "error", "fail", "wrong"]))
    else:
        print("COULD NOT FIND INPUTS FOR TC_018!")
        print("HTML at this moment:")
        print(driver.find_element(By.TAG_NAME, "body").get_attribute("innerHTML")[:2000])

    # TC_019
    print("\n[TC_019] Going to /login and clicking back button")
    go("/login")
    time.sleep(2)
    back = driver.find_elements(By.CSS_SELECTOR, ".back-btn")
    print(f"Back buttons found: {len(back)}")
    if back:
        back[0].click()
        time.sleep(3)
        print("URL after clicking back:", driver.current_url)
    else:
        print("NO BACK BUTTON FOUND!")

    # TC_020
    print("\n[TC_020] Attempting login with correct credentials")
    go("/login")
    time.sleep(2)
    u_inps = driver.find_elements(By.CSS_SELECTOR, "input[type='text']")
    p_inps = driver.find_elements(By.CSS_SELECTOR, "input[type='password']")
    if u_inps and p_inps:
        u_inps[0].clear()
        u_inps[0].send_keys("clinicaldoc")
        p_inps[0].clear()
        p_inps[0].send_keys("ClinicalPass123!")
        btn = driver.find_element(By.CSS_SELECTOR, ".login-btn, button[type='submit']")
        btn.click()
        time.sleep(5)
        print("URL after correct login:", driver.current_url)
        print("Contains patient/dashboard in body:", any(k in driver.find_element(By.TAG_NAME, "body").text.lower() for k in ["patient", "dashboard", "scan", "setting", "report"]))
    else:
        print("Could not find inputs for correct login!")

except Exception as e:
    print("CRASHED with exception:", e)
finally:
    driver.quit()
