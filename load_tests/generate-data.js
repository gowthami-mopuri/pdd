const fs = require('fs');

const generateData = () => {
  let csvContent = 'id,testcase_name\n';
  for (let i = 1; i <= 350; i++) {
    csvContent += `${i},LoadTestCase_${i}\n`;
  }
  fs.writeFileSync('test-data.csv', csvContent);
  console.log('Successfully generated test-data.csv with 350 test cases.');
};

generateData();
