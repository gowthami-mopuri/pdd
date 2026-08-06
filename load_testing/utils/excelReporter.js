const ExcelJS = require('exceljs');
const path = require('path');

class LoadTestExcelReporter {
    constructor(reportName = 'Load_Testing_Analysis_Report') {
        this.workbook = new ExcelJS.Workbook();
        this.sheet = this.workbook.addWorksheet('Test Case Results');
        this.reportName = reportName;
        
        // Setup columns for individual test cases
        this.sheet.columns = [
            { header: 'Test ID', key: 'id', width: 15 },
            { header: 'Endpoint / Payload', key: 'desc', width: 50 },
            { header: 'Requests Fired', key: 'count', width: 15 },
            { header: 'Avg Response Time (ms)', key: 'avgTime', width: 25 },
            { header: 'Status', key: 'status', width: 15 }
        ];
        
        this.sheet.getRow(1).font = { bold: true };

        this.metrics = {
            totalRequests: 0,
            startTime: null,
            endTime: null,
            minResponse: Infinity,
            maxResponse: 0,
            responseTimes: []
        };
        
        this.testCases = {};
    }

    startTest() {
        this.metrics.startTime = Date.now();
    }

    endTest() {
        this.metrics.endTime = Date.now();
    }

    logRequest(testId, description, responseTimeMs, isSuccess = true) {
        this.metrics.totalRequests++;
        this.metrics.responseTimes.push(responseTimeMs);
        if (responseTimeMs < this.metrics.minResponse) this.metrics.minResponse = responseTimeMs;
        if (responseTimeMs > this.metrics.maxResponse) this.metrics.maxResponse = responseTimeMs;

        if (!this.testCases[testId]) {
            this.testCases[testId] = {
                desc: description,
                count: 0,
                totalTime: 0,
                status: 'PASS' // User requested all to pass
            };
        }
        
        this.testCases[testId].count++;
        this.testCases[testId].totalTime += responseTimeMs;
        if (!isSuccess) {
            this.testCases[testId].status = 'FAIL';
        }
    }

    async save() {
        // Populate the primary sheet with the 300+ aggregated test cases
        for (const [id, data] of Object.entries(this.testCases)) {
            const avgTime = (data.totalTime / data.count).toFixed(2);
            this.sheet.addRow({
                id: id,
                desc: data.desc,
                count: data.count,
                avgTime: avgTime,
                status: data.status
            });
        }

        // Calculate global metrics
        const totalDurationSec = (this.metrics.endTime - this.metrics.startTime) / 1000;
        const rps = (this.metrics.totalRequests / totalDurationSec).toFixed(2);
        
        const totalResponseTime = this.metrics.responseTimes.reduce((a, b) => a + b, 0);
        const globalAvgResponse = this.metrics.responseTimes.length > 0 
            ? (totalResponseTime / this.metrics.responseTimes.length).toFixed(2) 
            : 0;

        const summarySheet = this.workbook.addWorksheet('Load Summary');
        summarySheet.columns = [
            { header: 'Metric', key: 'metric', width: 35 },
            { header: 'Value', key: 'value', width: 25 }
        ];
        summarySheet.getRow(1).font = { bold: true };
        
        summarySheet.addRow({ metric: 'Test Duration (seconds)', value: totalDurationSec });
        summarySheet.addRow({ metric: 'Total Requests Fired', value: this.metrics.totalRequests });
        summarySheet.addRow({ metric: 'Requests Per Second (RPS)', value: rps });
        summarySheet.addRow({ metric: 'Minimum Response Time (ms)', value: this.metrics.minResponse === Infinity ? 0 : this.metrics.minResponse });
        summarySheet.addRow({ metric: 'Average Response Time (ms)', value: globalAvgResponse });
        summarySheet.addRow({ metric: 'Maximum Response Time (ms)', value: this.metrics.maxResponse });
        summarySheet.addRow({ metric: 'Virtual Users', value: 100 });
        
        const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
        const filename = `${this.reportName}_${timestamp}.xlsx`;
        const filepath = path.join(__dirname, '..', filename);
        await this.workbook.xlsx.writeFile(filepath);
        console.log(`Excel Load Report saved to: ${filepath}`);
        
        // Print Summary to Console
        console.log(`\n--- LOAD TEST SUMMARY ---`);
        console.log(`Total Requests: ${this.metrics.totalRequests}`);
        console.log(`RPS: ${rps} req/sec`);
        console.log(`Avg Response: ${globalAvgResponse}ms`);
        console.log(`Min Response: ${this.metrics.minResponse === Infinity ? 0 : this.metrics.minResponse}ms`);
        console.log(`Max Response: ${this.metrics.maxResponse}ms`);
        console.log(`-------------------------\n`);
    }
}

module.exports = LoadTestExcelReporter;
