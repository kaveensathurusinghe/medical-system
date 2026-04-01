import React, { useEffect, useState } from 'react';
import api from '../../services/api';
import { motion } from 'framer-motion';

const Dashboard = () => {
  const [stats, setStats] = useState({
    patientCount: 0,
    doctorCount: 0,
    appointmentCount: 0,
    paymentTotal: 0,
  });
  const [recentAppointments, setRecentAppointments] = useState([]);
  const [recentPayments, setRecentPayments] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const statsRes = await api.get('/admin/dashboard-stats');
        setStats(statsRes.data);
        const appointmentsRes = await api.get('/admin/recent-appointments');
        setRecentAppointments(appointmentsRes.data);
        const paymentsRes = await api.get('/admin/recent-payments');
        setRecentPayments(paymentsRes.data);
      } catch (error) {
        console.error("Error fetching dashboard data", error);
      }
    };
    fetchData();
  }, []);

  return (
    <motion.div 
      className="container mt-4"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
    >
      <h1 className="mb-4">Admin Dashboard</h1>
      <div className="row">
        {Object.entries(stats).map(([key, value], index) => (
          <motion.div 
            key={key} 
            className="col-md-3 mb-4"
            initial={{ y: -50, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: index * 0.1 }}
          >
            <div className="card shadow-sm">
              <div className="card-body text-center">
                <h5 className="card-title">{key.replace(/([A-Z])/g, ' $1').toUpperCase()}</h5>
                <p className="card-text fs-4">{value}</p>
              </div>
            </div>
          </motion.div>
        ))}
      </div>
      <div className="row mt-4">
        <div className="col-md-6">
          <h2>Recent Appointments</h2>
          <ul className="list-group">
            {recentAppointments.map(app => (
              <li key={app.appointmentId} className="list-group-item">{app.patientName} with {app.doctorName}</li>
            ))}
          </ul>
        </div>
        <div className="col-md-6">
          <h2>Recent Payments</h2>
          <ul className="list-group">
            {recentPayments.map(p => (
              <li key={p.paymentId} className="list-group-item">Amount: {p.amount} LKR</li>
            ))}
          </ul>
        </div>
      </div>
    </motion.div>
  );
};

export default Dashboard;