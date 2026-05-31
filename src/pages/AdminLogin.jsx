import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck, AlertCircle, ArrowLeft } from 'lucide-react';
import './AdminLogin.css';

const AdminLogin = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    // Hardcoded credentials check
    setTimeout(() => {
      if (username === 'admin' && password === 'admin123') {
        localStorage.setItem('adminAuthenticated', 'true');
        navigate('/admin');
      } else {
        setError('Invalid admin credentials. Access denied.');
      }
      setIsLoading(false);
    }, 600); // Small delay to feel like a real authentication request
  };

  return (
    <div className="admin-login-page animation-fade-in">
      <div className="admin-login-container">
        
        <button 
          onClick={() => navigate('/')} 
          className="flex items-center gap-2 text-slate-500 hover:text-slate-800 transition-colors mb-6 font-medium"
        >
          <ArrowLeft size={18} /> Back to Selection
        </button>

        <div className="admin-login-icon">
          <ShieldCheck size={36} color="#0f172a" />
        </div>
        
        <h1>Admin Secure Login</h1>
        <p>Please enter your master credentials.</p>

        {error && (
          <div className="admin-error-message animation-slide-up">
            <AlertCircle size={18} />
            {error}
          </div>
        )}

        <form className="admin-login-form" onSubmit={handleLogin}>
          <div className="admin-input-group">
            <label>Master Username</label>
            <input 
              type="text" 
              placeholder="Enter admin username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="off"
            />
          </div>
          
          <div className="admin-input-group">
            <label>Master Password</label>
            <input 
              type="password" 
              placeholder="Enter admin password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn-admin-login" disabled={isLoading}>
            {isLoading ? 'Verifying...' : 'Access Dashboard'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default AdminLogin;
