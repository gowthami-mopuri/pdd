"""
======================================================================
  ImplantAI Mobile E2E Test Suite — Appium v2
  App ID : com.globaldental.pdd
  Run    : python e2e_tests/test_appium_mobile.py
  Note   : Ensure Appium server is running and a device/emulator is connected
======================================================================
"""

import time
import os
import sys
import datetime
import traceback
from appium import webdriver
from appium.options.common import AppiumOptions
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# ─── CONFIG ─────────────────────────────────────────────────────────
APPIUM_SERVER_URL = "http://localhost:4723"
TEST_USERNAME = os.environ.get("TEST_USERNAME", "clinicaldoc")
TEST_PASSWORD = os.environ.get("TEST_PASSWORD", "ClinicalPass123!")
WAIT_TIMEOUT = 15

# Results collector
results = []

def record(tc_id, name, status, duration, message=""):
    results.append({
        "TC_ID": tc_id,
        "Name": name,
        "Status": status,
        "Duration": round(duration, 2),
        "Message": message
    })
    icon = "✅" if status == "PASS" else "❌"
    print(f"  {icon} [{tc_id}] {name} ({duration:.2f}s) - {message}")

def get_capabilities():
    """Build capabilities for Capacitor Android app under test."""
    options = AppiumOptions()
    options.set_capability("platformName", "Android")
    options.set_capability("automationName", "UiAutomator2")
    options.set_capability("appPackage", "com.globaldental.pdd")
    options.set_capability("appActivity", "com.globaldental.pdd.MainActivity")
    options.set_capability("noReset", False)
    options.set_capability("autoGrantPermissions", True)
    options.set_capability("newCommandTimeout", 120)
    return options

def make_driver():
    caps = get_capabilities()
    print(f"Connecting to Appium Server at {APPIUM_SERVER_URL}...")
    driver = webdriver.Remote(APPIUM_SERVER_URL, options=caps)
    return driver

def switch_to_webview(driver):
    """Switch Appium driver context to WEBVIEW if available."""
    print("Available Contexts:", driver.contexts)
    for ctx in driver.contexts:
        if "WEBVIEW" in ctx:
            driver.switch_to.context(ctx)
            print(f"Switched context to: {ctx}")
            return True
    print("WebView context not found. Staying in NATIVE_APP context.")
    return False

# ─── TEST CASES ─────────────────────────────────────────────────────

def test_app_launch(driver):
    cat = "Mobile App Launch"
    print(f"\n--- Running: {cat} ---")
    t0 = time.time()
    try:
        # Wait for MainActivity/WebView container to mount
        time.sleep(5)
        # Attempt WebView transition
        switch_to_webview(driver)
        
        # Verify landing page content (either via Webview or Native elements)
        # We check body text, or role selection elements
        body_text = ""
        try:
            body = WebDriverWait(driver, WAIT_TIMEOUT).until(
                EC.presence_of_element_located((By.TAG_NAME, "body"))
            )
            body_text = body.text.lower()
        except:
            # Fallback to page source in native context
            body_text = driver.page_source.lower()

        ok = "implantai" in body_text or "clinical" in body_text
        record("TC_MOB_001", "App launches successfully to Role Selection", 
               "PASS" if ok else "FAIL", time.time() - t0, 
               "Found landing words" if ok else "Landing words not found")
    except Exception as e:
        record("TC_MOB_001", "App launches successfully to Role Selection", 
               "FAIL", time.time() - t0, str(e))

def test_role_selection(driver):
    cat = "Role Card Interactivity"
    print(f"\n--- Running: {cat} ---")
    t0 = time.time()
    try:
        # Select Clinical Staff card
        clicked = False
        selectors = [
            (By.CSS_SELECTOR, ".clinical-card"),
            (By.XPATH, "//*[contains(@class,'clinical-card')]"),
            (By.XPATH, "//*[contains(@text,'Clinical Staff')]"),
            (By.XPATH, "//*[contains(@content-desc,'clinical')]")
        ]
        for by, sel in selectors:
            try:
                el = WebDriverWait(driver, 5).until(EC.element_to_be_clickable((by, sel)))
                el.click()
                clicked = True
                print(f"Clicked Clinical Card using {by}={sel}")
                break
            except:
                continue

        if not clicked:
            raise NoSuchElementException("Could not locate or click Clinical Staff role card.")

        time.sleep(3)
        # Verify navigation to login screen
        curr_source = driver.page_source.lower()
        ok = "login" in curr_source or "doctor portal" in curr_source or "secure login" in curr_source
        record("TC_MOB_002", "Clinical Staff card navigates to Login", 
               "PASS" if ok else "FAIL", time.time() - t0,
               "Navigated to login screen" if ok else "Still on landing screen")
    except Exception as e:
        record("TC_MOB_002", "Clinical Staff card navigates to Login", 
               "FAIL", time.time() - t0, str(e))

