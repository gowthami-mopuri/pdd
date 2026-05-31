import React, { useState } from 'react';
import { ArrowRight, ArrowLeft, Check, Activity } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../lib/supabaseClient';
import './AddPatient.css';

const STEPS = [
  'Patient Details',
  'Medical Conditions',
  'Smoking & Diabetes',
  'Site Factors'
];

const AddPatient = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(0);
  
  const [formData, setFormData] = useState({
    patientId: '', patientName: '', age: '', gender: '', height: '', weight: '',
    conditions: [],
    implantSite: '', implantPosition: '', toothNumber: '', implantType: '', implantDiameter: '', implantLength: '', loadingProtocol: '',
    boneDensityClass: '', boneDensityScore: '',
    smokingStatus: '', packYears: '', diabetesStatus: '', hba1c: '', fastingSugar: '',
    siteFactors: []
  });

  const handleNext = () => {
    if (currentStep < STEPS.length - 1) setCurrentStep(prev => prev + 1);
  };

  const handlePrev = () => {
    if (currentStep > 0) setCurrentStep(prev => prev - 1);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const toggleArrayItem = (field, item) => {
    setFormData(prev => {
      const array = prev[field];
      if (array.includes(item)) {
        return { ...prev, [field]: array.filter(i => i !== item) };
      } else {
        return { ...prev, [field]: [...array, item] };
      }
    });
  };

  const [isSaving, setIsSaving] = useState(false);

  const handleFinish = async () => {
    if (!formData.patientId) {
      alert('Patient ID is required.');
      setCurrentStep(0);
      return;
    }
    
    setIsSaving(true);
    try {
      const { data, error } = await supabase
        .from('patients')
        .insert([
          {
            patient_id: formData.patientId,
            name: formData.patientName || 'Unknown Patient',
            age: formData.age ? parseInt(formData.age) : null,
            gender: formData.gender,
            height: formData.height ? parseInt(formData.height) : null,
            weight: formData.weight ? parseInt(formData.weight) : null,
            status: 'Consultation',
            risk: 'Pending',
            clinical_data: formData
          }
        ]);

      if (error) throw error;
      
      alert('Patient Profile Created successfully!');
      navigate('/patients');
    } catch (error) {
      console.error('Error saving patient:', error);
      alert('Failed to save patient. Please check the console for details.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="add-patient-workflow">
      <div className="wizard-container card">
        
        {/* Progress Bar */}
        <div className="wizard-progress">
          {STEPS.map((step, index) => (
            <div key={index} className={`progress-step ${index === currentStep ? 'active' : ''} ${index < currentStep ? 'completed' : ''}`}>
              <div className="step-circle">
                {index < currentStep ? <Check size={14} /> : index + 1}
              </div>
              <span className="step-label">{step}</span>
            </div>
          ))}
        </div>

        <div className="wizard-content">
          {/* Step 1: Patient Details */}
          {currentStep === 0 && (
            <div className="step-pane animation-fade-in">
              <h2 className="step-title">Patient Details</h2>
              <p className="text-muted mb-4">Enter basic demographic and physical information.</p>
              
              <div className="form-grid">
                <div className="form-group">
                  <label>Patient ID <span className="text-danger">*</span></label>
                  <input type="text" className="input-field" name="patientId" value={formData.patientId} onChange={handleChange} placeholder="e.g., PT-2024-001" required />
                </div>
                <div className="form-group">
                  <label>Patient Name</label>
                  <input type="text" className="input-field" name="patientName" value={formData.patientName} onChange={handleChange} placeholder="e.g., John Doe" />
                </div>
                <div className="form-group">
                  <label>Age</label>
                  <input type="number" className="input-field" name="age" value={formData.age} onChange={handleChange} placeholder="e.g., 45" />
                </div>
                <div className="form-group">
                  <label>Gender</label>
                  <select className="input-field" name="gender" value={formData.gender} onChange={handleChange}>
                    <option value="">Select gender</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Height (cm)</label>
                  <input type="number" className="input-field" name="height" value={formData.height} onChange={handleChange} placeholder="e.g., 175" />
                </div>
                <div className="form-group">
                  <label>Weight (kg)</label>
                  <input type="number" className="input-field" name="weight" value={formData.weight} onChange={handleChange} placeholder="e.g., 70" />
                </div>
              </div>
            </div>
          )}

          {/* Step 2: Medical Conditions */}
          {currentStep === 1 && (
            <div className="step-pane animation-fade-in">
              <h2 className="step-title">Medical Conditions</h2>
              <p className="text-muted mb-4">Select all that apply</p>
              
              <div className="checkbox-grid">
                {[
                  { id: 'diabetes', title: 'Diabetes', desc: 'Type 1 or Type 2' },
                  { id: 'hypertension', title: 'Hypertension', desc: 'High blood pressure' },
                  { id: 'osteoporosis', title: 'Osteoporosis', desc: 'Bone density issues' },
                  { id: 'heart_disease', title: 'Heart Disease', desc: 'Cardiovascular conditions' },
                  { id: 'autoimmune', title: 'Autoimmune Disorders', desc: 'Immune system conditions' },
                  { id: 'blood_disorders', title: 'Blood Disorders', desc: 'Clotting or bleeding issues' },
                  { id: 'medications', title: 'Medications', desc: 'Currently taking medications' },
                  { id: 'allergies', title: 'Allergies', desc: 'Drug or material allergies' }
                ].map(condition => (
                  <div key={condition.id} className={`selectable-card ${formData.conditions.includes(condition.id) ? 'selected' : ''}`} onClick={() => toggleArrayItem('conditions', condition.id)}>
                    <div className="selectable-header">
                      <span className="selectable-title">{condition.title}</span>
                      <div className={`checkbox-circle ${formData.conditions.includes(condition.id) ? 'checked' : ''}`}>
                        {formData.conditions.includes(condition.id) && <Check size={12} />}
                      </div>
                    </div>
                    <span className="selectable-desc">{condition.desc}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Step 3: Smoking & Diabetes Assessment */}
          {currentStep === 2 && (
            <div className="step-pane animation-fade-in">
              <h2 className="step-title">Smoking & Diabetes Assessment</h2>
              
              <div className="split-sections">
                <div className="section">
                  <h3 className="section-title">Smoking Status <span className="warning-text">▲ Smoking increases implant failure risk</span></h3>
                  <select className="input-field mb-3" name="smokingStatus" value={formData.smokingStatus} onChange={handleChange}>
                    <option value="">Select status</option>
                    <option value="Non-Smoker">Non-Smoker (Never smoked or quit &gt; 1 year)</option>
                    <option value="Former Smoker">Former Smoker (Quit within the last year)</option>
                    <option value="Light Smoker">Light Smoker (&lt; 10 cigarettes per day)</option>
                    <option value="Moderate Smoker">Moderate Smoker (10-20 cigarettes per day)</option>
                    <option value="Heavy Smoker">Heavy Smoker (&gt; 20 cigarettes per day)</option>
                  </select>
                  
                  {['Former Smoker', 'Light Smoker', 'Moderate Smoker', 'Heavy Smoker'].includes(formData.smokingStatus) && (
                    <div className="form-group animation-fade-in">
                      <label>Pack Years (Calculate: packs per day x years)</label>
                      <input type="number" className="input-field" name="packYears" value={formData.packYears} onChange={handleChange} />
                    </div>
                  )}
                </div>

                <div className="section">
                  <h3 className="section-title">Diabetes Assessment</h3>
                  <p className="text-muted" style={{fontSize: '0.875rem', marginBottom: '0.5rem'}}>Blood sugar control impacts healing</p>
                  <select className="input-field mb-3" name="diabetesStatus" value={formData.diabetesStatus} onChange={handleChange}>
                    <option value="">Select status</option>
                    <option value="No Diabetes">No Diabetes (Normal glucose levels)</option>
                    <option value="Pre-Diabetes">Pre-Diabetes (Elevated blood sugar levels)</option>
                    <option value="Type 1 Diabetes">Type 1 Diabetes (Insulin-dependent)</option>
                    <option value="Type 2 (Controlled)">Type 2 Diabetes (Controlled - HbA1c &lt; 7.0%)</option>
                    <option value="Type 2 (Uncontrolled)">Type 2 Diabetes (Uncontrolled - HbA1c &gt; 7.0%)</option>
                  </select>

                  {formData.diabetesStatus && formData.diabetesStatus !== 'No Diabetes' && (
                    <div className="form-grid animation-fade-in">
                      <div className="form-group">
                        <label>HbA1c Level (%)</label>
                        <input type="number" step="0.1" className="input-field" name="hba1c" value={formData.hba1c} onChange={handleChange} placeholder="Enter HbA1c percentage" />
                      </div>
                      <div className="form-group">
                        <label>Fasting Blood Sugar (mg/dL)</label>
                        <input type="number" className="input-field" name="fastingSugar" value={formData.fastingSugar} onChange={handleChange} placeholder="Enter fasting glucose level" />
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Step 4: Site-Specific Factors */}
          {currentStep === 3 && (
            <div className="step-pane animation-fade-in">
              <h2 className="step-title">Site-Specific Factors</h2>
              <p className="text-muted mb-4">Select all applicable conditions</p>
              
              <div className="checkbox-grid">
                {[
                  { id: 'bone_volume', title: 'Adequate Bone Volume', desc: 'Sufficient bone width and height' },
                  { id: 'bone_grafting', title: 'Bone Grafting Required', desc: 'Augmentation procedure needed' },
                  { id: 'sinus_lift', title: 'Sinus Lift Required', desc: 'Maxillary sinus augmentation' },
                  { id: 'adjacent_tooth_loss', title: 'Adjacent Tooth Loss', desc: 'Missing neighboring teeth' },
                  { id: 'previous_failure', title: 'Previous Implant Failure', desc: 'Failed implant at same site' },
                  { id: 'infection', title: 'Infection Present', desc: 'Active infection at site' },
                  { id: 'poor_hygiene', title: 'Poor Oral Hygiene', desc: 'Plaque index > 20%' },
                  { id: 'parafunctional', title: 'Parafunctional Habits', desc: 'Bruxism or clenching' }
                ].map(factor => (
                  <div 
                    key={factor.id}
                    className={`selectable-card ${formData.siteFactors.includes(factor.id) ? 'selected' : ''}`}
                    onClick={() => toggleArrayItem('siteFactors', factor.id)}
                  >
                    <div className="selectable-header">
                      <span className="selectable-title">{factor.title}</span>
                      <div className={`checkbox-circle ${formData.siteFactors.includes(factor.id) ? 'checked' : ''}`}>
                        {formData.siteFactors.includes(factor.id) && <Check size={12} />}
                      </div>
                    </div>
                    <span className="selectable-desc">{factor.desc}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Footer Actions */}
        <div className="wizard-footer">
          <button 
            className="btn btn-secondary" 
            onClick={handlePrev} 
            disabled={currentStep === 0}
            style={{ visibility: currentStep === 0 ? 'hidden' : 'visible' }}
          >
            <ArrowLeft size={16} className="mr-2" /> Back
          </button>
          
          {currentStep < STEPS.length - 1 ? (
            <button className="btn btn-primary" onClick={handleNext}>
              Next <ArrowRight size={16} className="ml-2" />
            </button>
          ) : (
            <button className="btn btn-primary bg-green" onClick={handleFinish} disabled={isSaving}>
              {isSaving ? 'Saving...' : 'Save Patient'} <Check size={16} className="ml-2" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default AddPatient;
