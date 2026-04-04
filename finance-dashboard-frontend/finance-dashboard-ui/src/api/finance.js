import api from './api';

export const getRecords = (params = {}) =>
  api.get('/api/finance/records', { params });

export const getRecordById = (id) =>
  api.get(`/api/finance/records/${id}`);

export const createRecord = (data) =>
  api.post('/api/finance/records', data);

export const updateRecord = (id, data) =>
  api.put(`/api/finance/records/${id}`, data);

export const deleteRecord = (id) =>
  api.delete(`/api/finance/records/${id}`);

export const getDashboardSummary = () =>
  api.get('/api/finance/dashboard/summary');

export const getCategoryBreakdown = (type) =>
  api.get('/api/finance/dashboard/category-breakdown', {
    params: type ? { type } : {},
  });
