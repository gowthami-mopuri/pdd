import React, { useState, useEffect } from 'react';
import { Outlet, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Users, Activity, FileText, Settings, Bell, Search, Hexagon, Zap, ShieldCheck } from 'lucide-react';
import './Layout.css';

const Layout = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem('doctorUser');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const getPageTitle = () => {
    const path = location.pathname;
    if (path.includes('dashboard')) return 'Dashboard Analytics';
    if (path.includes('patients')) return 'Patient Management';
    if (path.includes('predict')) return 'AI Prediction Workflow';
    if (path.includes('reports')) return 'Medical Reports';
    if (path.includes('profile')) return 'My Profile';
    if (path.includes('admin')) return 'Admin Dashboard';
    return 'Implant Survival AI';
  };

  return (
    <div className="layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="logo-container">
          <Hexagon size={32} color="var(--primary-color)" />
          <div className="logo-text">
            ImplantAI<br />
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 500 }}>Predictive Analytics</span>
          </div>
        </div>

        <nav className="nav-links">
          <NavLink to="/dashboard" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <LayoutDashboard size={20} />
            <span>Dashboard</span>
          </NavLink>
          <NavLink to="/patients" className={({ isActive }) => `nav-item ${isActive || location.pathname.includes('/patients') ? 'active' : ''}`}>
            <Users size={20} />
            <span>Patients</span>
          </NavLink>
          <NavLink to="/ai-analysis" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <Zap size={20} />
            <span>AI Analysis</span>
          </NavLink>
          <NavLink to="/reports" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <FileText size={20} />
            <span>Reports</span>
          </NavLink>
          <div style={{ flex: 1 }}></div>
          <NavLink to="/" end className="nav-item" style={{ marginTop: 'auto', color: 'var(--danger-color)' }} onClick={() => localStorage.removeItem('doctorUser')}>
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
            <span>Switch Role</span>
          </NavLink>
        </nav>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        {/* Header */}
        <header className="header">
          <div className="header-title">{getPageTitle()}</div>
          
          <div className="header-actions">
            <button className="icon-btn" aria-label="Settings" onClick={() => navigate('/settings')}>
              <Settings size={20} />
            </button>
            
            <div 
              className="user-profile" 
              style={{ cursor: 'pointer', padding: '0.5rem', borderRadius: '8px', transition: 'background 0.2s' }}
              onClick={() => navigate('/profile')}
              onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f1f5f9'}
              onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
              title="View My Profile"
            >
              <div className="avatar">Dr</div>
              <div className="user-info">
                <span className="user-name">Dr. {user ? user.username : 'Jenkins'}</span>
                <span className="user-role">Clinical Staff</span>
              </div>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <div className="page-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default Layout;
