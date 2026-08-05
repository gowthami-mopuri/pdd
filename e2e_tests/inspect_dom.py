"""Deep DOM inspector for ImplantAI app."""
import sys, re, time, json
sys.stdout.reconfigure(encoding='utf-8')

# ── Part 1: analyse bundle for routes & keywords ────────────────────────────
with open(r'dist/assets/index-C-OAeCyq.js', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

routes = re.findall(r'path:"(/[^"]{0,60})"', content)
routes = sorted(set(routes))
print('=== ROUTES ===')
for r in routes:
    print(' ', r)

# Sidebar/nav class patterns
nav_patterns = re.findall(r'(sidebar|nav-item|navitem|menu-item|menuitem|nav__)', content, re.I)
print('\n=== NAV PATTERNS ===', sorted(set(p.lower() for p in nav_patterns)))

# Find text labels in nav
labels = re.findall(r'"(Patients|Dashboard|Settings|Reports|Analysis|Scan|Add Patient|Chat|Logout|Sign Out)"', content)
print('\n=== NAV LABELS ===', sorted(set(labels)))

# All classNames
classnames = re.findall(r'className:"([^"]{3,60})"', content)
classnames += re.findall(r"className:'([^']{3,60})'", content)
print('\n=== SAMPLE CLASSNAMES (first 60) ===')
for c in sorted(set(classnames))[:60]:
    print(' ', c)

# Check for data-testid
testids = re.findall(r'data-testid:"([^"]+)"', content)
testids += re.findall(r"data-testid:'([^']+)'", content)
print('\n=== DATA-TESTID ===', testids[:30])

# ── Part 2: Selenium deep scan ──────────────────────────────────────────────
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager

BASE = 'https://pdd-zfqq.onrender.com'

opts = Options()
opts.add_argument('--start-maximized')
opts.add_argument('--disable-notifications')
svc = Service(ChromeDriverManager().install())
driver = webdriver.Chrome(service=svc, options=opts)
driver.set_page_load_timeout(30)

def deep_snap(label, url, extra_wait=8):
    print(f'\n\n{"="*60}')
    print(f'PAGE: {label}  |  URL: {url}')
    print('='*60)
    driver.get(url)
    # Wait for React to render - wait for ANY element inside #root
    try:
        WebDriverWait(driver, 20).until(
            lambda d: len(d.find_elements(By.CSS_SELECTOR, '#root *')) > 10
        )
    except: pass
    time.sleep(extra_wait)

    # Full inner HTML of root (truncated)
    root_html = driver.execute_script("return document.getElementById('root').innerHTML;")
    print(f'\n--- ROOT HTML (first 3000 chars) ---')
    print(root_html[:3000])

    # All elements with class attributes
    all_els = driver.find_elements(By.CSS_SELECTOR, '[class]')
    classes = set()
    for el in all_els[:200]:
        cls = el.get_attribute('class') or ''
        for c in cls.split():
            if len(c) > 3:
                classes.add(c)
    print(f'\n--- ALL CLASSES ({len(classes)}) ---')
    for c in sorted(classes)[:80]:
        print(' ', c)

    # Links
    links = driver.find_elements(By.TAG_NAME, 'a')
    print(f'\n--- LINKS ({len(links)}) ---')
    for l in links[:25]:
        print(f"  text='{l.text[:40]}' href='{l.get_attribute('href')}' class='{l.get_attribute('class')}'")

    # Buttons
    btns = driver.find_elements(By.TAG_NAME, 'button')
    print(f'\n--- BUTTONS ({len(btns)}) ---')
    for b in btns[:30]:
        print(f"  text='{b.text[:40]}' class='{b.get_attribute('class')}' aria='{b.get_attribute('aria-label')}'")

    # Inputs
    inps = driver.find_elements(By.TAG_NAME, 'input')
    print(f'\n--- INPUTS ({len(inps)}) ---')
    for i in inps[:20]:
        print(f"  type='{i.get_attribute('type')}' name='{i.get_attribute('name')}' "
              f"placeholder='{i.get_attribute('placeholder')}' class='{i.get_attribute('class')}'")

    # Selects
    sels = driver.find_elements(By.TAG_NAME, 'select')
    print(f'\n--- SELECTS ({len(sels)}) ---')
    for s in sels:
        print(f"  name='{s.get_attribute('name')}' class='{s.get_attribute('class')}'")

    # Textareas
    txts = driver.find_elements(By.TAG_NAME, 'textarea')
    print(f'\n--- TEXTAREAS ({len(txts)}) ---')
    for t in txts:
        print(f"  name='{t.get_attribute('name')}' class='{t.get_attribute('class')}' placeholder='{t.get_attribute('placeholder')}'")

    # SVGs
    svgs = driver.find_elements(By.TAG_NAME, 'svg')
    print(f'\n--- SVGs: {len(svgs)}, Canvas: {len(driver.find_elements(By.TAG_NAME, "canvas"))} ---')

    # Visible text snippets
    body_text = driver.find_element(By.TAG_NAME, 'body').text
    print(f'\n--- BODY TEXT (first 1500 chars) ---')
    print(body_text[:1500])

try:
    deep_snap('homepage', BASE + '/', extra_wait=10)

    # Try to find and click first patient
    try:
        links = driver.find_elements(By.TAG_NAME, 'a')
        patient_link = None
        for l in links:
            href = l.get_attribute('href') or ''
            if '/patient' in href and 'add' not in href:
                patient_link = href; break
        if patient_link:
            deep_snap('patient_detail', patient_link, extra_wait=8)
        else:
            print('\n[!] No patient detail link found on homepage')
            # Try clicking first card/row
            cards = driver.find_elements(By.CSS_SELECTOR, '[class*="card"], [class*="row"], li, tr')
            if cards:
                try:
                    cards[0].click()
                    time.sleep(5)
                    print(f'[!] Clicked first card - new URL: {driver.current_url}')
                    deep_snap('after_card_click', driver.current_url, extra_wait=6)
                except: pass
    except Exception as ex:
        print(f'Patient link error: {ex}')

    deep_snap('add_patient', BASE + '/#/patients/add', extra_wait=8)

finally:
    driver.quit()

print('\n\nDONE.')
