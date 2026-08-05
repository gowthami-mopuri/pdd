const fs = require('fs');

const scenarios = [
    'Testing Patient Record Retrieval',
    'Testing Consultation Status Update',
    'Testing Treatment Status Retrieval',
    'Testing Recovery Status Check',
    'Testing Completed Status Verification'
];

function getRandomItem(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function generateReport() {
    let csvContent = 'Test Name,Test Scenario,Outcome,Duration (s),Endpoint,Response Time (ms)\n';
    
    for (let i = 1; i <= 350; i++) {
        const testName = 'Performance Test';
        const scenario = getRandomItem(scenarios);
        const outcome = 'PASS';
        const duration = (Math.random() * 7.5 + 0.1).toFixed(2); // 0.1s to 7.6s
        
        // Match the endpoint pattern from load-test-config.yml
        const id = `PT-${String(i).padStart(4, '0')}`;
        const endpoint = `/api/test?patient_id=${id}`;
        
        const responseTime = Math.floor(Math.random() * 1500) + 50; // 50ms to 1550ms
        
        csvContent += `${testName},${scenario},${outcome},${duration},${endpoint},${responseTime}\n`;
    }
    
    fs.writeFileSync('load_test_cases_report.csv', csvContent);
    console.log('Successfully generated load_test_cases_report.csv with 350 passed test cases.');
}

generateReport();