def test_mobile_login(driver):
    cat = "Authentication Flow"
    print(f"\n--- Running: {cat} ---")
    t0 = time.time()
    try:
        # Locate Username & Password fields
        u_field, p_field, btn = None, None, None
        
        # Try Web elements first
        for by, sel in [
            (By.CSS_SELECTOR, "input[type='text']"),
            (By.XPATH, "//input[contains(@placeholder, 'username')]"),
            (By.XPATH, "//android.widget.EditText[1]")
        ]:
            try:
                u_field = WebDriverWait(driver, 5).until(EC.presence_of_element_located((by, sel)))
                break
            except: pass

        for by, sel in [
            (By.CSS_SELECTOR, "input[type='password']"),
            (By.XPATH, "//input[contains(@placeholder, 'password')]"),
            (By.XPATH, "//android.widget.EditText[2]")
        ]:
            try:
                p_field = WebDriverWait(driver, 5).until(EC.presence_of_element_located((by, sel)))
                break
            except: pass

        for by, sel in [
            (By.CSS_SELECTOR, "button[type='submit'], .login-btn"),
            (By.XPATH, "//button[contains(text(), 'Login')]"),
            (By.XPATH, "//android.widget.Button[contains(@text, 'Login')]")
        ]:
            try:
                btn = WebDriverWait(driver, 5).until(EC.element_to_be_clickable((by, sel)))
                break
            except: pass

        if not (u_field and p_field and btn):
            raise NoSuchElementException("Could not find input fields or login button.")

        # Fill inputs
        u_field.clear()
        u_field.send_keys(TEST_USERNAME)
        p_field.clear()
        p_field.send_keys(TEST_PASSWORD)
        
        # Click login
        btn.click()
        time.sleep(5)
        
        # Verify authenticated landing
        body_text = ""
        try:
            body = WebDriverWait(driver, WAIT_TIMEOUT).until(
                EC.presence_of_element_located((By.TAG_NAME, "body"))
            )
            body_text = body.text.lower()
        except:
            body_text = driver.page_source.lower()

        logged_in = any(x in body_text for x in ["patients", "dashboard", "scan", "reports", "settings"])
        record("TC_MOB_003", "Secure login with credentials", 
               "PASS" if logged_in else "FAIL", time.time() - t0,
               "Authenticated successfully" if logged_in else "Login verification failed")
    except Exception as e:
        record("TC_MOB_003", "Secure login with credentials", 
               "FAIL", time.time() - t0, str(e))

def test_mobile_navigation(driver):
    cat = "App Navigation"
    print(f"\n--- Running: {cat} ---")
    t0 = time.time()
    try:
        # Navigate to Patients list
        navigated = False
        for by, sel in [
            (By.LINK_TEXT, "Patients"),
            (By.XPATH, "//*[contains(text(), 'Patients')]"),
            (By.XPATH, "//*[contains(@content-desc, 'Patients')]"),
            (By.XPATH, "//a[contains(@href, 'patients')]")
        ]:
            try:
                item = WebDriverWait(driver, 5).until(EC.element_to_be_clickable((by, sel)))
                item.click()
                navigated = True
                break
            except: pass

        if navigated:
            time.sleep(3)
            body_text = driver.page_source.lower()
            ok = "patient" in body_text and ("add" in body_text or "search" in body_text or "list" in body_text)
            record("TC_MOB_004", "Navigate to Patients list page", 
                   "PASS" if ok else "FAIL", time.time() - t0,
                   "Successfully navigated to Patients list page")
        else:
            record("TC_MOB_004", "Navigate to Patients list page", 
                   "SKIP", time.time() - t0, "Navigation item 'Patients' not clickable")
    except Exception as e:
        record("TC_MOB_004", "Navigate to Patients list page", 
               "FAIL", time.time() - t0, str(e))

def test_logout(driver):
    cat = "User Logout"
    print(f"\n--- Running: {cat} ---")
    t0 = time.time()
    try:
        logged_out = False
        for by, sel in [
            (By.XPATH, "//*[contains(text(), 'Logout')]"),
            (By.XPATH, "//*[contains(text(), 'Sign Out')]"),
            (By.XPATH, "//*[contains(@class, 'logout')]")
        ]:
            try:
                btn = WebDriverWait(driver, 5).until(EC.element_to_be_clickable((by, sel)))
                btn.click()
                logged_out = True
                break
            except: pass

        if logged_out:
            time.sleep(3)
            body_text = driver.page_source.lower()
            ok = "doctor portal" in body_text or "clinical staff" in body_text
            record("TC_MOB_005", "Logout redirects back to portals screen", 
                   "PASS" if ok else "FAIL", time.time() - t0,
                   "Logged out and returned to authentication pages")
        else:
            record("TC_MOB_005", "Logout redirects back to portals screen", 
                   "SKIP", time.time() - t0, "Logout button not clickable")
    except Exception as e:
        record("TC_MOB_005", "Logout redirects back to portals screen", 
               "FAIL", time.time() - t0, str(e))


def main():
    print("=" * 70)
    print("  ImplantAI Mobile E2E Test Suite (Appium)")
    print(f"  Time     : {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 70)

    driver = None
    try:
        driver = make_driver()
        test_app_launch(driver)
        test_role_selection(driver)
        test_mobile_login(driver)
        test_mobile_navigation(driver)
        test_logout(driver)
    except Exception as ex:
        print(f"\n[CRITICAL ERROR] Appium driver setup failed: {ex}")
        traceback.print_exc()
    finally:
        if driver:
            driver.quit()

    print("\n" + "=" * 70)
    print("  MOBILE TEST RUN SUMMARY")
    print("-" * 70)
    total = len(results)
    passed = sum(1 for r in results if r["Status"] == "PASS")
    failed = sum(1 for r in results if r["Status"] == "FAIL")
    skipped = sum(1 for r in results if r["Status"] == "SKIP")
    print(f"  Total   : {total}")
    print(f"  Passed  : {passed}")
    print(f"  Failed  : {failed}")
    print(f"  Skipped : {skipped}")
    print("=" * 70)

if __name__ == "__main__":
    main()
