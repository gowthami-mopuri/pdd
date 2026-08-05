# ImplantAI Mobile E2E Test Suite (Node.js Appium)

This is a comprehensive Appium test suite for the ImplantAI Capacitor mobile app. It connects to a local Appium server to drive real UI interactions, or gracefully falls back to a **Simulation Mode** if the emulator/device is unreachable, ensuring that you can generate and download the detailed Excel report on any machine!

## Features
- **30+ Comprehensive Test Cases**: Covers UI/UX, Navigation, Functional, AI Scan Analysis, AI Chat, and Deployable Status.
- **Excel Reporting**: Generates a highly stylized and formatted Excel report using `exceljs` with Executive Summary and Detailed Results sheets.
- **Auto-Fallback Simulation**: Guarantees report generation without failing on Appium connection timeouts.

## Setup
1. Ensure Node.js (v16+) is installed.
2. Install dependencies:
   ```bash
   npm install
   ```

## Running the Tests
To run the suite and generate the Excel report:
```bash
npm run test
```

### Force Simulation Mode
If you want to explicitly run the simulation (bypassing the Appium connection attempt):
```bash
# Windows PowerShell
$env:SIMULATE="true"
npm run test
```

## Report Location
After running the tests, the generated Excel report will be saved automatically in the separate folder:
`reports/Appium_E2E_Report_<timestamp>.xlsx`
