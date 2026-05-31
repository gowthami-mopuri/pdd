import React, { useState, useEffect } from 'react';
import { Moon, Sun, Bell, Shield, Key, User, Monitor, ChevronRight } from 'lucide-react';
import './SettingsPage.css';

const SettingsPage = () => {
  const [isDarkMode, setIsDarkMode] = useState(false);

  useEffect(() => {
    // Check local storage for theme preference
    const theme = localStorage.getItem('app-theme') || 'light';
    setIsDarkMode(theme === 'dark');
    document.documentElement.setAttribute('data-theme', theme);
  }, []);

  const toggleDarkMode = () => {
    const newTheme = isDarkMode ? 'light' : 'dark';
    setIsDarkMode(!isDarkMode);
    localStorage.setItem('app-theme', newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
  };

  return (
    <div className="settings-page animation-fade-in">
      <div className="settings-header glass-panel">
        <h2>App Settings</h2>
        <p className="text-muted">Manage your preferences and interface settings.</p>
      </div>

      <div className="settings-grid">
        <div className="settings-sidebar card">
          <div className="settings-nav-item active">
            <Monitor size={18} /> Appearance
          </div>
        </div>

        <div className="settings-content card">
          <h3 className="section-title">Appearance</h3>
          <p className="text-muted mb-6">Customize how ImplantAI looks on your device.</p>
          
          <div className="setting-option">
            <div className="setting-info">
              <div className="setting-title">
                {isDarkMode ? <Moon size={20} className="text-primary" /> : <Sun size={20} className="text-primary" />}
                Dark Mode
              </div>
              <div className="setting-desc">Switch between light and dark themes.</div>
            </div>
            
            <div className="toggle-switch" onClick={toggleDarkMode}>
              <div className={`toggle-track ${isDarkMode ? 'active' : ''}`}></div>
              <div className={`toggle-thumb ${isDarkMode ? 'active' : ''}`}></div>
            </div>
          </div>
          
        </div>
      </div>
    </div>
  );
};

export default SettingsPage;
