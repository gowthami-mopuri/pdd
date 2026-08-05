import sys, time
sys.stdout.reconfigure(encoding='utf-8')
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait, Select
from selenium.webdriver.support import expected_conditions as EC
import tempfile, os, struct, zlib

opts = Options()
opts.add_argument('--headless')
opts.add_argument('--no-sandbox')
opts.add_argument('--disable-gpu')
driver = webdriver.Chrome(options=opts)
BASE = 'https://pdd-zfqq.onrender.com'

def go_client(path):
    driver.execute_script(f"window.history.pushState(null, '', '{path}'); window.dispatchEvent(new PopStateEvent('popstate'));")
    time.sleep(4)

def make_png():
    def chunk(name, data):
        c = zlib.crc32(name + data) & 0xFFFFFFFF
        return struct.pack(">I", len(data)) + name + data + struct.pack(">I", c)
    ihdr = chunk(b"IHDR", struct.pack(">IIBBBBB", 10, 10, 8, 2, 0, 0, 0))
    raw  = b"".join(b"\x00" + b"\xFF\x00\x00" * 10 for _ in range(10))
    idat = chunk(b"IDAT", zlib.compress(raw))
    iend = chunk(b"IEND", b"")
    return b"\x89PNG\r\n\x1a\n" + ihdr + idat + iend

try:
    driver.get(BASE + '/')
    time.sleep(3)
    go_client('/login')
    
    driver.find_element(By.CSS_SELECTOR, "input[type='text']").send_keys("clinicaldoc")
    driver.find_element(By.CSS_SELECTOR, "input[type='password']").send_keys("ClinicalPass123!")
    driver.find_element(By.CSS_SELECTOR, ".login-btn").click()
    time.sleep(6)
    
    go_client("/ai-analysis")
    
    # Select patient
    sel_el = driver.find_elements(By.TAG_NAME, "select")
    if sel_el:
        Select(sel_el[0]).select_by_index(1)
        time.sleep(1)
        
    # Upload image
    tmp = tempfile.NamedTemporaryFile(suffix=".png", delete=False)
    tmp.write(make_png()); tmp.flush(); tmp.close()
    
    inp = driver.find_element(By.CSS_SELECTOR, "input[type='file']")
    driver.execute_script("arguments[0].style.display='block';arguments[0].style.opacity='1';", inp)
    inp.send_keys(tmp.name)
    time.sleep(3)
    
    print("--- After upload, page text ---")
    print(driver.find_element(By.TAG_NAME, "body").text[:1500])
    
    # Click Run AI Analysis
    run_btn = driver.find_elements(By.XPATH, "//button[contains(text(), 'Run AI Analysis')]")
    print(f"Run AI Analysis buttons: {len(run_btn)}")
    if run_btn:
        driver.execute_script("arguments[0].click();", run_btn[0])
        print("Clicking Run AI Analysis...")
        time.sleep(10)
        
        print("--- After running AI Analysis, page text ---")
        print(driver.find_element(By.TAG_NAME, "body").text[:2000])
        
        # Check buttons
        print("Buttons on page:")
        for b in driver.find_elements(By.TAG_NAME, "button"):
            print(f"  text='{b.text}' class='{b.get_attribute('class')}'")
            
finally:
    try: os.unlink(tmp.name)
    except: pass
    driver.quit()
