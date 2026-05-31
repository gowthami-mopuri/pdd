import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../lib/supabaseClient';
import { Stethoscope, ArrowLeft, AlertCircle } from 'lucide-react';
import './DoctorLogin.css';

const DoctorLogin = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    if (!username || !password) {
      setError('Please enter both username and password.');
      setIsLoading(false);
      return;
    }

    try {
      // Query the staff_accounts table for a matching username and password
      const { data, error: fetchError } = await supabase
        .from('staff_accounts')
        .select('*')
        .eq('username', username)
        .eq('password', password)
        .single();

      if (fetchError || !data) {
        setError('Invalid username or password.');
      } else if (data.status === 'suspended') {
        setError('This account has been suspended by the administrator.');
      } else {
        // Save to localStorage
        localStorage.setItem('doctorUser', JSON.stringify(data));
        // Success! Route to the dashboard
        navigate('/dashboard');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError('An error occurred during login. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-page animation-fade-in">
      <div className="login-card">
        <div className="login-header">
          <Stethoscope size={48} color="var(--primary-color)" />
          <h1>Doctor Portal</h1>
          <p>Please enter your credentials to access the clinical workspace.</p>
        </div>

        {error && (
          <div className="login-error">
            <AlertCircle size={20} />
            {error}
          </div>
        )}

        <form className="login-form" onSubmit={handleLogin}>
          <div className="form-group">
            <label>Username</label>
            <input
              type="text"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>
          <button type="submit" className="login-btn" disabled={isLoading}>
            {isLoading ? 'Authenticating...' : 'Secure Login'}
          </button>
        </form>

        <button className="back-btn" onClick={() => navigate('/')}>
          <ArrowLeft size={16} /> Return to Role Selection
        </button>
      </div>
    </div>
  );
};

export default DoctorLogin;
