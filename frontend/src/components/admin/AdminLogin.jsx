import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import keycloakService from '../../keycloak';

const AdminLogin = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const role = localStorage.getItem('role');
    if (role === 'ROLE_ADMIN') {
      navigate('/admin/dashboard');
    }
  }, [navigate]);

  const handleLogin = async (e) => {
    e && e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await keycloakService.login();
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
        <h1 className="text-3xl font-bold text-slate-900">Admin Login</h1>
        <p className="mt-2 text-sm text-slate-600">Sign in as site administrator</p>

        {error && <p className="mt-4 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

        <div className="mt-6 space-y-4">
          <motion.button
            type="button"
            onClick={handleLogin}
            className="mt-2 w-full rounded-xl bg-slate-900 px-4 py-3 font-semibold text-white transition hover:bg-slate-800 disabled:opacity-60"
            whileHover={{ scale: 1.01 }}
            whileTap={{ scale: 0.99 }}
            disabled={loading}
          >
            {loading ? 'Redirecting...' : 'Sign in with SSO'}
          </motion.button>
        </div>

        <p className="mt-6 text-sm text-slate-600">
          Staff or patient account?{' '}
          <Link to="/login" className="font-semibold text-cyan-800 hover:underline">
            Go to regular login
          </Link>
        </p>
      </motion.div>
    </div>
  );
};

export default AdminLogin;
