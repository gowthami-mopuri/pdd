import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase } from '../lib/supabaseClient';
import { ShieldCheck, UserPlus, Users, AlertCircle, CheckCircle2, ArrowLeft, Edit2, Pause, Trash2, Play } from 'lucide-react';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  
  // Form State
  const [editingUserId, setEditingUserId] = useState(null);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [gender, setGender] = useState('');
  const [dob, setDob] = useState('');
  const [address, setAddress] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [isFetching, setIsFetching] = useState(true);

  // Fetch existing staff accounts
  const fetchUsers = async () => {
    setIsFetching(true);
    try {
      const { data, error } = await supabase
        .from('staff_accounts')
        .select('*')
        .order('created_at', { ascending: false });

      if (error) {
        if (error.code === '42P01') {
          setMessage({ 
            type: 'error', 
            text: 'Table "staff_accounts" does not exist or needs to be updated. Please run the provided SQL query.' 
          });
        } else {
          throw error;
        }
      } else {
        setUsers(data || []);
      }
    } catch (error) {
      console.error('Error fetching users:', error);
      setMessage({ type: 'error', text: 'Failed to fetch users: ' + error.message });
    } finally {
      setIsFetching(false);
    }
  };

  useEffect(() => {
    // Security check
    const isAuthenticated = localStorage.getItem('adminAuthenticated');
    if (isAuthenticated !== 'true') {
      navigate('/admin-login');
      return;
    }
    fetchUsers();
  }, [navigate]);

  const resetForm = () => {
    setEditingUserId(null);
    setUsername('');
    setEmail('');
    setPhone('');
    setGender('');
    setDob('');
    setAddress('');
    setPassword('');
    setConfirmPassword('');
  };

  const handleLogout = () => {
    localStorage.removeItem('adminAuthenticated');
    navigate('/');
  };

  const handleCreateOrUpdateUser = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setMessage({ type: '', text: '' });

    // When updating, password is only required if they want to change it.
    if (!username || !email) {
      setMessage({ type: 'error', text: 'Username and Email are required fields.' });
      setIsLoading(false);
      return;
    }

    if (!editingUserId && (!password || !confirmPassword)) {
      setMessage({ type: 'error', text: 'Password is required for new accounts.' });
      setIsLoading(false);
      return;
    }

    if (password && password !== confirmPassword) {
      setMessage({ type: 'error', text: 'Passwords do not match.' });
      setIsLoading(false);
      return;
    }

    try {
      const payload = {
        username,
        email,
        phone,
        gender,
        dob: dob || null,
        address,
      };

      if (password) {
        payload.password = password;
        // Reset the one-time change flag if the admin sets a new password
        payload.password_changed = false;
      }

      let error;

      if (editingUserId) {
        // Update existing user
        const { error: updateError } = await supabase
          .from('staff_accounts')
          .update(payload)
          .eq('id', editingUserId);
        error = updateError;
      } else {
        // Insert new user
        payload.status = 'active'; // Default status
        const { error: insertError } = await supabase
          .from('staff_accounts')
          .insert([payload]);
        error = insertError;
      }

      if (error) {
        if (error.message.includes('column') && error.message.includes('does not exist')) {
          setMessage({ type: 'error', text: 'Database schema is old. Please run the SQL command to drop and recreate the table.' });
          throw error;
        }
        throw error;
      }

      setMessage({ type: 'success', text: `User "${username}" ${editingUserId ? 'updated' : 'created'} successfully!` });
      resetForm();
      fetchUsers();
    } catch (error) {
      console.error('Error saving user:', error);
      if (!message.text) {
        setMessage({ type: 'error', text: 'Error saving user: ' + error.message });
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = (user) => {
    setEditingUserId(user.id);
    setUsername(user.username || '');
    setEmail(user.email || '');
    setPhone(user.phone || '');
    setGender(user.gender || '');
    setDob(user.dob || '');
    setAddress(user.address || '');
    setPassword('');
    setConfirmPassword('');
    setMessage({ type: '', text: '' });
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete ${name}? This cannot be undone.`)) {
      return;
    }
    
    try {
      const { error } = await supabase
        .from('staff_accounts')
        .delete()
        .eq('id', id);

      if (error) throw error;
      
      setMessage({ type: 'success', text: `User ${name} deleted successfully.` });
      
      // If we are currently editing this user, reset the form
      if (editingUserId === id) resetForm();
      
      fetchUsers();
    } catch (error) {
      console.error('Error deleting user:', error);
      setMessage({ type: 'error', text: 'Failed to delete user.' });
    }
  };

  const handleToggleSuspend = async (user) => {
    const newStatus = user.status === 'suspended' ? 'active' : 'suspended';
    const action = newStatus === 'suspended' ? 'Suspend' : 'Reactivate';
    
    if (!window.confirm(`Are you sure you want to ${action} ${user.username}?`)) {
      return;
    }

    try {
      const { error } = await supabase
        .from('staff_accounts')
        .update({ status: newStatus })
        .eq('id', user.id);

      if (error) throw error;
      
      setMessage({ type: 'success', text: `User ${user.username} is now ${newStatus}.` });
      fetchUsers();
    } catch (error) {
      console.error(`Error trying to ${action} user:`, error);
      setMessage({ type: 'error', text: `Failed to ${action} user.` });
    }
  };

  return (
    <div className="admin-dashboard animation-fade-in">
      <div className="flex justify-between items-center mb-8">
        <button onClick={() => navigate('/')} className="flex items-center gap-2 text-slate-500 hover:text-slate-800 transition-colors bg-white px-4 py-2 rounded-lg border shadow-sm">
          <ArrowLeft size={16} /> Back to Roles
        </button>
        <button onClick={handleLogout} className="flex items-center gap-2 text-danger-color hover:text-red-700 transition-colors bg-white px-4 py-2 rounded-lg border border-red-200 shadow-sm font-medium">
          Logout
        </button>
      </div>

      <div className="admin-header">
        <h1>Admin Dashboard</h1>
        <p>Create and manage detailed staff accounts.</p>
      </div>

      {message.text && (
        <div className={`alert-message ${message.type === 'error' ? 'alert-error' : 'alert-success'}`}>
          {message.type === 'error' ? <AlertCircle size={20} /> : <CheckCircle2 size={20} />}
          {message.text}
        </div>
      )}

      {message.text && (message.text.includes('42P01') || message.text.includes('schema is old') || message.text.includes('status')) && (
        <div className="bg-slate-800 p-6 rounded-lg text-white mb-8">
          <h3 className="font-bold mb-4 text-warning">Action Required: Update Table</h3>
          <p className="mb-4 text-slate-300">Run this SQL in your Supabase SQL Editor to support the new features (like Suspend):</p>
          <pre className="bg-black p-4 rounded text-sm text-green-400 overflow-x-auto">
{`DROP TABLE IF EXISTS staff_accounts;

CREATE TABLE staff_accounts (
  id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
  username text UNIQUE NOT NULL,
  email text,
  phone text,
  gender text,
  dob date,
  address text,
  password text NOT NULL,
  status text DEFAULT 'active',
  created_at timestamp with time zone DEFAULT timezone('utc'::text, now()) NOT NULL
);

ALTER TABLE staff_accounts DISABLE ROW LEVEL SECURITY;
GRANT SELECT, INSERT, UPDATE, DELETE ON staff_accounts TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON staff_accounts TO authenticated;`}
          </pre>
        </div>
      )}

      <div className="admin-grid" style={{ gridTemplateColumns: '1fr 2fr' }}>
        <div className="admin-card">
          <h2>
            {editingUserId ? <Edit2 className="text-primary" size={24} /> : <UserPlus className="text-primary" size={24} />} 
            {editingUserId ? 'Edit User Profile' : 'Create User Profile'}
          </h2>
          <form className="admin-form" onSubmit={handleCreateOrUpdateUser}>
            
            <div className="form-grid" style={{ gridTemplateColumns: '1fr' }}>
              <div className="form-group">
                <label>Username *</label>
                <input
                  type="text"
                  placeholder="e.g. drsmith"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  autoComplete="off"
                />
              </div>
              <div className="form-group">
                <label>Email Address *</label>
                <input
                  type="email"
                  placeholder="e.g. drsmith@clinic.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="email"
                />
              </div>

              <div className="form-group">
                <label>Phone Number</label>
                <input
                  type="tel"
                  placeholder="e.g. +1 555-0123"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Gender</label>
                <select value={gender} onChange={(e) => setGender(e.target.value)}>
                  <option value="">Select gender...</option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other</option>
                  <option value="Prefer not to say">Prefer not to say</option>
                </select>
              </div>

              <div className="form-group">
                <label>Date of Birth</label>
                <input
                  type="date"
                  value={dob}
                  onChange={(e) => setDob(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Address</label>
                <textarea
                  placeholder="Enter full address..."
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>Password {editingUserId ? '(Leave blank to keep)' : '*'}</label>
                <input
                  type="password"
                  placeholder={editingUserId ? "Leave blank to keep current" : "Create password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="new-password"
                />
              </div>
              {(!editingUserId || password) && (
                <div className="form-group">
                  <label>Re-enter Password *</label>
                  <input
                    type="password"
                    placeholder="Confirm password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    autoComplete="new-password"
                  />
                </div>
              )}
            </div>

            <div className="flex gap-2" style={{ marginTop: '1rem' }}>
              <button type="submit" disabled={isLoading || isFetching} style={{ flex: 1 }}>
                {isLoading ? 'Saving...' : editingUserId ? 'Update Account' : 'Create Account'}
              </button>
              {editingUserId && (
                <button type="button" onClick={resetForm} style={{ background: '#64748b', flex: 0.5 }}>
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>

        <div className="admin-card">
          <h2><Users className="text-primary" size={24} /> Registered Staff</h2>
          <div className="user-table-container">
            <table className="user-table">
              <thead>
                <tr>
                  <th>Staff Member</th>
                  <th>Contact</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {isFetching ? (
                  <tr>
                    <td colSpan="4" className="text-center text-muted">Loading...</td>
                  </tr>
                ) : users.length === 0 ? (
                  <tr>
                    <td colSpan="4">
                      <div className="empty-state">
                        <ShieldCheck size={48} />
                        <p>No staff accounts found.</p>
                      </div>
                    </td>
                  </tr>
                ) : (
                  users.map((user) => (
                    <tr key={user.id} className={user.status === 'suspended' ? 'user-row-suspended' : ''}>
                      <td>
                        <div className="font-medium">{user.username}</div>
                        <div className="text-xs text-muted" style={{color: '#64748b'}}>{user.gender}</div>
                      </td>
                      <td>
                        <div className="font-medium text-sm">{user.email || 'N/A'}</div>
                        <div className="text-xs text-muted" style={{color: '#64748b'}}>{user.phone}</div>
                      </td>
                      <td>
                        <span className={`status-badge ${user.status === 'suspended' ? 'status-suspended' : 'status-active'}`}>
                          {user.status || 'active'}
                        </span>
                      </td>
                      <td>
                        <div className="action-buttons">
                          <button 
                            className="action-btn btn-edit" 
                            title="Edit"
                            onClick={() => handleEdit(user)}
                          >
                            <Edit2 size={14} /> Edit
                          </button>
                          <button 
                            className="action-btn btn-suspend" 
                            title={user.status === 'suspended' ? 'Reactivate' : 'Suspend'}
                            onClick={() => handleToggleSuspend(user)}
                          >
                            {user.status === 'suspended' ? <Play size={14} /> : <Pause size={14} />} 
                            {user.status === 'suspended' ? 'Reactivate' : 'Suspend'}
                          </button>
                          <button 
                            className="action-btn btn-delete" 
                            title="Delete"
                            onClick={() => handleDelete(user.id, user.username)}
                          >
                            <Trash2 size={14} /> Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
