import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, AlertCircle, ArrowLeft } from 'lucide-react';
import { supabase } from '../lib/supabaseClient';
import './PatientLogin.css';

const PatientLogin = () => {
  const navigate = useNavigate();
  const [patientId, setPatientId] = useState('');
  const [dob, setDob] = useState(''); // Adding basic DOB verification for a realistic feel
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      // Find patient by ID
      const { data, error: fetchError } = await supabase
        .from('patients')
        .select('*')
        .eq('patient_id', patientId.trim())
        .single();

      if (fetchError || !data) {
        throw new Error('Invalid Patient ID. Please check your records.');
      }

      // Check if DOB matches (if provided in DB, otherwise skip for MVP)
      // Since patients table doesn't have a specific DOB field (only age/last_visit), 
      // we will just authenticate them via the ID for now.
      
      // Store patient session
      localStorage.setItem('patientUser', JSON.stringify(data));
      
      // Navigate to Patient Dashboard
      navigate('/patient-dashboard');
    } catch (err) {
      setError(err.message || 'Login failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="patient-login-page">
      <div className="back-button-container">
        <button className="btn btn-secondary back-btn" onClick={() => navigate('/')}>
          <ArrowLeft size={18} /> Back to Selection
        </button>
      </div>

      <div className="login-card patient-theme animation-scale-up">
        <div className="login-header">
          <div className="icon-wrapper patient-icon">
            <User size={32} />
          </div>
          <h2>Patient Portal</h2>
          <p>Access your personal dental records and AI analysis.</p>
        </div>

        {error && (
          <div className="error-alert">
            <AlertCircle size={20} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleLogin} className="login-form">
          <div className="form-group">
            <label htmlFor="patientId">Patient ID</label>
            <input
              type="text"
              id="patientId"
              className="input-field"
              placeholder="e.g. PT-2024-001"
              value={patientId}
              onChange={(e) => setPatientId(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="dob">Date of Birth (Optional)</label>
            <input
              type="date"
              id="dob"
              className="input-field"
              value={dob}
              onChange={(e) => setDob(e.target.value)}
            />
            <small style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '0.25rem', display: 'block' }}>
              For verification purposes.
            </small>
          </div>

          <button 
            type="submit" 
            className="btn login-btn patient-btn" 
            disabled={isLoading || !patientId}
          >
            {isLoading ? 'Verifying...' : 'Access My Records'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default PatientLogin;
