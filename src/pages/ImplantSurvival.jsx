import React, { useState, useEffect, useRef } from 'react';
import { 
  Activity, Shield, AlertTriangle, CheckCircle, TrendingUp, 
  FileText, Download, Share2, FileSignature, BrainCircuit,
  Stethoscope, Droplets, Bone, Cigarette, Check, User
} from 'lucide-react';
import { 
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer 
} from 'recharts';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import './ImplantSurvival.css';

// Screen 4: Graph Data
const survivalData = [
  { time: '1M', patient: 99, average: 95 },
  { time: '3M', patient: 95, average: 88 },
  { time: '6M', patient: 91, average: 82 },
  { time: '1Y', patient: 88, average: 79 },
  { time: '2Y', patient: 86, average: 77 },
  { time: '5Y', patient: 84, average: 76 },
];



const ImplantSurvival = ({ uploadedImage, imageFile, patientData, onBack }) => {
  const [isProcessing, setIsProcessing] = useState(true);
  const [processingStep, setProcessingStep] = useState(0);
  const [aiData, setAiData] = useState(null);
  const [error, setError] = useState(null);

  const steps = [
    'Uploaded image received...',
    'Connecting to Google Gemini 2.5 Vision...',
    'Analyzing medical & anatomical factors...',
    'Running survival probability predictions...',
    'Generating clinical recommendations...'
  ];

  useEffect(() => {
    // Step progression animation
    const interval = setInterval(() => {
      setProcessingStep(prev => {
        if (prev >= steps.length - 1) return prev;
        return prev + 1;
      });
    }, 1200);

    // Call the backend
    const fetchPrediction = async () => {
      try {
        const formData = new FormData();
        formData.append('file', imageFile);
        formData.append('patient_data', JSON.stringify(patientData));

        const response = await fetch('http://localhost:8000/analyze/gemini-survival', {
          method: 'POST',
          body: formData,
        });

        if (!response.ok) {
          throw new Error(`Backend error: ${response.status}`);
        }

        const json = await response.json();
        
        if (json.status === 'success') {
          setAiData(json.data);
        } else {
          throw new Error(json.message || 'Unknown error from Gemini');
        }
      } catch (err) {
        console.error("Gemini AI Error:", err);
        setError(err.message);
      } finally {
        clearInterval(interval);
        setProcessingStep(4);
        setTimeout(() => setIsProcessing(false), 1000);
      }
    };

    fetchPrediction();
    return () => clearInterval(interval);
  }, [imageFile, patientData]);

  if (isProcessing) {
    return (
      <div className="processing-container">
        <div className="processing-box glass-panel text-center">
          {uploadedImage && (
            <img src={uploadedImage} alt="Analyzing" className="processing-image" />
          )}
          <BrainCircuit className="text-primary pulse-anim mb-15 mx-auto" size={48} />
          <h2 className="mb-20 text-center">Gemini AI Analysis</h2>
          <div className="text-muted text-center mb-20">{patientData.name}</div>
          {error ? (
             <div className="text-danger mb-10"><AlertTriangle size={16} className="inline mr-5"/> {error}</div>
          ) : (
             <div className="steps-list">
               {steps.map((step, idx) => (
                 <div key={idx} className={`step-item ${idx <= processingStep ? 'active' : ''} ${idx < processingStep ? 'completed' : ''}`}>
                   <div className="step-icon">
                     {idx < processingStep ? <Check size={14} /> : <div className="dot"></div>}
                   </div>
                   <span>{step}</span>
                 </div>
               ))}
             </div>
          )}
        </div>
      </div>
    );
  }

  // Fallback to static data if Gemini fails so the UI doesn't crash completely during demo
  const data = aiData || {
    survival_probability: 84,
    failure_risk: 16,
    confidence: 91,
    risk_factors: [
        {"label": "Bone Density", "risk": "20%", "level": "LOW", "color": "success"},
        {"label": "Smoking History", "risk": "45%", "level": "MEDIUM", "color": "warning"},
        {"label": "Diabetes", "risk": "15%", "level": "LOW", "color": "success"},
        {"label": "Overall Risk", "risk": "22%", "level": "LOW", "color": "success"}
    ],
    success_factors: [
        {"factor": "Bone Density Quality", "impact": "+18%", "pos": true},
        {"factor": "Smoking Habit", "impact": "-8%", "pos": false}
    ],
    action_items: [
        {"text": "Smoking Cessation", "level": "MEDIUM", "type": "warning"}
    ],
    narrative: [
        "Survival probability is estimated based on visible bone quality and medical history.",
        "Recommend strict follow-up."
    ]
  };

  const isHighSuccess = data.survival_probability > 75;

  const downloadPDF = async () => {
    const element = document.getElementById('report-content');
    if (!element) return;
    
    try {
      const canvas = await html2canvas(element, { scale: 1.5, useCORS: true });
      const imgData = canvas.toDataURL('image/jpeg', 0.9);
      const pdf = new jsPDF('p', 'mm', 'a4');
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = (canvas.height * pdfWidth) / canvas.width;
      
      pdf.addImage(imgData, 'JPEG', 0, 0, pdfWidth, pdfHeight);
      pdf.save(`AI_Implant_Report_${patientData.name.replace(/\s+/g, '_')}.pdf`);
    } catch (err) {
      console.error("Error generating PDF", err);
    }
  };

  const handleSaveReport = () => {
    try {
      const existingReports = JSON.parse(localStorage.getItem('savedReports') || '[]');
      const newReport = {
        id: Date.now().toString(),
        date: new Date().toISOString(),
        patientName: patientData.name,
        patientId: patientData.patient_id,
        survivalProbability: data.survival_probability,
        riskLevel: isHighSuccess ? 'LOW' : 'MEDIUM',
        failureRisk: data.failure_risk,
        actionItems: data.action_items.map(a => a.text)
      };
      
      localStorage.setItem('savedReports', JSON.stringify([newReport, ...existingReports]));
      alert('Report saved successfully! You can view it in the Reports tab.');
    } catch (err) {
      console.error("Error saving report", err);
      alert('Failed to save report.');
    }
  };

  return (
    <div id="report-content" className="survival-dashboard animation-fade-in">
      {error && (
        <div className="bg-[var(--danger-color)] bg-opacity-20 text-[var(--danger-color)] p-4 rounded-lg mb-20 border border-[var(--danger-color)]">
          <AlertTriangle size={20} className="inline mr-10"/>
          <strong>Gemini Error:</strong> {error}. Using fallback placeholder data. Ensure GEMINI_API_KEY is configured on the backend.
        </div>
      )}

      <div className="welcome-banner glass-panel mb-20 flex-row-between items-center" data-html2canvas-ignore="true">
        <div>
          <h2>Implant Survival Prediction</h2>
          <p className="text-muted">Dynamic Gemini AI risk analysis based on uploaded scan for <strong>{patientData.name}</strong>.</p>
        </div>
        <div className="flex-row gap-10">
          {onBack && <button className="btn btn-secondary" onClick={onBack}>← Back</button>}
          <button className="btn btn-secondary" onClick={handleSaveReport}><FileText size={16}/> Save to Reports</button>
          <button className="btn btn-primary" onClick={downloadPDF}><Download size={16}/> Download PDF</button>
        </div>
      </div>

      {/* Patient Information Banner */}
      <div className="data-box mb-20">
        <h3 className="icon-title mb-10"><User className="text-primary" size={20}/> Patient Profile</h3>
        <div className="grid-3-col">
          <div>
            <div className="text-small text-muted mb-5">Full Name</div>
            <div className="font-bold" style={{ color: '#0f172a' }}>{patientData.name}</div>
          </div>
          <div>
            <div className="text-small text-muted mb-5">Patient ID / Demographics</div>
            <div className="font-bold" style={{ color: '#0f172a' }}>
              {patientData.patient_id} • Age: {patientData.age || 'N/A'} {patientData.gender ? `• ${patientData.gender}` : ''}
            </div>
          </div>
          <div>
            <div className="text-small text-muted mb-5">Medical History</div>
            <div className="font-bold" style={{ color: '#0f172a' }}>{patientData.medical_history || 'None reported'}</div>
          </div>
        </div>
      </div>

      <div className="grid-3-col mb-20">
        
        {/* Screen 2: Prediction Result */}
        <div className="card result-hero flex-col-center text-center">
          {uploadedImage && (
             <img src={uploadedImage} alt="Patient Scan" className="hero-scan-image" />
          )}
          <h3 className="text-muted mb-15">Survival Probability</h3>
          <div className={`score-circle ${isHighSuccess ? 'good' : ''} mb-15`} style={!isHighSuccess ? {color: 'var(--warning-color)', textShadow: '0 0 15px rgba(234, 179, 8, 0.5)'} : {}}>
            <span className="score-value">{data.survival_probability}%</span>
          </div>
          <div className="flex-row justify-center mb-15">
            <div className={`badge ${isHighSuccess ? 'badge-success' : 'badge-warning'} px-medium py-small text-medium`}>
              <CheckCircle size={16} className="inline mr-5"/> {isHighSuccess ? 'LOW RISK' : 'MEDIUM RISK'}
            </div>
          </div>
          <div className="grid-2-col w-full text-left mt-10">
            <div className="data-box">
              <div className="text-small text-muted">Failure Risk</div>
              <div className="text-large font-bold text-danger">{data.failure_risk}%</div>
            </div>
            <div className="data-box">
              <div className="text-small text-muted">AI Confidence</div>
              <div className="text-large font-bold text-primary">{data.confidence}%</div>
            </div>
          </div>
        </div>

        {/* Screen 3: Risk Analysis */}
        <div className="card col-span-2">
          <h3 className="icon-title mb-20"><AlertTriangle className="text-warning" size={20}/> AI Risk Analysis Breakdown</h3>
          <div className="risk-grid">
            {data.risk_factors.map((item, idx) => (
              <div key={idx} className="risk-item">
                <div className="flex-row-between mb-5">
                  <span className="icon-title text-small text-muted"><Activity size={16}/> {item.label}</span>
                  <span className={`text-small font-bold text-${item.color}`}>{item.level} ({item.risk})</span>
                </div>
                <div className="progress-bg">
                  <div className={`progress-fill bg-${item.color}`} style={{ width: item.risk }}></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="grid-2-col mb-20">
        
        {/* Screen 4: Analytics Graph */}
        <div className="card h-full min-h-300">
          <h3 className="icon-title mb-15"><TrendingUp className="text-primary" size={20}/> Survival Trajectory (5 Years)</h3>
          <div className="chart-container">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={survivalData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#333" vertical={false} />
                <XAxis dataKey="time" stroke="#888" />
                <YAxis domain={[0, 100]} stroke="#888" />
                <Tooltip contentStyle={{ backgroundColor: '#1e1e1e', borderColor: '#333' }} />
                <Line type="monotone" name="Patient Score" dataKey="patient" stroke="#4ade80" strokeWidth={3} dot={{r: 4}} />
                <Line type="monotone" name="Population Avg" dataKey="average" stroke="#6b7280" strokeWidth={2} strokeDasharray="5 5" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Screen 5: Success Probability Factors */}
        <div className="card h-full">
          <h3 className="icon-title mb-15"><Activity className="text-primary" size={20}/> Success Probability Factors</h3>
          <div className="flex-row-between data-box mb-20 items-center">
            <div className="text-center">
              <div className="text-small text-muted">Base Success</div>
              <div className="text-xl font-bold text-success">{data.survival_probability}%</div>
            </div>
            <div className="text-muted font-bold">vs</div>
            <div className="text-center">
              <div className="text-small text-muted">Failure</div>
              <div className="text-xl font-bold text-danger">{data.failure_risk}%</div>
            </div>
          </div>
          <div className="factor-list">
            {data.success_factors.map((f, i) => (
              <div key={i} className="factor-row">
                <span>{f.factor}</span>
                <span className={`font-bold ${f.pos ? 'text-success' : 'text-danger'}`}>{f.impact}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="mb-20">
        
        {/* Screen 6 & 7: Recommendations & AI Suggestions */}
        <div className="card w-full">
          <h3 className="icon-title mb-15"><Stethoscope className="text-primary" size={20}/> Clinical Recommendations & Gemini Narrative</h3>
          <div className="grid-2-col gap-20">
            <div>
              <h4 className="section-label">Action Items</h4>
              <ul className="action-list">
                {data.action_items.map((action, i) => (
                    <li key={i} className={action.type}>{action.text} <span>{action.level}</span></li>
                ))}
              </ul>
            </div>
            <div>
              <h4 className="section-label">AI Narrative</h4>
              <div className="narrative-box h-full">
                {data.narrative.map((text, i) => (
                    <p key={i}>{i===0 ? <strong>{text}</strong> : text}</p>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Screen 10: PDF Report Preview */}
      <div className="card">
        <h3 className="icon-title mb-15"><FileSignature className="text-primary" size={20}/> Auto-Generated PDF Report Preview</h3>
        <div className="pdf-mockup">
          <div className="pdf-header">
             <div>
               <h2 className="pdf-title">Global Dental Clinics</h2>
               <div className="pdf-subtitle">Implant Survival Analysis Report for {patientData.name}</div>
             </div>
             <div className="pdf-header-right">
               <div className="pdf-score">{data.survival_probability}% Success</div>
               <div className="pdf-risk">Risk Level: {isHighSuccess ? 'LOW' : 'MEDIUM'}</div>
             </div>
          </div>
          
          <div className="pdf-body grid-2-col gap-20">
             <div>
               <h4 className="pdf-section-title">Risk Factors</h4>
               <ul className="pdf-list">
                 {data.risk_factors.slice(0,4).map((r, i) => (
                     <li key={i}>{r.label}: {r.level} Risk</li>
                 ))}
               </ul>
             </div>
             <div>
               <h4 className="pdf-section-title">Recommendations</h4>
               <ul className="pdf-list">
                 {data.action_items.map((a, i) => (
                     <li key={i}>{a.text}</li>
                 ))}
               </ul>
             </div>
          </div>

          <div className="pdf-footer">
             <div className="pdf-footer-left">Generated by Dental AI & Google Gemini</div>
             <div className="pdf-signature">
                <span className="pdf-sig-name">Dr. Signature</span>
                <span className="pdf-sig-role">Attending Physician</span>
             </div>
          </div>
        </div>
      </div>

    </div>
  );
};

export default ImplantSurvival;
