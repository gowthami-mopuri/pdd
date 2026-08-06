const ExcelJS = require('exceljs');
const path = require('path');

class ExcelReporter {
    constructor(reportName = 'Mobile_Appium_Test_Report') {
        this.workbook = new ExcelJS.Workbook();
        this.sheet = this.workbook.addWorksheet('Test Results');
        this.reportName = reportName;
        
        // Setup columns
        this.sheet.columns = [
            { header: 'Test ID', key: 'id', width: 15 },
            { header: 'Category', key: 'category', width: 20 },
            { header: 'Description', key: 'desc', width: 50 },
            { header: 'Status', key: 'status', width: 15 },
            { header: 'Error Details', key: 'error', width: 50 }
        ];
        
        this.sheet.getRow(1).font = { bold: true };

        this.summary = {
            total: 0,
            passed: 0,
            failed: 0
        };
    }

    addResult(id, category, desc, status, error = '') {
        this.sheet.addRow({ id, category, desc, status, error });
        this.summary.total += 1;
        if (status.toUpperCase() === 'PASS') {
            this.summary.passed += 1;
        } else {
            this.summary.failed += 1;
        }
    }

    async save() {
        const summarySheet = this.workbook.addWorksheet('Summary');
        summarySheet.columns = [
            { header: 'Metric', key: 'metric', width: 25 },
            { header: 'Count', key: 'count', width: 15 }
        ];
        summarySheet.getRow(1).font = { bold: true };
        
        summarySheet.addRow({ metric: 'Total Test Cases', count: this.summary.total });
        summarySheet.addRow({ metric: 'Total Passed', count: this.summary.passed });
        summarySheet.addRow({ metric: 'Total Failed', count: this.summary.failed });
        
        const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
        const filename = `${this.reportName}_${timestamp}.xlsx`;
        const filepath = path.join(__dirname, '..', filename);
        await this.workbook.xlsx.writeFile(filepath);
        console.log(`Excel report saved to: ${filepath}`);
    }
}

module.exports = ExcelReporter;
