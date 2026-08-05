@echo off
title ImplantAI Selenium Test Suite — 410+ Tests
echo.
echo ============================================================
echo  ImplantAI Dental App — Selenium E2E Test Suite
echo  410+ Test Cases  ^|  15 Categories
echo  Report saved to: selenium_tests\reports\
echo ============================================================
echo.

REM Set credentials (or edit implantai_full_suite.py directly)
set TEST_USERNAME=clinicaldoc
set TEST_PASSWORD=ClinicalPass123!

REM Run with visible browser (set HEADLESS=false to watch)
set HEADLESS=true

echo [INFO] Installing dependencies...
pip install selenium webdriver-manager openpyxl --quiet

echo.
echo [INFO] Starting tests...
python "%~dp0implantai_full_suite.py"

echo.
echo [INFO] Done! Check the reports\ folder for the Excel report.
pause
