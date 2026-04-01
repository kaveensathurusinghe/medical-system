import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowRight, Calendar, HeartPulse, ShieldCheck, Stethoscope } from 'lucide-react';
import api from '../services/api';

const Home = () => {
  const [glanceStats, setGlanceStats] = useState({
    appointmentCount: 0,
    recordCount: 0,
    paymentTotal: 0,
  });
  const [loadingStats, setLoadingStats] = useState(true);

  useEffect(() => {
    const fetchGlanceStats = async () => {
      try {
        const response = await api.get('/admin/dashboard-stats');
        setGlanceStats({
          appointmentCount: response.data?.appointmentCount ?? 0,
          recordCount: response.data?.recordCount ?? 0,
          paymentTotal: response.data?.paymentTotal ?? 0,
        });
      } catch (error) {
        console.error('Unable to load home glance stats', error);
      } finally {
        setLoadingStats(false);
      }
    };

    fetchGlanceStats();
  }, []);

  const formatCurrency = (value) => (
    new Intl.NumberFormat('en-LK', {
      style: 'currency',
      currency: 'LKR',
      maximumFractionDigits: 0,
    }).format(Number(value) || 0)
  );

  return (
    <div className="hero-grid min-h-screen px-4 py-10 sm:px-6 lg:px-8">
      <div className="mx-auto grid w-full max-w-6xl gap-8 lg:grid-cols-2">
        <motion.section
          className="glass-card rounded-3xl p-8 sm:p-10"
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.55 }}
        >
          <span className="brand-pill">Digital Care Platform</span>
          <h1 className="mt-5 text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
            Welcome to MediCare
          </h1>
          <p className="mt-4 max-w-xl text-base leading-7 text-slate-600 sm:text-lg">
            Modern appointment management, patient records, and role-based dashboards for
            administrators, doctors, and patients.
          </p>

          <div className="mt-7 flex flex-wrap gap-3">
            <Link
              to="/login"
              className="inline-flex items-center gap-2 rounded-xl bg-cyan-700 px-5 py-3 font-semibold text-white transition hover:bg-cyan-800"
            >
              Login
              <ArrowRight size={16} />
            </Link>
            <Link
              to="/register"
              className="inline-flex items-center gap-2 rounded-xl border border-cyan-700 px-5 py-3 font-semibold text-cyan-800 transition hover:bg-cyan-50"
            >
              Register
            </Link>
          </div>

          <div className="mt-8 grid gap-3 sm:grid-cols-3">
              <Link to="/admin/login" className="surface-card rounded-2xl p-4 transition hover:-translate-y-0.5">
              <ShieldCheck className="mb-2 text-cyan-700" size={20} />
              <p className="text-sm font-semibold text-slate-900">Admin</p>
            </Link>
            <Link to="/login" className="surface-card rounded-2xl p-4 transition hover:-translate-y-0.5">
              <Stethoscope className="mb-2 text-cyan-700" size={20} />
              <p className="text-sm font-semibold text-slate-900">Doctor</p>
            </Link>
            <Link to="/login" className="surface-card rounded-2xl p-4 transition hover:-translate-y-0.5">
              <HeartPulse className="mb-2 text-cyan-700" size={20} />
              <p className="text-sm font-semibold text-slate-900">Patient</p>
            </Link>
          </div>
        </motion.section>

        <motion.aside
          className="surface-card rounded-3xl p-8 sm:p-10"
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.55, delay: 0.08 }}
        >
          <h2 className="text-2xl font-bold text-slate-900">Today at a Glance</h2>
            <p className="mt-2 text-slate-600">A quick live snapshot of appointments, records, and payments across your clinic.</p>

          <div className="mt-6 space-y-4">
            <div className="surface-card rounded-2xl p-4">
              <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-slate-500">Appointments</p>
                <Calendar size={18} className="text-cyan-700" />
              </div>
                <p className="mt-2 text-2xl font-bold text-slate-900">
                  {loadingStats ? '...' : glanceStats.appointmentCount}
                </p>
              <p className="mt-1 text-sm text-slate-500">Scheduled across all doctors</p>
            </div>

            <div className="surface-card rounded-2xl p-4">
              <p className="text-sm font-medium text-slate-500">Records Updated</p>
                <p className="mt-2 text-2xl font-bold text-slate-900">
                  {loadingStats ? '...' : glanceStats.recordCount}
                </p>
              <p className="mt-1 text-sm text-slate-500">Latest notes synchronized</p>
            </div>

            <div className="surface-card rounded-2xl p-4">
              <p className="text-sm font-medium text-slate-500">Payments Processed</p>
                <p className="mt-2 text-2xl font-bold text-slate-900">
                  {loadingStats ? '...' : formatCurrency(glanceStats.paymentTotal)}
                </p>
                <p className="mt-1 text-sm text-slate-500">Total collected amount</p>
            </div>
          </div>
        </motion.aside>
      </div>
    </div>
  );
};

export default Home;
