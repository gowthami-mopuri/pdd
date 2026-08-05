const fs = require('fs');

const firstNames = ['James', 'Mary', 'John', 'Patricia', 'Robert', 'Jennifer', 'Michael', 'Linda', 'William', 'Elizabeth', 'David', 'Barbara', 'Richard', 'Susan', 'Joseph', 'Jessica', 'Thomas', 'Sarah', 'Charles', 'Karen'];
const lastNames = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Rodriguez', 'Martinez', 'Hernandez', 'Lopez', 'Gonzales', 'Wilson', 'Anderson', 'Thomas', 'Taylor', 'Moore', 'Jackson', 'Martin'];
const genders = ['Male', 'Female', 'Other'];
const statuses = ['Consultation', 'Treatment', 'Recovery', 'Completed'];
const medicalHistories = ['None', 'Diabetes', 'Smoking', 'Hypertension', 'Asthma', 'Heart Disease', 'Allergy to Penicillin', 'Previous Implant Failure'];

function getRandomItem(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

const generateData = () => {
    let csvContent = 'patient_id,name,age,gender,medical_history,status,weight_kg,height_cm,Test_Result\n';
    
    for (let i = 1; i <= 350; i++) {
        // Generate realistic mock data
        const id = `PT-${String(i).padStart(4, '0')}`;
        const name = `${getRandomItem(firstNames)} ${getRandomItem(lastNames)}`;
        const age = Math.floor(Math.random() * (85 - 18 + 1)) + 18; // 18 to 85
        const gender = getRandomItem(genders);
        const history = getRandomItem(medicalHistories);
        const status = getRandomItem(statuses);
        const weight = Math.floor(Math.random() * (120 - 50 + 1)) + 50; // 50kg to 120kg
        const height = Math.floor(Math.random() * (195 - 150 + 1)) + 150; // 150cm to 195cm
        
        csvContent += `${id},${name},${age},${gender},${history},${status},${weight},${height},PASSED\n`;
    }
    
    fs.writeFileSync('test-data-passed.csv', csvContent);
    console.log('Successfully generated detailed test-data-passed.csv with 350 realistic patient records.');
};

generateData();
