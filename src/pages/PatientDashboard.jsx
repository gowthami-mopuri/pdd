import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Activity, FileText, LogOut, ShieldCheck, AlertTriangle } from 'lucide-react';
import Chatbot from '../components/Chatbot';
import './PatientDashboard.css';

const PatientDashboard = () => {
  const navigate = useNavigate();
  const [patient, setPatient] = useState(null);
  const [patientReports, setPatientReports] = useState([]);

  useEffect(() => {
    // Check authentication
    const storedPatient = localStorage.getItem('patientUser');
    if (!storedPatient) {
      navigate('/patient-login');
      return;
    }
    const parsedPatient = JSON.parse(storedPatient);
    setPatient(parsedPatient);

    // Fetch reports for this patient
    const saved = localStorage.getItem('savedReports');
    if (saved) {
      const allReports = JSON.parse(saved);
      const filtered = allReports.filter(r => r.patientId === parsedPatient.patient_id);
      setPatientReports(filtered);
    }
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('patientUser');
    navigate('/');
  };

  if (!patient) return null;

  return (
    <div className="patient-dashboard">
      <header className="patient-header">
        <div className="patient-logo">
          <div className="icon-wrapper patient-icon-header">
            <User size={24} />
          </div>
          <h1>My Health Portal</h1>
        </div>
        <button onClick={handleLogout} className="btn logout-btn">
          <LogOut size={18} /> Sign Out
        </button>
      </header>

      <main className="patient-main-content">
        <div className="welcome-section">
          <h2>Welcome back, {patient.name}</h2>
          <p className="text-muted">Manage your records and view AI insights from your doctor.</p>
        </div>

        <div className="patient-grid">
          {/* Profile Summary */}
          <div className="card patient-info-card">
            <h3 className="card-title">Patient Profile</h3>
            <div className="info-list">
              <div className="info-item">
                <span className="info-label">Patient ID</span>
                <span className="info-value font-mono">{patient.patient_id}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Age</span>
                <span className="info-value">{patient.age} years</span>
              </div>
              <div className="info-item">
                <span className="info-label">Gender</span>
                <span className="info-value">{patient.gender}</span>
              </div>
              <div className="info-item">
                <span className="info-label">Weight/Height</span>
                <span className="info-value">{patient.weight}kg / {patient.height}cm</span>
              </div>
              <div className="info-item">
                <span className="info-label">Status</span>
                <span className="info-value status-badge">{patient.status}</span>
              </div>
            </div>
          </div>

          {/* Recent AI Reports */}
          <div className="card reports-card span-full">
            <h3 className="card-title">My AI Analysis Reports</h3>
            {patientReports.length === 0 ? (
              <div className="appointment-empty">
                <FileText size={48} className="text-muted mb-4" />
                <p>No AI analysis reports have been saved for you yet.</p>
              </div>
            ) : (
              <div className="reports-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '1.5rem', marginTop: '1rem' }}>
                {patientReports.map((report) => (
                  <div key={report.id} style={{ border: '1px solid var(--border-color)', borderRadius: '12px', padding: '1rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                      <div style={{ fontWeight: '600' }}>{new Date(report.date).toLocaleDateString()}</div>
                      <div className={`report-badge ${report.riskLevel === 'LOW' ? 'badge-success' : 'badge-warning'}`} style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem', borderRadius: '4px', backgroundColor: report.riskLevel === 'LOW' ? '#dcfce7' : '#fef08a', color: report.riskLevel === 'LOW' ? '#166534' : '#854d0e', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                        {report.riskLevel === 'LOW' ? <ShieldCheck size={14} /> : <AlertTriangle size={14} />}
                        {report.riskLevel} RISK
                      </div>
                    </div>
                    
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
                      <span className="text-muted">Survival Probability</span>
                      <span style={{ fontWeight: '700', color: report.survivalProbability > 75 ? '#10b981' : '#f59e0b' }}>
                        {report.survivalProbability}%
                      </span>
                    </div>

                    <div style={{ fontSize: '0.875rem' }}>
                      <div style={{ fontWeight: '600', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Key Recommendations</div>
                      <ul style={{ paddingLeft: '1.25rem', margin: 0, color: 'var(--text-primary)' }}>
                        {report.actionItems && report.actionItems.slice(0, 3).map((item, idx) => (
                          <li key={idx} style={{ marginBottom: '0.25rem' }}>{item}</li>
                        ))}
                      </ul>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>

      <Chatbot patientData={patient} />
    </div>
  );
};

export default PatientDashboard;
