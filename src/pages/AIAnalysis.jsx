import React, { useState, useRef, useEffect } from 'react';
import { Zap, Upload, Crosshair, ArrowRight, Activity, AlertCircle, User } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import ImplantSurvival from './ImplantSurvival';
import Chatbot from '../components/Chatbot';
import { supabase } from '../lib/supabaseClient';
import './AIAnalysis.css';

const AIAnalysis = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);
  const scanType = 'panoramic'; // Defaulting to panoramic or you can change to implant
  const [selectedPatient, setSelectedPatient] = useState('');
  const [patients, setPatients] = useState([]);
  const [isLoadingPatients, setIsLoadingPatients] = useState(true);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [results, setResults] = useState(null);
  const [error, setError] = useState(null);
  const [scanStatus, setScanStatus] = useState('');
  const [showSurvivalPrediction, setShowSurvivalPrediction] = useState(false);

  useEffect(() => {
    fetchPatients();
  }, []);

  const fetchPatients = async () => {
    try {
      setIsLoadingPatients(true);
      const { data, error } = await supabase
        .from('patients')
        .select('*')
        .order('created_at', { ascending: false });

      if (error) throw error;
      setPatients(data || []);
    } catch (error) {
      console.error('Error fetching patients:', error);
    } finally {
      setIsLoadingPatients(false);
    }
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
        setResults(null);
        setError(null);
        setShowSurvivalPrediction(false);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleUploadClick = () => {
    fileInputRef.current.click();
  };

  const runAnalysis = async () => {
    if (!selectedFile) {
      setError('Please upload a scan first.');
      return;
    }
    if (!selectedPatient) {
      setError('Please select a patient first.');
      return;
    }
    
    setError(null);
    setShowSurvivalPrediction(true);
  };

  if (showSurvivalPrediction) {
    const fullPatientData = patients.find(p => p.id.toString() === selectedPatient.toString()) || { name: 'Unknown Patient' };
    return (
      <>
        <ImplantSurvival 
          uploadedImage={imagePreview} 
          imageFile={selectedFile}
          patientData={fullPatientData}
          onBack={() => setShowSurvivalPrediction(false)}
        />
        <Chatbot patients={patients} selectedPatient={selectedPatient} />
      </>
    );
  }

  return (
    <div className="ai-analysis-container animation-fade-in">
      <div className="welcome-banner glass-panel mb-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h2>Global Scan Analysis</h2>
          <p className="text-muted">Upload a CBCT or Panoramic X-Ray to instantly extract clinical insights using our proprietary AI vision model.</p>
        </div>
        
        {/* Sleek Patient Selection */}
        <div className="patient-selector-card">
          <div className="patient-selector-icon">
            <User size={20} />
          </div>
          <div className="patient-selector-content">
            <label>Select Patient Record</label>
            <select 
              value={selectedPatient}
              onChange={(e) => {
                setSelectedPatient(e.target.value);
                setError(null);
              }}
              className="patient-select-input"
            >
              <option value="" disabled>-- Select a Patient --</option>
              {isLoadingPatients ? (
                <option value="" disabled>Loading patients...</option>
              ) : patients.length === 0 ? (
                <option value="" disabled>No patients found</option>
              ) : (
                patients.map(p => (
                  <option key={p.id} value={p.id}>{p.name} ({p.patient_id})</option>
                ))
              )}
            </select>
          </div>
        </div>
      </div>

      <div className="card">
        {!imagePreview ? (
          <div className="upload-zone" onClick={handleUploadClick}>
            <input 
              type="file" 
              ref={fileInputRef} 
              onChange={handleFileChange} 
              accept="image/*" 
              style={{ display: 'none' }} 
            />
            <div className="upload-icon">
              <Upload size={32} />
            </div>
            <h3>Upload CBCT or Panoramic Scan</h3>
            <p className="text-muted mt-2 mb-4">Drag and drop your image here, or click to browse files.</p>
            <span className="text-sm text-muted">Supports JPG, PNG (YOLOv8 analysis)</span>
          </div>
        ) : (
          <div className="preview-container">
            <div className="image-preview-wrapper">
              <img src={imagePreview} alt="Scan Preview" className="image-preview" />
              
              {isAnalyzing && (
                <div className="scan-overlay">
                  <div className="scan-laser"></div>
                  <div className="scan-text">{scanStatus}</div>
                </div>
              )}
            </div>

            {error && (
              <div className="mt-4 p-4 bg-danger bg-opacity-10 text-danger rounded-lg flex items-center gap-2">
                <AlertCircle size={20} />
                {error}
              </div>
            )}

            {!isAnalyzing && !results && !error && (
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem', justifyContent: 'center' }}>
                <button className="btn btn-secondary" style={{ padding: '0.75rem 1.5rem', borderRadius: '8px', fontWeight: 'bold' }} onClick={() => setImagePreview(null)}>
                  Change Image
                </button>
                <button className="btn btn-primary" style={{ padding: '0.75rem 1.5rem', borderRadius: '8px', fontWeight: 'bold', display: 'flex', alignItems: 'center' }} onClick={runAnalysis}>
                  <Zap size={18} style={{ marginRight: '8px' }} /> Run AI Analysis
                </button>
              </div>
            )}

            {results && (
              <div className="results-wrapper animation-fade-in w-full mt-6">
                <h3 className="mb-4 text-center text-success flex items-center justify-center gap-2">
                  <Activity size={24} /> AI Analysis Complete
                </h3>
                
                <div className="result-card p-6 bg-[var(--surface-color)] rounded-xl border border-[var(--border-color)]">
                  <div className="flex items-center gap-2 mb-4 text-lg font-medium text-primary">
                    <Crosshair size={20} /> Detected Features ({results.detections?.length || 0})
                  </div>
                  
                  {results.detections && results.detections.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {results.detections.map((det, index) => (
                        <div key={index} className="flex justify-between items-center p-3 rounded-lg bg-black bg-opacity-20 border border-[var(--border-color)]">
                          <span className="font-medium text-[var(--text-color)] capitalize">{det.class.replace(/-/g, ' ')}</span>
                          <span className="text-success font-semibold">
                            {Math.round(det.confidence * 100)}% Conf
                          </span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-muted p-4 text-center">
                      No significant features detected in this image for the selected model.
                    </div>
                  )}
                </div>

                <div className="flex justify-center mt-6 gap-4">
                  <button className="btn btn-secondary" onClick={() => setResults(null)}>
                    Run Another Model
                  </button>
                  <button 
                    className="btn btn-warning" 
                    onClick={() => setShowSurvivalPrediction(true)}
                  >
                    <Activity size={16} className="mr-2" /> Predict Implant Survival
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
      
      <Chatbot patients={patients} selectedPatient={selectedPatient} />
    </div>
  );
};

export default AIAnalysis;
