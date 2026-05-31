import React, { useState, useEffect } from 'react';
import { FileText, Download, Trash2, ShieldCheck, AlertTriangle } from 'lucide-react';
import './Reports.css';

const Reports = () => {
  const [reports, setReports] = useState([]);

  useEffect(() => {
    const saved = localStorage.getItem('savedReports');
    if (saved) {
      setReports(JSON.parse(saved));
    }
  }, []);

  const handleDelete = (id) => {
    if (window.confirm('Are you sure you want to delete this saved report?')) {
      const updated = reports.filter(r => r.id !== id);
      setReports(updated);
      localStorage.setItem('savedReports', JSON.stringify(updated));
    }
  };

  return (
    <div className="reports-page animation-fade-in">
      <div className="welcome-banner glass-panel mb-8">
        <h2>Medical Reports</h2>
        <p className="text-muted">Review and manage saved AI Implant Survival Analysis reports.</p>
      </div>

      {reports.length === 0 ? (
        <div className="empty-state">
          <FileText size={64} color="#94a3b8" style={{ marginBottom: '1rem' }} />
          <h3>No Reports Saved Yet</h3>
          <p className="text-muted text-center max-w-md mx-auto mt-2">
            Run an AI Analysis and click "Save to Reports" to securely store your patient analysis results here for future clinical review.
          </p>
        </div>
      ) : (
        <div className="reports-grid">
          {reports.map((report) => (
            <div key={report.id} className="report-card">
              
              <div className="report-header">
                <div>
                  <div className="report-patient-name">{report.patientName}</div>
                  <div className="report-patient-id">ID: {report.patientId}</div>
                </div>
                <div className={`report-badge ${report.riskLevel === 'LOW' ? 'badge-success' : 'badge-warning'}`}>
                  {report.riskLevel === 'LOW' ? <ShieldCheck size={14} /> : <AlertTriangle size={14} />}
                  {report.riskLevel} RISK
                </div>
              </div>
              
              <div className="report-body">
                <div className="report-stat-row">
                  <span className="report-stat-label">Survival Probability</span>
                  <span className={`report-stat-value ${report.survivalProbability > 75 ? 'success' : 'warning'}`}>
                    {report.survivalProbability}%
                  </span>
                </div>
                
                <div className="report-stat-row">
                  <span className="report-stat-label">Date Saved</span>
                  <span className="report-stat-value neutral">
                    {new Date(report.date).toLocaleDateString()}
                  </span>
                </div>
                
                <div className="report-actions-list-container">
                  <div className="report-actions-title">Key Action Items</div>
                  <ul className="report-actions-list">
                    {report.actionItems && report.actionItems.slice(0, 2).map((item, idx) => (
                      <li key={idx}>{item}</li>
                    ))}
                    {report.actionItems && report.actionItems.length > 2 && (
                      <li className="more-items">+{report.actionItems.length - 2} additional recommendations</li>
                    )}
                  </ul>
                </div>
              </div>

              <div className="report-footer">
                <button className="btn-delete-report" onClick={() => handleDelete(report.id)} title="Delete this report">
                  <Trash2 size={16} /> Delete Report
                </button>
              </div>
              
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Reports;
