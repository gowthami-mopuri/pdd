import React, { useState, useEffect } from 'react';
import { supabase } from '../lib/supabaseClient';
import './MyProfile.css';

const MyProfile = () => {
  const [user, setUser] = useState(null);
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState({ type: '', text: '' });
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const storedUser = localStorage.getItem('doctorUser');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setMessage({ type: '', text: '' });

    if (newPassword !== confirmPassword) {
      setMessage({ type: 'error', text: 'Passwords do not match.' });
      setIsLoading(false);
      return;
    }

    try {
      const { error } = await supabase
        .from('staff_accounts')
        .update({ 
          password: newPassword,
          password_changed: true 
        })
        .eq('id', user.id);

      if (error) {
        if (error.message.includes('column') && error.message.includes('does not exist')) {
          throw new Error('Database column "password_changed" does not exist. Please run the SQL command in Supabase.');
        }
        throw error;
      }

      // Update local storage so UI updates
      const updatedUser = { ...user, password_changed: true };
      setUser(updatedUser);
      localStorage.setItem('doctorUser', JSON.stringify(updatedUser));
      
      setMessage({ type: 'success', text: 'Password successfully updated!' });
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      console.error('Error changing password:', err);
      setMessage({ type: 'error', text: err.message });
    } finally {
      setIsLoading(false);
    }
  };

  if (!user) {
    return (
      <div className="profile-page animation-fade-in">
        <div className="no-profile">
          <h2>Profile Not Found</h2>
          <p>Please log in through the role selection screen to view your profile.</p>
        </div>
      </div>
    );
  }

  // Generate initials
  const initials = user.username ? user.username.substring(0, 2).toUpperCase() : 'DR';

  return (
    <div className="profile-page animation-fade-in">
      <div className="profile-card">
        <div className="profile-header">
          <div className="profile-avatar">
            {initials}
          </div>
          <div className="profile-title">
            <h1>Dr. {user.username}</h1>
            <p>Clinical Staff Member</p>
          </div>
        </div>

        <div className="profile-details">
          <div className="detail-group">
            <span className="detail-label">Email Address</span>
            <span className="detail-value">{user.email || 'Not provided'}</span>
          </div>
          <div className="detail-group">
            <span className="detail-label">Phone Number</span>
            <span className="detail-value">{user.phone || 'Not provided'}</span>
          </div>
          <div className="detail-group">
            <span className="detail-label">Gender</span>
            <span className="detail-value">{user.gender || 'Not specified'}</span>
          </div>
          <div className="detail-group">
            <span className="detail-label">Date of Birth</span>
            <span className="detail-value">{user.dob ? new Date(user.dob).toLocaleDateString() : 'Not provided'}</span>
          </div>
          <div className="detail-group full-width">
            <span className="detail-label">Residential/Clinic Address</span>
            <span className="detail-value">{user.address || 'Not provided'}</span>
          </div>
          <div className="detail-group">
            <span className="detail-label">Account Created</span>
            <span className="detail-value">{new Date(user.created_at).toLocaleDateString()}</span>
          </div>
          <div className="detail-group">
            <span className="detail-label">Account Status</span>
            <span className="detail-value" style={{ color: user.status === 'suspended' ? '#ef4444' : '#10b981', fontWeight: '700', textTransform: 'capitalize' }}>
              {user.status || 'Active'}
            </span>
          </div>
        </div>
      </div>

      <div className="profile-card" style={{ marginTop: '2rem' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: '700', marginBottom: '1rem', color: '#0f172a' }}>Security Settings</h2>
        
        {user.password_changed ? (
          <div style={{ padding: '1rem', backgroundColor: '#f1f5f9', borderRadius: '8px', color: '#475569', fontSize: '0.875rem' }}>
            <strong>Password Secured.</strong> You have already set your custom password. If you have forgotten it or need to reset it again, please contact your Administrator.
          </div>
        ) : (
          <form onSubmit={handlePasswordChange} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <p style={{ color: '#64748b', fontSize: '0.875rem' }}>
              Please update your default password. You may only change this once.
            </p>
            
            {message.text && (
              <div style={{ padding: '0.75rem', borderRadius: '8px', fontSize: '0.875rem', backgroundColor: message.type === 'error' ? '#fee2e2' : '#dcfce7', color: message.type === 'error' ? '#991b1b' : '#166534' }}>
                {message.text}
              </div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.875rem', fontWeight: '600', color: '#475569' }}>New Password</label>
              <input 
                type="password" 
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
                style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid #cbd5e1' }}
              />
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.875rem', fontWeight: '600', color: '#475569' }}>Confirm New Password</label>
              <input 
                type="password" 
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                style={{ padding: '0.75rem', borderRadius: '8px', border: '1px solid #cbd5e1' }}
              />
            </div>

            <button 
              type="submit" 
              disabled={isLoading}
              style={{ padding: '0.875rem', background: '#0f172a', color: 'white', borderRadius: '8px', fontWeight: '600', cursor: isLoading ? 'not-allowed' : 'pointer' }}
            >
              {isLoading ? 'Updating...' : 'Set Custom Password'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default MyProfile;
