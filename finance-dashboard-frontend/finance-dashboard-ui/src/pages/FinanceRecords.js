import React, { useEffect, useState, useCallback } from 'react';
import { getRecords, createRecord, updateRecord, deleteRecord } from '../api/finance';
import { useAuth } from '../context/AuthContext';

const fmt = (n) => `₹${Number(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
const CATEGORIES = ['Salary','Freelance','Investment','Rent','Groceries','Utilities','Healthcare','Transport','Education','Entertainment','Food','Shopping','Other'];
const EMPTY_FORM = { amount: '', type: 'INCOME', category: 'Salary', date: new Date().toISOString().slice(0, 10), notes: '' };

function RecordModal({ record, onClose, onSave }) {
  const [form, setForm]     = useState(record || EMPTY_FORM);
  const [error, setError]   = useState('');
  const [saving, setSaving] = useState(false);

  const set = (k) => (e) => setForm(f => ({ ...f, [k]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.amount || Number(form.amount) <= 0) { setError('Amount must be greater than 0.'); return; }
    setSaving(true);
    try {
      const payload = { ...form, amount: parseFloat(form.amount) };
      if (record?.id) {
        const { data } = await updateRecord(record.id, payload);
        onSave(data, 'update');
      } else {
        const { data } = await createRecord(payload);
        onSave(data, 'create');
      }
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save record.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h2>{record?.id ? '✏️ Edit Record' : '➕ New Record'}</h2>
          <button className="btn btn-ghost btn-icon" onClick={onClose}>✕</button>
        </div>

        {error && <div className="alert alert-error" style={{ marginBottom: 0 }}>⚠️ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
              <div className="form-group">
                <label>Type</label>
                <select value={form.type} onChange={set('type')}>
                  <option value="INCOME">INCOME</option>
                  <option value="EXPENSE">EXPENSE</option>
                </select>
              </div>
              <div className="form-group">
                <label>Amount (₹)</label>
                <input type="number" min="0.01" step="0.01" placeholder="0.00" value={form.amount} onChange={set('amount')} required />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
              <div className="form-group">
                <label>Category</label>
                <select value={form.category} onChange={set('category')}>
                  {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Date</label>
                <input type="date" value={form.date} onChange={set('date')} required />
              </div>
            </div>
            <div className="form-group">
              <label>Notes</label>
              <textarea rows={2} placeholder="Optional description…" value={form.notes || ''} onChange={set('notes')} style={{ resize: 'vertical' }} />
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving…' : record?.id ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function FinanceRecords() {
  const { hasRole } = useAuth();
  const [records, setRecords]   = useState([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState('');
  const [success, setSuccess]   = useState('');
  const [modal, setModal]       = useState(null); // null | 'create' | record object
  const [filters, setFilters]   = useState({ type: '', category: '', from: '', to: '' });

  const canWrite  = hasRole('ANALYST', 'ADMIN');
  const canDelete = hasRole('ADMIN');

  const flash = (msg) => { setSuccess(msg); setTimeout(() => setSuccess(''), 3000); };

  const fetchRecords = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const params = {};
      if (filters.type)     params.type     = filters.type;
      if (filters.category) params.category = filters.category;
      if (filters.from)     params.from     = filters.from;
      if (filters.to)       params.to       = filters.to;
      const { data } = await getRecords(params);
      setRecords(data);
    } catch {
      setError('Failed to load records.');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => { fetchRecords(); }, [fetchRecords]);

  const handleSave = async (saved, op) => {
    await fetchRecords();
    flash(op === 'create' ? '✅ Record created successfully!' : '✅ Record updated successfully!');
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this financial record? This cannot be undone.')) return;
    try {
      await deleteRecord(id);
      setRecords(r => r.filter(x => x.id !== id));
      flash('✅ Record deleted.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete record.');
    }
  };

  const setFilter = (k) => (e) => setFilters(f => ({ ...f, [k]: e.target.value }));
  const clearFilters = () => setFilters({ type: '', category: '', from: '', to: '' });

  const totalIncome   = records.filter(r => r.type === 'INCOME').reduce((s, r) => s + Number(r.amount), 0);
  const totalExpenses = records.filter(r => r.type === 'EXPENSE').reduce((s, r) => s + Number(r.amount), 0);

  return (
    <div>
      {error   && <div className="alert alert-error">⚠️ {error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {/* ── Summary strip ────────────────────────────────── */}
      <div className="stat-grid" style={{ marginBottom: 20 }}>
        <div className="stat-card">
          <div className="stat-icon income">📈</div>
          <div className="stat-info"><div className="stat-label">Showing Income</div><div className="stat-value income">{fmt(totalIncome)}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon expense">📉</div>
          <div className="stat-info"><div className="stat-label">Showing Expenses</div><div className="stat-value expense">{fmt(totalExpenses)}</div></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon neutral">🗂️</div>
          <div className="stat-info"><div className="stat-label">Showing Records</div><div className="stat-value">{records.length}</div></div>
        </div>
      </div>

      {/* ── Filters ──────────────────────────────────────── */}
      <div className="filter-bar">
        <div className="filter-group">
          <label>Type:</label>
          <select value={filters.type} onChange={setFilter('type')} style={{ minWidth: 120 }}>
            <option value="">All</option>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </select>
        </div>
        <div className="filter-group">
          <label>Category:</label>
          <select value={filters.category} onChange={setFilter('category')} style={{ minWidth: 140 }}>
            <option value="">All</option>
            {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
          </select>
        </div>
        <div className="filter-group">
          <label>From:</label>
          <input type="date" value={filters.from} onChange={setFilter('from')} style={{ minWidth: 130 }} />
        </div>
        <div className="filter-group">
          <label>To:</label>
          <input type="date" value={filters.to} onChange={setFilter('to')} style={{ minWidth: 130 }} />
        </div>
        {(filters.type || filters.category || filters.from || filters.to) && (
          <button className="btn btn-ghost btn-sm" onClick={clearFilters}>✕ Clear</button>
        )}
      </div>

      {/* ── Header ───────────────────────────────────────── */}
      <div className="section-header">
        <h2>Financial Records ({records.length})</h2>
        {canWrite && (
          <button className="btn btn-primary" onClick={() => setModal({})}>
            ➕ Add Record
          </button>
        )}
      </div>

      {/* ── Table ────────────────────────────────────────── */}
      {loading
        ? <div className="spinner-wrap"><div className="spinner" /></div>
        : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>#</th><th>Date</th><th>Category</th><th>Type</th>
                  <th style={{ textAlign: 'right' }}>Amount</th><th>Notes</th><th>Created by</th>
                  {(canWrite || canDelete) && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {records.length === 0
                  ? <tr><td colSpan={8}><div className="empty-state"><div className="empty-icon">📭</div><p>No records found. {canWrite ? "Click 'Add Record' to create one." : ''}</p></div></td></tr>
                  : records.map(r => (
                    <tr key={r.id}>
                      <td style={{ color: 'var(--clr-text-dim)', fontSize: '.8rem' }}>{r.id}</td>
                      <td style={{ color: 'var(--clr-text-muted)', fontSize: '.85rem', whiteSpace: 'nowrap' }}>{r.date}</td>
                      <td style={{ fontWeight: 500 }}>{r.category}</td>
                      <td><span className={`badge badge-${r.type.toLowerCase()}`}>{r.type}</span></td>
                      <td style={{ textAlign: 'right', fontWeight: 600 }}
                        className={r.type === 'INCOME' ? 'text-income' : 'text-expense'}>
                        {r.type === 'INCOME' ? '+' : '-'}{fmt(r.amount)}
                      </td>
                      <td style={{ color: 'var(--clr-text-muted)', fontSize: '.82rem', maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {r.notes || '—'}
                      </td>
                      <td style={{ color: 'var(--clr-text-dim)', fontSize: '.78rem' }}>{r.createdBy || '—'}</td>
                      {(canWrite || canDelete) && (
                        <td>
                          <div style={{ display: 'flex', gap: 6 }}>
                            {canWrite  && <button className="btn btn-ghost btn-sm" onClick={() => setModal(r)}>✏️</button>}
                            {canDelete && <button className="btn btn-danger btn-sm" onClick={() => handleDelete(r.id)}>🗑️</button>}
                          </div>
                        </td>
                      )}
                    </tr>
                  ))
                }
              </tbody>
            </table>
          </div>
        )
      }

      {modal !== null && (
        <RecordModal
          record={modal?.id ? modal : null}
          onClose={() => setModal(null)}
          onSave={handleSave}
        />
      )}
    </div>
  );
}
