import api from '../services/api';

export const resolveUserId = async (expectedRole) => {
  const storedRole = localStorage.getItem('role');
  const storedUserId = localStorage.getItem('userId');

  const numericStoredUserId = storedUserId && /^\d+$/.test(storedUserId)
    ? Number(storedUserId)
    : null;

  if (storedRole === expectedRole && numericStoredUserId !== null) {
    return numericStoredUserId;
  }

  const response = await api.get('/session/me');
  const role = response?.data?.role;
  const userId = response?.data?.userId;

  if (!role || !userId || role !== expectedRole) {
    throw new Error(`Please sign in as ${expectedRole.replace('ROLE_', '').toLowerCase()}.`);
  }

  localStorage.setItem('role', role);
  localStorage.setItem('userId', String(userId));
  return Number(userId);
};
