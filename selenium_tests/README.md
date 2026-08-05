# ImplantAI — Selenium Full Test Suite (410+ Test Cases)

## Overview

`implantai_full_suite.py` is a complete, self-contained Selenium end-to-end test
suite for the **ImplantAI dental web application** at `https://pdd-zfqq.onrender.com`.

It covers **410+ unique test cases** across **15 test categories**, automatically
generates a detailed **Excel report** (5 sheets + charts), and saves it in the
`selenium_tests/reports/` folder.

---

## Test Categories & Coverage

| # | Category | TCs | What's Tested |
|---|---|---|---|
| CAT-01 | UI/UX Testing | TC_001–025 | Layout, cards, headings, forms, dark mode |
| CAT-02 | Functional Testing | TC_026–060 | Login, search, CRUD, navigation, AI scan |
| CAT-03 | Unit-Level Testing | TC_061–085 | Individual components (tabs, search, canvas, LS) |
| CAT-04 | Validation Testing | TC_086–115 | Form validation, XSS, SQL injection, boundary values |
| CAT-05 | Security Testing | TC_116–135 | HTTPS, route guards, XSS, cookies, rate limiting |
| CAT-06 | Performance Testing | TC_136–155 | Load times, heap memory, resource size, FCP |
| CAT-07 | Accessibility Testing | TC_156–175 | Alt text, headings, tab order, responsive breakpoints |
| CAT-08 | Deployment/Status Testing | TC_176–195 | HTTP 200, SSL, Supabase, Render.com live status |
| CAT-09 | AI Scan & Chat Testing | TC_196–230 | Upload scan, run analysis, survival prediction, Gemini chat |
| CAT-10 | Reports & Patient Data | TC_231–265 | Report CRUD, patient detail, dashboard, data integrity |
| CAT-11 | Integration & E2E Journeys | TC_266–310 | 10 full user journeys, multi-tab, ML integration |
| CAT-12 | Data Integrity Testing | TC_311–335 | ID formats, null/NaN checks, schema fields, Supabase |
| CAT-13 | Browser Compatibility | TC_336–355 | ES6+, CSS Grid, Canvas, Fetch, localStorage APIs |
| CAT-14 | Mobile Responsiveness | TC_356–375 | 10 device sizes, viewport meta, no overflow |
| CAT-15 | Edge Case & Stress Testing | TC_376–410 | Rapid refresh, Unicode, large files, zoom, keyboard spam |

---

## Excel Report Sheets

| Sheet | Contents |
|---|---|
| **Executive Summary** | KPI cards (total/pass/fail/skip/pass-rate), meta info, category breakdown |
| **Detailed Results** | All 410+ rows — TC ID, name, category, status, duration, message |
| **Charts & Analysis** | Grouped bar chart (by category) + overall pass-rate pie chart |
| **❌ Failed Tests** | Only failed tests, red-highlighted for quick triage |
| **⚠️ Skipped Tests** | Only skipped tests, amber-highlighted for investigation |

---

## How to Run

### Option 1 — Double-click (Windows)
```
Double-click:  selenium_tests\run_tests.bat
```

### Option 2 — Command line
```powershell
cd "c:\Users\Lenovo\Documents\dental_project\project 6"
pip install selenium webdriver-manager openpyxl
python selenium_tests\implantai_full_suite.py
```

### Option 3 — With visible browser
```powershell
$env:HEADLESS = "false"
python selenium_tests\implantai_full_suite.py
```

### Option 4 — Custom credentials
```powershell
$env:TEST_USERNAME = "clinicaldoc"
$env:TEST_PASSWORD = "ClinicalPass123!"
python selenium_tests\implantai_full_suite.py
```

---

## Report Location

After each run, a timestamped Excel file is saved:
```
selenium_tests\reports\ImplantAI_Report_2026-XX-XXT00-00-00.xlsx
```

---

## Requirements

```
selenium
webdriver-manager
openpyxl
```

ChromeDriver is auto-installed via `webdriver-manager` — no manual setup needed.

---

## Notes

- First run may be slow (Render.com cold start takes ~15–30s)
- SKIP status means an element was not found — not a test framework failure
- Set `HEADLESS=false` to watch the browser during tests
- The suite re-authenticates automatically if session expires during testing
