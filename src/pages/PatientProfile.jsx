import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { supabase } from '../lib/supabaseClient';
import { ArrowLeft, Edit, Calendar, MapPin, Activity, FileText, Settings, ShieldAlert, CheckCircle, Zap } from 'lucide-react';
import './PatientProfile.css';

const PatientProfile = () => {
  const { id } = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const [patient, setPatient] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  
  // AI Prediction State
  const [isPredicting, setIsPredicting] = useState(false);
  const [predictionResult, setPredictionResult] = useState(null);
  const [loadingStep, setLoadingStep] = useState(0);

  const activeTab = searchParams.get('tab') || 'overview';

  useEffect(() => {
    fetchPatientData();
  }, [id]);

  const fetchPatientData = async () => {
    try {
      setIsLoading(true);
      const { data, error } = await supabase
        .from('patients')
        .select('*')
        .eq('patient_id', id)
        .single();

      if (error) throw error;
      setPatient(data);
    } catch (error) {
      console.error('Error fetching patient profile:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleTabChange = (tab) => {
    setSearchParams({ tab });
  };

  const runAIPrediction = () => {
    setIsPredicting(true);
    setLoadingStep(0);
    
    // Simulate AI loading steps
    const steps = [
      'Extracting clinical parameters...',
      'Analyzing bone density and site factors...',
      'Evaluating systemic risk indicators...',
      'Calculating survival probability matrix...'
    ];
    
    let currentStep = 0;
    const interval = setInterval(() => {
      currentStep++;
      if (currentStep < steps.length) {
        setLoadingStep(currentStep);
      } else {
        clearInterval(interval);
        calculatePrediction();
      }
    }, 1200);
  };

  const calculatePrediction = () => {
    if (!patient || !patient.clinical_data) return;
    const clinical = patient.clinical_data;
    
    // Base survival probability
    let baseScore = 95.0;
    let factors = [];
    
    // Analyze Systemic Conditions
    if (clinical.conditions && clinical.conditions.includes('diabetes')) {
      if (clinical.diabetesStatus === 'Type 1' || clinical.hba1c > 8) {
        baseScore -= 6.5;
        factors.push({ name: 'Poorly Controlled Diabetes', impact: -6.5, type: 'negative' });
      } else {
        baseScore -= 3.0;
        factors.push({ name: 'Controlled Diabetes', impact: -3.0, type: 'negative' });
      }
    }
    
    // Analyze Smoking
    if (clinical.smokingStatus === 'Heavy Smoker' || clinical.smokingStatus === 'Smoker') {
      baseScore -= 5.5;
      factors.push({ name: 'Smoking (Vasoconstriction Risk)', impact: -5.5, type: 'negative' });
    } else if (clinical.smokingStatus === 'Former Smoker') {
      baseScore -= 2.0;
      factors.push({ name: 'Former Smoker', impact: -2.0, type: 'negative' });
    }
    
    // Analyze Bone Density
    if (clinical.boneDensityClass === 'Type 1 - Dense Bone') {
      baseScore += 2.0;
      factors.push({ name: 'Excellent Bone Density (Type 1)', impact: +2.0, type: 'positive' });
    } else if (clinical.boneDensityClass === 'Type 4 - Soft Bone') {
      baseScore -= 4.5;
      factors.push({ name: 'Poor Primary Stability (Type 4)', impact: -4.5, type: 'negative' });
    } else if (clinical.boneDensityClass) {
      baseScore += 1.0;
      factors.push({ name: 'Adequate Bone Quality', impact: +1.0, type: 'positive' });
    }
    
    // Additional Site Factors
    if (clinical.siteFactors && clinical.siteFactors.includes('bone_grafting')) {
      baseScore -= 2.5;
      factors.push({ name: 'Concurrent Bone Grafting Required', impact: -2.5, type: 'negative' });
    }
    if (clinical.siteFactors && clinical.siteFactors.includes('periodontal_disease')) {
      baseScore -= 4.0;
      factors.push({ name: 'History of Periodontal Disease', impact: -4.0, type: 'negative' });
    }

    // Clamp score to max 99.5%
    const finalScore = Math.min(Math.max(baseScore, 40), 99.5);
    
    // Determine Risk Level
    let riskLevel = 'Low';
    if (finalScore < 80) riskLevel = 'High';
    else if (finalScore < 90) riskLevel = 'Medium';
    
    setPredictionResult({
      score: finalScore.toFixed(1),
      factors: factors.sort((a, b) => Math.abs(b.impact) - Math.abs(a.impact)),
      riskLevel: riskLevel,
      confidence: (85 + Math.random() * 10).toFixed(1)
    });
    
    setIsPredicting(false);
  };

  if (isLoading) {
    return <div className="profile-loading"><div className="spinner"></div><p>Loading patient profile...</p></div>;
  }

  if (!patient) {
    return (
      <div className="profile-not-found">
        <h2>Patient Not Found</h2>
        <p className="text-muted">Could not locate patient record {id}.</p>
        <button className="btn btn-primary mt-4" onClick={() => navigate('/patients')}>Back to Directory</button>
      </div>
    );
  }

  const clinical = patient.clinical_data || {};

  return (
    <div className="patient-profile">
      {/* Header Actions */}
      <div className="profile-header-actions">
        <button className="btn btn-secondary" onClick={() => navigate('/patients')}>
          <ArrowLeft size={16} className="mr-2" /> Back to Directory
        </button>
        <div className="header-right">
          <button className="btn btn-primary" onClick={() => { handleTabChange('predictions'); if (!predictionResult && !isPredicting) runAIPrediction(); }}>
            <Zap size={16} className="mr-2" /> Run AI Prediction
          </button>
          <button className="icon-btn tooltip" data-tip="Edit Patient">
            <Edit size={18} />
          </button>
        </div>
      </div>

      {/* Main Profile Card */}
      <div className="card profile-main-card">
        <div className="profile-hero">
          <div className="profile-avatar-large">{patient.name.charAt(0)}</div>
          <div className="profile-info">
            <div className="profile-title-row">
              <h2>{patient.name}</h2>
              <span className={`badge badge-${(patient.risk || 'Medium').toLowerCase()}`}>{patient.risk} Risk</span>
            </div>
            <div className="profile-demographics">
              <span>{patient.patient_id}</span>
              <span className="dot-separator">•</span>
              <span>{patient.age} yrs, {patient.gender}</span>
              <span className="dot-separator">•</span>
              <span>{patient.height}cm, {patient.weight}kg</span>
            </div>
            <div className="profile-meta">
              <span className="meta-item"><Calendar size={14}/> Last Visit: {new Date(patient.last_visit).toLocaleDateString()}</span>
              <span className="meta-item"><Activity size={14}/> Status: {patient.status}</span>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="profile-tabs">
          <button className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => handleTabChange('overview')}>
            Overview
          </button>
          <button className={`tab-btn ${activeTab === 'history' ? 'active' : ''}`} onClick={() => handleTabChange('history')}>
            Medical History
          </button>
          <button className={`tab-btn ${activeTab === 'predictions' ? 'active' : ''}`} onClick={() => handleTabChange('predictions')}>
            AI Predictions
          </button>
          <button className={`tab-btn ${activeTab === 'scans' ? 'active' : ''}`} onClick={() => handleTabChange('scans')}>
            Scans & Imaging
          </button>
        </div>
      </div>

      {/* Tab Content */}
      <div className="tab-content-container">
        
        {/* OVERVIEW TAB */}
        {activeTab === 'overview' && (
          <div className="tab-pane animation-fade-in">
            <div className="dashboard-grid">
              <div className="card">
                <h3>Treatment Summary</h3>
                <p className="text-muted mt-2">Patient is currently in the consultation phase for a potential dental implant procedure.</p>
                <div className="summary-list mt-4">
                  <div className="summary-item">
                    <span className="summary-label">Target Site:</span>
                    <span className="summary-val font-medium">{clinical.implantSite || 'N/A'} (Tooth {clinical.toothNumber || 'N/A'})</span>
                  </div>
                  <div className="summary-item">
                    <span className="summary-label">Implant Type:</span>
                    <span className="summary-val">{clinical.implantType || 'N/A'} - {clinical.implantDiameter}x{clinical.implantLength}mm</span>
                  </div>
                  <div className="summary-item">
                    <span className="summary-label">Bone Quality:</span>
                    <span className="summary-val">{clinical.boneDensityClass || 'Not Assessed'}</span>
                  </div>
                </div>
              </div>
              <div className="card">
                <h3>Key Risk Factors</h3>
                <div className="risk-factors-list mt-3">
                  {clinical.conditions && clinical.conditions.includes('diabetes') && (
                    <div className="risk-alert alert-warning">
                      <ShieldAlert size={16}/> Diabetes ({clinical.diabetesStatus})
                    </div>
                  )}
                  {clinical.smokingStatus && clinical.smokingStatus !== 'Non-Smoker' && (
                    <div className="risk-alert alert-warning">
                      <ShieldAlert size={16}/> Smoker ({clinical.smokingStatus})
                    </div>
                  )}
                  {(!clinical.conditions?.includes('diabetes') && (!clinical.smokingStatus || clinical.smokingStatus === 'Non-Smoker')) && (
                    <div className="risk-alert alert-success">
                      <CheckCircle size={16}/> No major systemic risk factors identified.
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* MEDICAL HISTORY TAB */}
        {activeTab === 'history' && (
          <div className="tab-pane animation-fade-in">
            <div className="card full-width">
              <h3>Complete Clinical Assessment</h3>
              <div className="history-sections mt-4">
                
                <div className="history-block">
                  <h4><FileText size={16} className="mr-2"/> Systemic Conditions</h4>
                  <div className="tags-container mt-2">
                    {clinical.conditions?.length > 0 ? clinical.conditions.map(c => (
                      <span key={c} className="clinical-tag">{c.replace('_', ' ').toUpperCase()}</span>
                    )) : <span className="text-muted">None reported</span>}
                  </div>
                </div>

                <div className="history-block mt-4">
                  <h4><Activity size={16} className="mr-2"/> Vitals & Lifestyle</h4>
                  <div className="grid-2-col mt-2">
                    <div><strong>Smoking:</strong> {clinical.smokingStatus || 'Unknown'} {clinical.packYears ? `(${clinical.packYears} Pack Years)` : ''}</div>
                    <div><strong>Diabetes:</strong> {clinical.diabetesStatus || 'Unknown'} {clinical.hba1c ? `(HbA1c: ${clinical.hba1c}%)` : ''}</div>
                  </div>
                </div>

                <div className="history-block mt-4">
                  <h4><MapPin size={16} className="mr-2"/> Site-Specific Factors</h4>
                  <div className="tags-container mt-2">
                    {clinical.siteFactors?.length > 0 ? clinical.siteFactors.map(f => (
                      <span key={f} className="clinical-tag tag-blue">{f.replace('_', ' ').toUpperCase()}</span>
                    )) : <span className="text-muted">No site factors reported</span>}
                  </div>
                </div>

              </div>
            </div>
          </div>
        )}

        {/* PREDICTIONS TAB */}
        {activeTab === 'predictions' && (
          <div className="tab-pane animation-fade-in">
            {!isPredicting && !predictionResult ? (
              <div className="empty-state card">
                <Zap size={48} className="text-muted mb-4" />
                <h3>No Predictions Run Yet</h3>
                <p className="text-muted mb-4">Run the AI analysis pipeline to generate implant survival probability scores based on this patient's clinical data and scans.</p>
                <button className="btn btn-primary" onClick={runAIPrediction}>Initialize AI Prediction Pipeline</button>
              </div>
            ) : isPredicting ? (
              <div className="card ai-loading-container">
                <div className="ai-scanner">
                  <div className="scan-line"></div>
                </div>
                <h3>AI Neural Engine Processing</h3>
                <div className="loading-steps">
                  <p className={loadingStep >= 0 ? "step-active" : "step-pending"}>Extracting clinical parameters...</p>
                  <p className={loadingStep >= 1 ? "step-active" : "step-pending"}>Analyzing bone density and site factors...</p>
                  <p className={loadingStep >= 2 ? "step-active" : "step-pending"}>Evaluating systemic risk indicators...</p>
                  <p className={loadingStep >= 3 ? "step-active" : "step-pending"}>Calculating survival probability matrix...</p>
                </div>
              </div>
            ) : (
              <div className="dashboard-grid">
                <div className="card score-card text-center">
                  <h3 className="mb-4">Overall Survival Probability</h3>
                  
                  <div className="circular-progress-container">
                    <div className="circular-progress" style={{background: `conic-gradient(var(--${predictionResult.riskLevel === 'Low' ? 'success' : predictionResult.riskLevel === 'Medium' ? 'warning' : 'danger'}-color) ${predictionResult.score}%, #e2e8f0 0deg)`}}>
                      <div className="inner-circle">
                        <span className="score-value">{predictionResult.score}%</span>
                        <span className="score-label">5-Year Survival</span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="score-meta mt-4">
                    <div className="meta-box">
                      <span className="meta-title">Risk Level</span>
                      <span className={`badge badge-${predictionResult.riskLevel.toLowerCase()} mt-1`}>{predictionResult.riskLevel} Risk</span>
                    </div>
                    <div className="meta-box">
                      <span className="meta-title">AI Confidence</span>
                      <span className="font-medium text-primary mt-1">{predictionResult.confidence}%</span>
                    </div>
                  </div>
                  
                  <button className="btn btn-secondary mt-4 w-full">Save Prediction to Record</button>
                </div>
                
                <div className="card">
                  <h3>Clinical Factor Analysis</h3>
                  <p className="text-muted mb-4">How patient data influenced the survival probability model.</p>
                  
                  <div className="factor-list">
                    {predictionResult.factors.map((factor, index) => (
                      <div key={index} className="factor-item">
                        <div className="factor-info">
                          <span className={`factor-icon ${factor.type === 'positive' ? 'bg-success' : 'bg-danger'}`}>
                            {factor.type === 'positive' ? '+' : '-'}
                          </span>
                          <span className="font-medium">{factor.name}</span>
                        </div>
                        <span className={`factor-score text-${factor.type === 'positive' ? 'success' : 'danger'}`}>
                          {factor.impact > 0 ? '+' : ''}{factor.impact}%
                        </span>
                      </div>
                    ))}
                    {predictionResult.factors.length === 0 && (
                      <div className="text-center text-muted p-4">No significant risk factors identified. Baseline survival rate applies.</div>
                    )}
                  </div>
                  
                  <div className="clinical-recommendation mt-4">
                    <h4><Activity size={16} className="mr-2"/> AI Recommendation</h4>
                    <p className="text-sm mt-2">
                      {predictionResult.riskLevel === 'Low' ? 'Standard immediate or delayed loading protocol is suitable. High probability of osseointegration success.' :
                       predictionResult.riskLevel === 'Medium' ? 'Delayed loading protocol recommended. Monitor systemic factors closely during healing phase.' :
                       'High risk of failure. Significant bone grafting and strict management of systemic conditions required prior to implant placement.'}
                    </p>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* SCANS TAB */}
        {activeTab === 'scans' && (
          <div className="tab-pane animation-fade-in">
            <div className="empty-state card">
              <Settings size={48} className="text-muted mb-4" />
              <h3>Imaging & Scans</h3>
              <p className="text-muted mb-4">No CBCT or panoramic X-rays have been uploaded for this patient yet.</p>
              <button className="btn btn-secondary">Upload Scans</button>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default PatientProfile;
