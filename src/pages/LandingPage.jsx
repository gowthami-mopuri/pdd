import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck, Stethoscope, Hexagon, User } from 'lucide-react';
import './LandingPage.css';

const LandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="landing-page animation-fade-in">
      <div className="landing-content">
        <div className="landing-header">
          <div className="landing-logo">
            <Hexagon size={48} color="var(--primary-color)" />
          </div>
          <h1>ImplantAI Ecosystem</h1>
          <p className="landing-subtitle">Predictive analytics and clinical management platform.</p>
        </div>
        
        <div className="role-cards-container">
          
          <div 
            className="role-card admin-card"
            onClick={() => navigate('/admin-login')}
          >
            <div className="card-icon-wrapper">
              <ShieldCheck size={36} />
            </div>
            <h2>Administrator</h2>
            <p>System configuration, user management, and security controls.</p>
          </div>
          
          <div 
            className="role-card clinical-card"
            onClick={() => navigate('/login')}
          >
            <div className="card-icon-wrapper">
              <Stethoscope size={36} />
            </div>
            <h2>Clinical Staff</h2>
            <p>Patient management, AI analysis, and medical reports.</p>
          </div>

          <div 
            className="role-card patient-card"
            onClick={() => navigate('/patient-login')}
          >
            <div className="card-icon-wrapper">
              <User size={36} color="var(--secondary-color)" />
            </div>
            <h2>Patient Portal</h2>
            <p>View your personal profile, appointments, and status.</p>
          </div>
          
        </div>
        
        <div className="landing-footer">
          <p>© 2026 Dental AI Systems. All rights reserved.</p>
        </div>
      </div>
    </div>
  );
};

export default LandingPage;
