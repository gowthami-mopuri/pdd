# Baseline / Load Testing Suite

This directory contains the load tests for validating the system performance under a normal expected load of 100 concurrent users.

## Tools Used
- **Artillery**: A modern, powerful, and easy-to-use load testing toolkit for Node.js.

## Test Configuration
The load test is configured in `load-test-config.yml`.
- **Target URL**: `http://localhost:3000` (Update this in the `yml` file or pass via command line parameter to point to the actual environment)
- **Duration**: 1 minute (60 seconds)
- **Concurrency/Load**: Simulates 100 new request sessions per second (arrivalRate: 100).
- **Test Cases**: The payload data is driven by `test-data.csv` which contains 350 testcases. Each case is injected dynamically into the HTTP requests.

## How to Run

1. Open your terminal in this directory (`load_tests`).
2. Ensure you have installed the dependencies:
   ```bash
   npm install
   ```
3. Run the load test:
   ```bash
   npm run test:load
   ```

## What to Look For (Interpreting Results)
Once the test finishes (after 1 minute), Artillery will output a summary block. Look for these metrics:

- **http.requests**: The total number of requests sent out (thousands).
- **http.request_rate**: Requests per second (RPS) handled during the test.
- **http.response_time**:
  - **min**: The fastest response time (e.g., 50ms)
  - **max**: The slowest response time (e.g., 1500ms)
  - **median / p95 / p99**: Useful metrics for the average response times.
- **vusers.completed**: Total number of virtual users who successfully completed their scenario.
- **errors**: Any failed requests will be listed here.
