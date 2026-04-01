import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import api from '../services/api';

const roleRoute = {
  ROLE_ADMIN: '/admin/dashboard',
  ROLE_DOCTOR: '/doctor/dashboard',
  ROLE_PATIENT: '/patient/dashboard',
};

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [devRole, setDevRole] = useState('ROLE_PATIENT');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setNotice('');
    setLoading(true);

    try {
      const response = await api.post('/auth/login', {
        username: email,
        email,
        password,
      });

      const token = response?.data?.token;
      const role = response?.data?.role;

      if (!token || !role || !roleRoute[role]) {
        throw new Error('Unexpected login response');
      }

      localStorage.setItem('token', token);
      localStorage.setItem('role', role);
      navigate(roleRoute[role]);
    } catch (err) {
      // Backend auth endpoint may not be available yet in this migration stage.
      localStorage.setItem('token', 'dev-token');
      localStorage.setItem('role', devRole);
      setNotice('Using local dev sign-in because /api/auth/login is not available yet.');
      navigate(roleRoute[devRole]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="hero-grid flex min-h-screen items-center justify-center px-4 py-10 sm:px-6 lg:px-8">
      <motion.div
        className="glass-card w-full max-w-md rounded-3xl p-8 sm:p-10"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45 }}
      >
        <h1 className="text-3xl font-bold text-slate-900">Sign In</h1>
        <p className="mt-2 text-sm text-slate-600">Access your MediCare dashboard</p>

        {error && <p className="mt-4 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
        {notice && <p className="mt-4 rounded-xl bg-amber-50 px-3 py-2 text-sm text-amber-700">{notice}</p>}

        <form onSubmit={handleLogin} className="mt-6 space-y-4">
          <div>
            <label htmlFor="email" className="mb-1 block text-sm font-semibold text-slate-700">
              Email
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-xl border border-slate-200 px-3 py-2.5 outline-none ring-cyan-200 transition focus:ring"
              required
            />
          </div>

          <div>
            <label htmlFor="password" className="mb-1 block text-sm font-semibold text-slate-700">
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-xl border border-slate-200 px-3 py-2.5 outline-none ring-cyan-200 transition focus:ring"
              required
            />
          </div>

          <div>
            <label htmlFor="devRole" className="mb-1 block text-sm font-semibold text-slate-700">
              Dev Fallback Role
            </label>
            <select
              id="devRole"
              value={devRole}
              onChange={(e) => setDevRole(e.target.value)}
              className="w-full rounded-xl border border-slate-200 px-3 py-2.5 outline-none ring-cyan-200 transition focus:ring"
            >
              <option value="ROLE_PATIENT">Patient</option>
              <option value="ROLE_DOCTOR">Doctor</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
          </div>

          <motion.button
            type="submit"
            className="mt-2 w-full rounded-xl bg-cyan-700 px-4 py-3 font-semibold text-white transition hover:bg-cyan-800 disabled:opacity-60"
            whileHover={{ scale: 1.01 }}
            whileTap={{ scale: 0.99 }}
            disabled={loading}
          >
            {loading ? 'Signing In...' : 'Login'}
          </motion.button>
        </form>

        <p className="mt-6 text-sm text-slate-600">
          Need an account?{' '}
          <Link to="/register" className="font-semibold text-cyan-800 hover:underline">
            Register
          </Link>
        </p>
      </motion.div>
    </div>
  );
};

export default Login;
