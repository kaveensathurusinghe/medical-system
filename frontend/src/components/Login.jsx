import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import keycloakService from '../keycloak';

const roleRoute = {
  ROLE_ADMIN: '/admin/dashboard',
  ROLE_DOCTOR: '/doctor/dashboard',
  ROLE_PATIENT: '/patient/dashboard',
};

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const role = localStorage.getItem('role');
    if (role && roleRoute[role]) {
      navigate(roleRoute[role]);
    }
  }, [navigate]);

  const handleLogin = async (e) => {
    e && e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await keycloakService.login({ username: email, password });
      const role = localStorage.getItem('role');
      if (role && roleRoute[role]) {
        navigate(roleRoute[role]);
      } else {
        navigate('/');
      }
    } catch (err) {
      setError('Login failed');
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

        <form className="mt-6 space-y-4" onSubmit={handleLogin}>
          <div>
            <label className="block text-sm font-medium text-slate-700">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="mt-1 w-full rounded-md border px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="mt-1 w-full rounded-md border px-3 py-2"
            />
          </div>
          <motion.button
            type="submit"
            className="mt-2 w-full rounded-xl bg-cyan-700 px-4 py-3 font-semibold text-white transition hover:bg-cyan-800 disabled:opacity-60"
            whileHover={{ scale: 1.01 }}
            whileTap={{ scale: 0.99 }}
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </motion.button>
        </form>

        <p className="mt-6 text-sm text-slate-600">
          Need an account?{' '}
          <Link to="/register" className="font-semibold text-cyan-800 hover:underline">
            Register
          </Link>
        </p>

          <p className="mt-2 text-sm text-slate-600">
            Site admin?{' '}
            <Link to="/admin/login" className="font-semibold text-slate-900 hover:underline">
              Admin login
            </Link>
          </p>
      </motion.div>
    </div>
  );
};

export default Login;
