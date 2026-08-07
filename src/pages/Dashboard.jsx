import React, { useState, useEffect } from 'react';
import { Activity, Users, CheckCircle, AlertTriangle, Calendar } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../lib/supabaseClient';
import './Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const [patients, setPatients] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem('doctorUser');
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (err) {
        console.error('Failed to parse doctorUser session:', err);
      }
    }
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setIsLoading(true);
      const { data, error } = await supabase
        .from('patients')
        .select('*')
        .order('created_at', { ascending: false });

      if (error) throw error;
      setPatients(data || []);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const highRiskCount = patients.filter(p => p.risk === 'High').length;
  const pendingRiskCount = patients.filter(p => p.risk === 'Pending' || !p.risk).length;
  const consultations = patients.filter(p => p.status === 'Consultation').length;

  const doctorDisplayName = user?.username || user?.full_name || user?.email || 'Doctor';

  return (
    <div className="dashboard">
      <div className="welcome-banner glass-panel">
        <div>
          <h2>Welcome back, Dr. {doctorDisplayName}</h2>
          <p className="text-muted">Here is the latest data from your patient directory.</p>
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/patients/add')}>
          + Add New Patient
        </button>
      </div>

      <div className="stats-grid">
        <div className="stat-card card">
          <div className="stat-header">
            <span className="stat-title">Total Patients</span>
            <div className="stat-icon bg-blue"><Users size={20} /></div>
          </div>
          <div className="stat-value">{isLoading ? '...' : patients.length}</div>
          <div className="stat-change text-muted">Registered in database</div>
        </div>
        
        <div className="stat-card card">
          <div className="stat-header">
            <span className="stat-title">Active Consultations</span>
            <div className="stat-icon bg-green"><Activity size={20} /></div>
          </div>
          <div className="stat-value">{isLoading ? '...' : consultations}</div>
          <div className="stat-change text-muted">Awaiting treatment</div>
        </div>

        <div className="stat-card card">
          <div className="stat-header">
            <span className="stat-title">High Risk Cases</span>
            <div className="stat-icon bg-red"><AlertTriangle size={20} /></div>
          </div>
          <div className="stat-value">{isLoading ? '...' : highRiskCount}</div>
          <div className="stat-change text-danger">Requires careful planning</div>
        </div>

        <div className="stat-card card">
          <div className="stat-header">
            <span className="stat-title">Pending Risk Profiles</span>
            <div className="stat-icon bg-cyan"><CheckCircle size={20} /></div>
          </div>
          <div className="stat-value">{isLoading ? '...' : pendingRiskCount}</div>
          <div className="stat-change text-warning">Run AI predictions needed</div>
        </div>
      </div>

      <div className="recent-activity card mt-4">
        <h3 className="chart-title">Recently Added Patients</h3>
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Patient ID</th>
                <th>Patient Name</th>
                <th>Date Added</th>
                <th>Status</th>
                <th>Risk Profile</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan="6" className="text-center p-4">Loading data...</td>
                </tr>
              ) : patients.length === 0 ? (
                <tr>
                  <td colSpan="6" className="text-center p-4 text-muted">No patients found in the database.</td>
                </tr>
              ) : (
                patients.slice(0, 5).map((p) => (
                  <tr key={p.id}>
                    <td>{p.patient_id}</td>
                    <td className="font-medium">{p.name}</td>
                    <td className="text-muted"><Calendar size={14} className="inline mr-1"/> {new Date(p.created_at).toLocaleDateString()}</td>
                    <td>{p.status}</td>
                    <td>
                      <span className={`badge badge-${(p.risk || 'Pending').toLowerCase()}`}>{p.risk || 'Pending'}</span>
                    </td>
                    <td>
                      <button className="btn-link" onClick={() => navigate(`/patients/${p.patient_id}`)}>View Profile</button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
