# ImplantAI — Selenium E2E Test Suite (315 Tests)

## Overview

`test_implantai_300_e2e.py` is a comprehensive end-to-end Selenium test suite
covering every major feature and flow of the ImplantAI dental web application.

**URL:** https://pdd-zfqq.onrender.com  
**Total Test Cases:** 315 (across 20 categories)  
**Report Format:** Excel (.xlsx) with 5 sheets

---

## Test Categories

| # | Category | Tests |
|---|----------|-------|
| CAT-01 | App Launch & Landing Page | TC_001–TC_015 |
| CAT-02 | Login & Authentication | TC_016–TC_035 |
| CAT-03 | Navigation & Sidebar | TC_036–TC_055 |
| CAT-04 | Patient List | TC_056–TC_075 |
| CAT-05 | Add / Edit Patient Form | TC_076–TC_095 |
| CAT-06 | Patient Detail Page | TC_096–TC_115 |
| CAT-07 | AI Scan Analysis | TC_116–TC_135 |
| CAT-08 | Implant Survival Prediction | TC_136–TC_155 |
| CAT-09 | AI Chat Assistant | TC_156–TC_170 |
| CAT-10 | Reports & PDF Export | TC_171–TC_190 |
| CAT-11 | Dashboard & Analytics | TC_191–TC_205 |
| CAT-12 | Settings & Profile | TC_206–TC_220 |
| CAT-13 | Security | TC_221–TC_235 |
| CAT-14 | Performance | TC_236–TC_250 |
| CAT-15 | Accessibility & UI/UX | TC_251–TC_265 |
| CAT-16 | Data Persistence (Supabase) | TC_266–TC_275 |
| CAT-17 | Edge Cases & Error Handling | TC_276–TC_285 |
| CAT-18 | Treatment & Appointments | TC_286–TC_295 |
| CAT-19 | Implant Details & Clinical Data | TC_296–TC_305 |
| CAT-20 | End-to-End User Journeys | TC_306–TC_315 |

---

## Requirements

```
pip install selenium webdriver-manager openpyxl
```

Or use the project requirements file:

```
pip install -r requirements-test.txt
```

---

## Running the Tests

### Default (headless, uses env vars for credentials)

```powershell
cd "c:\Users\Lenovo\Documents\dental_project\project 6"
python e2e_tests\test_implantai_300_e2e.py
```

### With credentials as environment variables

```powershell
$env:TEST_USERNAME = "clinicaldoc"
$env:TEST_PASSWORD = "ClinicalPass123!"
python e2e_tests\test_implantai_300_e2e.py
```

### Run with visible browser (non-headless)

```powershell
$env:HEADLESS = "false"
python e2e_tests\test_implantai_300_e2e.py
```

---

## Excel Report

After each run a timestamped Excel file is saved in `e2e_tests/`:

```
E2E_Test_Report_ImplantAI_2026-XX-XXT00-00-00.xlsx
```

### Report Sheets

| Sheet | Contents |
|-------|----------|
| **Executive Summary** | KPI cards (total/pass/fail/skip/pass-rate), category breakdown table |
| **Detailed Results** | All 315 test cases with ID, name, status, duration, message, expected, actual |
| **Charts** | Bar chart (pass/fail/skip by category) + Pie chart (overall pass rate) |
| **Failed Tests** | Only failed tests, red highlighted — for quick triage |
| **Skipped Tests** | Only skipped tests — features that need investigation |

---

## Credentials

Edit the top of the file or set environment variables:

```python
TEST_USERNAME = "clinicaldoc"      # line 35
TEST_PASSWORD = "ClinicalPass123!" # line 36
```

---

## Notes

- The suite auto-installs ChromeDriver via `webdriver-manager`
- Tests run sequentially; each category restores auth if needed
- Skipped tests indicate UI elements not found (not test framework failures)
- The first run on a cold Render.com server may be slow (spin-up delay)
