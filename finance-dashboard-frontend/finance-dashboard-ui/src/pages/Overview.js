import React, { useEffect, useState, useCallback } from 'react';
import {
  AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts';
import { getDashboardSummary } from '../api/finance';
import { useAuth } from '../context/AuthContext';

const fmt = (n) => `₹${Number(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
const PIE_COLORS = ['#4f7ef8','#22c55e','#f59e0b','#ef4444','#8b5cf6','#06b6d4','#ec4899','#84cc16'];

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{ background: '#1a1f2e', border: '1px solid #2e3554', borderRadius: 8, padding: '10px 14px', fontSize: '.8rem' }}>
      <p style={{ color: '#8b96b0', marginBottom: 6 }}>{label}</p>
      {payload.map((p, i) => (
        <p key={i} style={{ color: p.color }}>
          {p.name}: {fmt(p.value)}
        </p>
      ))}
    </div>
  );
};

export default function Overview() {
  const { hasRole } = useAuth();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');

  const canView = hasRole('ANALYST', 'ADMIN');

  const fetchSummary = useCallback(async () => {
    if (!canView) { setLoading(false); return; }
    try {
      const { data } = await getDashboardSummary();
      setSummary(data);
    } catch {
      setError('Failed to load dashboard data.');
    } finally {
      setLoading(false);
    }
  }, [canView]);

  useEffect(() => { fetchSummary(); }, [fetchSummary]);

  if (!canView) {
    return (
      <div className="access-denied">
        <div className="lock-icon">🔒</div>
        <h3>Access Restricted</h3>
        <p>The dashboard summary is available to Analysts and Admins only. Contact your administrator to upgrade your role.</p>
      </div>
    );
  }

  if (loading) return <div className="spinner-wrap"><div className="spinner" /></div>;
  if (error)   return <div className="alert alert-error" style={{ marginTop: 0 }}>⚠️ {error}</div>;
  if (!summary) return null;

  const netPositive = Number(summary.netBalance) >= 0;
  const pieData = Object.entries(summary.categoryTotals || {}).map(([name, value]) => ({ name, value: Number(value) }));

  return (
    <div>
      {/* ── Stat Cards ─────────────────────────────────────── */}
      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-icon income">📈</div>
          <div className="stat-info">
            <div className="stat-label">Total Income</div>
            <div className="stat-value income">{fmt(summary.totalIncome)}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon expense">📉</div>
          <div className="stat-info">
            <div className="stat-label">Total Expenses</div>
            <div className="stat-value expense">{fmt(summary.totalExpenses)}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon balance">⚖️</div>
          <div className="stat-info">
            <div className="stat-label">Net Balance</div>
            <div className={`stat-value ${netPositive ? 'positive' : 'negative'}`}>{fmt(summary.netBalance)}</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon neutral">🗂️</div>
          <div className="stat-info">
            <div className="stat-label">Total Records</div>
            <div className="stat-value">{summary.totalRecords}</div>
          </div>
        </div>
      </div>

      {/* ── Charts ─────────────────────────────────────────── */}
      <div className="charts-grid">
        {/* Monthly Trend */}
        <div className="chart-card" style={{ gridColumn: 'span 2' }}>
          <h3>Monthly Income vs Expenses</h3>
          <ResponsiveContainer width="100%" height={260}>
            <AreaChart data={summary.monthlyTrends || []} margin={{ top: 4, right: 10, bottom: 0, left: 10 }}>
              <defs>
                <linearGradient id="incomeGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#22c55e" stopOpacity={0.25} />
                  <stop offset="95%" stopColor="#22c55e" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="expenseGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#ef4444" stopOpacity={0.25} />
                  <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#2e3554" vertical={false} />
              <XAxis dataKey="month" stroke="#4a5568" tick={{ fill: '#8b96b0', fontSize: 11 }} />
              <YAxis stroke="#4a5568" tick={{ fill: '#8b96b0', fontSize: 11 }} tickFormatter={v => `₹${(v/1000).toFixed(0)}k`} />
              <Tooltip content={<CustomTooltip />} />
              <Legend wrapperStyle={{ fontSize: '.8rem', paddingTop: 12 }} />
              <Area type="monotone" dataKey="income"   name="Income"   stroke="#22c55e" fill="url(#incomeGrad)"  strokeWidth={2} dot={false} />
              <Area type="monotone" dataKey="expenses" name="Expenses" stroke="#ef4444" fill="url(#expenseGrad)" strokeWidth={2} dot={false} />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Category Pie */}
        <div className="chart-card">
          <h3>Spending by Category</h3>
          {pieData.length === 0
            ? <div className="empty-state"><div className="empty-icon">🥧</div><p>No category data yet</p></div>
            : (
              <ResponsiveContainer width="100%" height={240}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={55} outerRadius={95}
                    dataKey="value" nameKey="name" paddingAngle={3}>
                    {pieData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                  </Pie>
                  <Tooltip formatter={(v) => fmt(v)} contentStyle={{ background: '#1a1f2e', border: '1px solid #2e3554', borderRadius: 8, fontSize: '.8rem' }} />
                  <Legend wrapperStyle={{ fontSize: '.78rem' }} />
                </PieChart>
              </ResponsiveContainer>
            )}
        </div>

        {/* Monthly Net Bar */}
        <div className="chart-card">
          <h3>Monthly Net Balance</h3>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={summary.monthlyTrends || []} margin={{ top: 4, right: 10, bottom: 0, left: 10 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#2e3554" vertical={false} />
              <XAxis dataKey="month" stroke="#4a5568" tick={{ fill: '#8b96b0', fontSize: 11 }} />
              <YAxis stroke="#4a5568" tick={{ fill: '#8b96b0', fontSize: 11 }} tickFormatter={v => `₹${(v/1000).toFixed(0)}k`} />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="net" name="Net" radius={[4, 4, 0, 0]}>
                {(summary.monthlyTrends || []).map((entry, i) => (
                  <Cell key={i} fill={Number(entry.net) >= 0 ? '#22c55e' : '#ef4444'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* ── Recent Transactions ─────────────────────────────── */}
      <div className="chart-card">
        <div className="section-header">
          <h3 style={{ marginBottom: 0 }}>Recent Transactions</h3>
        </div>
        <div className="table-wrap" style={{ marginTop: 14 }}>
          <table>
            <thead>
              <tr>
                <th>Date</th><th>Category</th><th>Type</th><th>Amount</th><th>Notes</th>
              </tr>
            </thead>
            <tbody>
              {(summary.recentTransactions || []).length === 0
                ? <tr><td colSpan={5}><div className="empty-state"><div className="empty-icon">📭</div><p>No transactions yet</p></div></td></tr>
                : summary.recentTransactions.map(tx => (
                  <tr key={tx.id}>
                    <td style={{ color: 'var(--clr-text-muted)', fontSize: '.82rem' }}>{tx.date}</td>
                    <td style={{ fontWeight: 500 }}>{tx.category}</td>
                    <td><span className={`badge badge-${tx.type.toLowerCase()}`}>{tx.type}</span></td>
                    <td className={tx.type === 'INCOME' ? 'text-income' : 'text-expense'} style={{ fontWeight: 600 }}>
                      {tx.type === 'INCOME' ? '+' : '-'}{fmt(tx.amount)}
                    </td>
                    <td style={{ color: 'var(--clr-text-muted)', fontSize: '.82rem' }}>{tx.notes || '—'}</td>
                  </tr>
                ))
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
