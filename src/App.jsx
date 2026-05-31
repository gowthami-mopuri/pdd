import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/layout/Layout';
import Dashboard from './pages/Dashboard';
import Patients from './pages/Patients';
import AddPatient from './pages/AddPatient';
import PatientProfile from './pages/PatientProfile';
import Reports from './pages/Reports';
import AIAnalysis from './pages/AIAnalysis';
import ImplantSurvival from './pages/ImplantSurvival';
import AdminDashboard from './pages/AdminDashboard';
import LandingPage from './pages/LandingPage';
import DoctorLogin from './pages/DoctorLogin';
import AdminLogin from './pages/AdminLogin';
import MyProfile from './pages/MyProfile';
import SettingsPage from './pages/SettingsPage';
import PatientLogin from './pages/PatientLogin';
import PatientDashboard from './pages/PatientDashboard';

function App() {
  return (
    <Router>
      <Routes>
        {/* Landing Page without Layout/Sidebar */}
        <Route path="/" element={<LandingPage />} />
        
        {/* Logins */}
        <Route path="/login" element={<DoctorLogin />} />
        <Route path="/admin-login" element={<AdminLogin />} />
        <Route path="/patient-login" element={<PatientLogin />} />
        
        {/* Dashboards without Clinical Sidebar */}
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/patient-dashboard" element={<PatientDashboard />} />
        
        {/* App Pages with Layout/Sidebar */}
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/patients" element={<Patients />} />
          <Route path="/patients/add" element={<AddPatient />} />
          <Route path="/patients/:id" element={<PatientProfile />} />
          <Route path="/ai-analysis" element={<AIAnalysis />} />
          <Route path="/reports" element={<Reports />} />
          <Route path="/implant-survival" element={<ImplantSurvival />} />
          <Route path="/profile" element={<MyProfile />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
