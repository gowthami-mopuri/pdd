import React, { useState, useEffect } from 'react';
import { Search, Filter, Plus, FileText, Activity, MoreVertical, Eye } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../lib/supabaseClient';
import './Patients.css';



const Patients = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [patients, setPatients] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchPatients();
  }, []);

  const fetchPatients = async () => {
    try {
      setIsLoading(true);
      const { data, error } = await supabase
        .from('patients')
        .select('*')
        .order('created_at', { ascending: false });

      if (error) throw error;
      setPatients(data || []);
    } catch (error) {
      console.error('Error fetching patients:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddPatient = () => {
    // Navigate to the add patient wizard
    navigate('/patients/add');
  };

  return (
    <div className="patients-page">
      <div className="page-header">
        <div>
          <h2>Patient Directory</h2>
          <p className="text-muted">Manage patient records, medical histories, and implant treatments.</p>
        </div>
        <button className="btn btn-primary" onClick={handleAddPatient}>
          <Plus size={16} className="mr-2" /> Add New Patient
        </button>
      </div>

      <div className="card list-container">
        <div className="list-toolbar">
          <div className="search-bar">
            <Search size={18} className="text-muted" />
            <input 
              type="text" 
              placeholder="Search by name or ID..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="search-input"
            />
          </div>
          <button className="btn btn-secondary btn-icon">
            <Filter size={16} className="mr-2" /> Filter
          </button>
        </div>

        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Patient Name / ID</th>
                <th>Demographics</th>
                <th>Last Visit</th>
                <th>Treatment Status</th>
                <th>Implant Risk</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan="6" style={{textAlign: 'center', padding: '2rem'}}>Loading patient data...</td>
                </tr>
              ) : patients.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{textAlign: 'center', padding: '2rem', color: 'var(--text-muted)'}}>
                    No patients found. Click "Add New Patient" to get started!
                  </td>
                </tr>
              ) : patients.filter(p => p.name.toLowerCase().includes(searchTerm.toLowerCase()) || p.patient_id.toLowerCase().includes(searchTerm.toLowerCase())).map((patient) => (
                <tr key={patient.id}>
                  <td>
                    <div className="patient-name-cell">
                      <div className="patient-avatar">{patient.name.charAt(0)}</div>
                      <div>
                        <div className="font-medium">{patient.name}</div>
                        <div className="text-muted" style={{fontSize: '0.75rem'}}>{patient.patient_id}</div>
                      </div>
                    </div>
                  </td>
                  <td>{patient.age} yrs • {patient.gender}</td>
                  <td>{patient.last_visit ? new Date(patient.last_visit).toLocaleDateString() : 'N/A'}</td>
                  <td>
                    <span className="status-indicator">
                      <span className={`status-dot ${patient.status === 'Consultation' ? 'bg-blue' : patient.status === 'Post-Op Care' ? 'bg-green' : 'bg-orange'}`}></span>
                      {patient.status}
                    </span>
                  </td>
                  <td>
                    <span className={`badge badge-${(patient.risk || 'Medium').toLowerCase()}`}>{patient.risk} Risk</span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button className="icon-btn tooltip" data-tip="View Patient" onClick={() => navigate(`/patients/${patient.patient_id}`)}>
                        <Eye size={18} />
                      </button>
                      <button className="icon-btn tooltip" data-tip="Medical History" onClick={() => navigate(`/patients/${patient.patient_id}?tab=history`)}>
                        <FileText size={18} />
                      </button>
                      <button className="icon-btn tooltip" data-tip="New Prediction" onClick={() => navigate(`/patients/${patient.patient_id}?tab=predictions`)}>
                        <Activity size={18} />
                      </button>
                      <button className="icon-btn">
                        <MoreVertical size={18} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        
        <div className="pagination">
          <span className="text-muted">Showing {patients.length > 0 ? 1 : 0} to {patients.length} of {patients.length} entries</span>
          <div className="page-controls">
            <button className="btn btn-secondary" disabled>Previous</button>
            <button className="btn btn-secondary" disabled>Next</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Patients;
