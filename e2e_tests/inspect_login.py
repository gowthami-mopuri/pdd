"""Inspect login page and all authenticated pages of ImplantAI."""
import sys, time
sys.stdout.reconfigure(encoding='utf-8')

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.action_chains import ActionChains
from webdriver_manager.chrome import ChromeDriverManager

BASE = 'https://pdd-zfqq.onrender.com'

opts = Options()
opts.add_argument('--start-maximized')
opts.add_argument('--disable-notifications')
svc = Service(ChromeDriverManager().install())
driver = webdriver.Chrome(service=svc, options=opts)
driver.set_page_load_timeout(30)

def wait_render(timeout=20):
    try:
        WebDriverWait(driver, timeout).until(
            lambda d: len(d.find_elements(By.CSS_SELECTOR, '#root *')) > 5
        )
    except: pass
    time.sleep(3)

def dump_page(label):
    print(f'\n{"="*70}')
    print(f'  PAGE: {label}  |  URL: {driver.current_url}')
    print('='*70)
    root = driver.execute_script("return document.getElementById('root') ? document.getElementById('root').innerHTML : document.body.innerHTML;")
    print(f'ROOT HTML (first 5000):\n{root[:5000]}')
    btns = driver.find_elements(By.TAG_NAME,'button')
    inps = driver.find_elements(By.TAG_NAME,'input')
    links= driver.find_elements(By.TAG_NAME,'a')
    print(f'\nBUTTONS ({len(btns)}):')
    for b in btns[:30]:
        print(f'  text="{b.text[:50]}" class="{b.get_attribute("class")}" aria="{b.get_attribute("aria-label")}"')
    print(f'\nINPUTS ({len(inps)}):')
    for i in inps[:20]:
        print(f'  type={i.get_attribute("type")} name={i.get_attribute("name")} placeholder={i.get_attribute("placeholder")} class={i.get_attribute("class")}')
    print(f'\nLINKS ({len(links)}):')
    for l in links[:20]:
        print(f'  text="{l.text[:40]}" href={l.get_attribute("href")} class={l.get_attribute("class")}')
    print(f'\nBODY TEXT:\n{driver.find_element(By.TAG_NAME,"body").text[:2000]}')

try:
    # Step 1: Landing page
    driver.get(BASE + '/')
    wait_render()
    dump_page('LANDING')

    # Step 2: Click Clinical Staff card
    print('\n\n>>> Clicking Clinical Staff card...')
    cards = driver.find_elements(By.CSS_SELECTOR, '.role-card, .clinical-card')
    for c in cards:
        if 'clinical' in (c.get_attribute('class') or '').lower() or 'Clinical' in c.text:
            c.click()
            break
    time.sleep(5)
    wait_render(10)
    dump_page('LOGIN PAGE')

    # Step 3: Try to find sign up / register link
    src = driver.page_source
    if 'register' in src.lower() or 'sign up' in src.lower() or 'create account' in src.lower():
        print('\n>>> REGISTER LINK FOUND')
    else:
        print('\n>>> No register link - login only')

    # Step 4: Attempt login with test credentials
    print('\n\n>>> Attempting login with test credentials...')
    email_inp = driver.find_elements(By.CSS_SELECTOR, "input[type='email'], input[name='email'], input[placeholder*='email' i]")
    pass_inp  = driver.find_elements(By.CSS_SELECTOR, "input[type='password']")
    print(f'Email inputs: {len(email_inp)}, Password inputs: {len(pass_inp)}')

    if email_inp and pass_inp:
        # Try with a test account
        email_inp[0].clear()
        email_inp[0].send_keys('implantai.test@gmail.com')
        pass_inp[0].clear()
        pass_inp[0].send_keys('Test@123456')
        # Find submit button
        submit = driver.find_elements(By.CSS_SELECTOR, "button[type='submit'], button")
        login_btn = None
        for b in submit:
            txt = b.text.lower()
            if any(k in txt for k in ['login','sign in','submit','enter']):
                login_btn = b; break
        if not login_btn and submit:
            login_btn = submit[0]
        if login_btn:
            print(f'Clicking: "{login_btn.text}"')
            login_btn.click()
            time.sleep(6)
            print(f'After login URL: {driver.current_url}')
            dump_page('AFTER LOGIN ATTEMPT')
        else:
            print('No login button found')

    # Step 5: If redirected to dashboard, explore all pages
    current_url = driver.current_url
    if 'login' not in current_url:
        print('\n\n>>> LOGIN SUCCESSFUL - exploring authenticated pages...')
        time.sleep(2)

        # Dump main dashboard
        dump_page('DASHBOARD')

        # Look for nav items
        nav_items = driver.find_elements(By.CSS_SELECTOR,
            '.nav-item, [class*="nav-item"], [class*="sidebar"] a, [class*="menu"] a, nav a, aside a')
        print(f'\nNAV ITEMS: {len(nav_items)}')
        for n in nav_items:
            print(f'  text="{n.text}" href={n.get_attribute("href")} class={n.get_attribute("class")}')

        # Try patients page
        try:
            driver.get(BASE + '/patients')
            time.sleep(5)
            dump_page('PATIENTS LIST')
        except: pass

        # Try add patient
        try:
            driver.get(BASE + '/patients/add')
            time.sleep(5)
            dump_page('ADD PATIENT FORM')
        except: pass

        # Go back and click first patient
        try:
            driver.get(BASE + '/patients')
            time.sleep(5)
            all_links = driver.find_elements(By.TAG_NAME,'a')
            patient_links = [l for l in all_links if l.get_attribute('href') and '/patients/' in l.get_attribute('href') and 'add' not in l.get_attribute('href')]
            if patient_links:
                first_url = patient_links[0].get_attribute('href')
                driver.get(first_url)
                time.sleep(8)
                dump_page('PATIENT DETAIL')
        except Exception as ex:
            print(f'Patient detail error: {ex}')
    else:
        print(f'\n>>> Login failed or still on login page: {current_url}')
        # Show what error appeared
        body_text = driver.find_element(By.TAG_NAME,'body').text
        print(f'Page text: {body_text[:500]}')

finally:
    driver.quit()

print('\nDONE.')
