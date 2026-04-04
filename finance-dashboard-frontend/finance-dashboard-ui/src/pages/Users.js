import React, { useEffect, useState, useCallback } from 'react';
import { getUsers, createUser, updateUser, deleteUser } from '../api/users';
import { useAuth } from '../context/AuthContext';

const ROLES = ['VIEWER', 'ANALYST', 'ADMIN'];
const STATUSES = ['ACTIVE', 'INACTIVE'];
const EMPTY_FORM = { name: '', email: '', password: '', role: 'VIEWER', status: 'ACTIVE' };

function UserModal({ user, onClose, onSave }) {
  const isEdit = Boolean(user?.id);
  const [form, setForm] = useState(user || EMPTY_FORM);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const set = (k) => (e) => setForm(f => ({ ...f, [k]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      if (isEdit) {
        const payload = { name: form.name, email: form.email, role: form.role, status: form.status };
        const { data } = await updateUser(user.id, payload);
        onSave(data, 'update');
      } else {
        const { data } = await createUser({
          name: form.name,
          email: form.email,
          password: form.password,
          role: form.role
        });
        onSave(data, 'create');
      }
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save user.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h2>{isEdit ? '✏️ Edit User' : '👤 New User'}</h2>
          <button className="btn btn-ghost btn-icon" onClick={onClose}>✕</button>
        </div>

        {error && <div className="alert alert-error">⚠️ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="modal-body">

            <div className="form-group">
              <label>Full Name</label>
              <input value={form.name} onChange={set('name')} required />
            </div>

            <div className="form-group">
              <label>Email</label>
              <input value={form.email} onChange={set('email')} required />
            </div>

            {!isEdit && (
              <div className="form-group">
                <label>Password</label>
                <input type="password" value={form.password} onChange={set('password')} required />
              </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
              <div className="form-group">
                <label>Role</label>
                <select value={form.role} onChange={set('role')}>
                  {ROLES.map(r => <option key={r}>{r}</option>)}
                </select>
              </div>

              {isEdit && (
                <div className="form-group">
                  <label>Status</label>
                  <select value={form.status} onChange={set('status')}>
                    {STATUSES.map(s => <option key={s}>{s}</option>)}
                  </select>
                </div>
              )}
            </div>

          </div>

          <div className="modal-footer">
            <button type="button" onClick={onClose}>Cancel</button>
            <button type="submit" disabled={saving}>
              {saving ? 'Saving…' : isEdit ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function Users() {
  const { hasRole, user: me } = useAuth();

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modal, setModal] = useState(null);
  const [search, setSearch] = useState('');

  const isAdmin = hasRole('ADMIN');

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await getUsers();
      setUsers(data);
    } catch {
      setError('Failed to load users.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  if (!isAdmin) {
    return <div>Access Denied</div>;
  }

  const handleDelete = async (id) => {
    if (id === me?.id) return;
    await deleteUser(id);
    setUsers(u => u.filter(x => x.id !== id));
  };

  // ✅ THIS IS THE FIX 🔥
  const handleSave = () => {
    fetchUsers(); // reload list after create/update
  };

  const filtered = users.filter(u =>
    u.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      {error && <p>{error}</p>}

      <input
        placeholder="Search"
        value={search}
        onChange={e => setSearch(e.target.value)}
      />

      <button onClick={() => setModal({})}>Add User</button>

      {modal && (
        <UserModal
          user={modal}
          onClose={() => setModal(null)}
          onSave={handleSave}   // ✅ FIX APPLIED
        />
      )}

      {loading ? (
        <p>Loading...</p>
      ) : (
        <ul>
          {filtered.map(u => (
            <li key={u.id}>
              {u.name} ({u.role})

              <button onClick={() => setModal(u)}>Edit</button>

              <button onClick={() => handleDelete(u.id)}>Delete</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}