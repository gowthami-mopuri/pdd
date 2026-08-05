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

def go_client(path):
    driver.execute_script(f"window.history.pushState(null, '', '{path}'); window.dispatchEvent(new PopStateEvent('popstate'));")
    time.sleep(3)

try:
    print("Navigating to landing page...")
    driver.get("https://pdd-zfqq.onrender.com/")
    time.sleep(3)
    
    print("Navigating to login page...")
    go_client("/login")
    
    # Enter credentials
    print("Entering credentials...")
    u_inp = driver.find_element(By.CSS_SELECTOR, "input[type='text']")
    p_inp = driver.find_element(By.CSS_SELECTOR, "input[type='password']")
    u_inp.send_keys("teststaff")
    p_inp.send_keys("Password123!")
    
    btn = driver.find_element(By.CSS_SELECTOR, ".login-btn")
    btn.click()
    print("Clicked login, waiting for dashboard...")
    time.sleep(6)
    print("Current URL after login:", driver.current_url)
    
    # Check body text
    body_text = driver.find_element(By.TAG_NAME, "body").text
    print("\nBody text after login (first 1000 chars):")
    print(body_text[:1000])
    
    # Dump sidebar/navigation elements
    print("\nSearching for nav/sidebar/menu elements:")
    for tag in ['nav', 'aside', 'div', 'a', 'button']:
        elements = driver.find_elements(By.CSS_SELECTOR, f"{tag}[class*='sidebar' i], {tag}[class*='nav' i], {tag}[class*='menu' i]")
        if elements:
            print(f"Found {len(elements)} elements of tag {tag} with sidebar/nav/menu class:")
            for e in elements[:10]:
                print(f"  class='{e.get_attribute('class')}' text='{e.text.strip()[:40]}'")
                
    # Check what route we are on, try to navigate to /patients
    print("\nNavigating to /patients...")
    go_client("/patients")
    print("URL:", driver.current_url)
    print("Body text contains 'Patients':", "patient" in driver.find_element(By.TAG_NAME, "body").text.lower())
    
    # Check for links or buttons in the patient page
    all_links = driver.find_elements(By.TAG_NAME, "a")
    print(f"\nAll links on patients page ({len(all_links)}):")
    for l in all_links[:20]:
        print(f"  text='{l.text}' href='{l.get_attribute('href')}' class='{l.get_attribute('class')}'")
        
finally:
    driver.quit()
