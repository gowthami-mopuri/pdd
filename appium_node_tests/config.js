import dotenv from 'dotenv';

dotenv.config();

export const config = {
    // Appium Server capabilities
    appiumHost: process.env.APPIUM_HOST || '127.0.0.1',
    appiumPort: parseInt(process.env.APPIUM_PORT || '4723', 10),
    appiumPath: process.env.APPIUM_PATH || '/',

    // Authentication Credentials
    testUsername: process.env.TEST_USERNAME || 'clinicaldoc',
    testPassword: process.env.TEST_PASSWORD || 'ClinicalPass123!',

    // App capabilities
    capabilities: {
        platformName: 'Android',
        'appium:automationName': 'UiAutomator2',
        'appium:appPackage': 'com.globaldental.pdd',
        'appium:appActivity': 'com.globaldental.pdd.MainActivity',
        'appium:noReset': false,
        'appium:autoGrantPermissions': true,
        'appium:newCommandTimeout': 120
    },

    // Runner configuration
    simulateMode: process.env.SIMULATE === 'true' // Set to true to run mock simulation if Appium is not available
};
