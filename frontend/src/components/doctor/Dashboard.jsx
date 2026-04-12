import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { motion } from 'framer-motion';
import { Calendar, Clock, Users, Wallet } from 'lucide-react';
import { resolveUserId } from '../../utils/sessionUser';

const StatCard = ({ icon, label, value, color }) => (
    <motion.div 
        className="bg-white p-6 rounded-lg shadow-lg flex items-center"
        whileHover={{ y: -5, boxShadow: "0px 10px 20px rgba(0,0,0,0.1)" }}
    >
        <div className={`p-3 rounded-full mr-4 ${color}`}>
            {icon}
        </div>
        <div>
            <p className="text-gray-500 text-sm font-medium">{label}</p>
            <p className="text-2xl font-bold text-gray-800">{value}</p>
        </div>
    </motion.div>
);

const Dashboard = () => {
    const [stats, setStats] = useState({
        upcomingAppointments: 0,
        todayAppointments: 0,
        totalPatients: 0,
        totalIncome: 0,
    });
    const [recentAppointments, setRecentAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const doctorId = await resolveUserId('ROLE_DOCTOR');
                
                const statsPromise = api.get(`/doctors/${doctorId}/dashboard-stats`);
                const appointmentsPromise = api.get(`/doctors/${doctorId}/appointments?limit=5`);

                const [statsRes, appointmentsRes] = await Promise.all([statsPromise, appointmentsPromise]);

                setStats(statsRes.data);
                setRecentAppointments(appointmentsRes.data);
                setLoading(false);
            } catch (err) {
                setError('Failed to fetch dashboard data.');
                setLoading(false);
                console.error(err);
            }
        };

        fetchDashboardData();
    }, []);

    const containerVariants = {
        hidden: { opacity: 0 },
        visible: {
            opacity: 1,
            transition: { staggerChildren: 0.1, delayChildren: 0.2 }
        }
    };

    const itemVariants = {
        hidden: { y: 20, opacity: 0 },
        visible: { y: 0, opacity: 1 }
    };

    const formatCurrency = (amount) => new Intl.NumberFormat('en-LK', {
        style: 'currency',
        currency: 'LKR',
        minimumFractionDigits: 2,
    }).format(Number(amount) || 0);

    if (loading) return <div className="flex justify-center items-center h-screen"><p>Loading...</p></div>;
    if (error) return <div className="flex justify-center items-center h-screen"><p className="text-red-500">{error}</p></div>;

    return (
        <motion.div 
            className="container mx-auto p-8 bg-gray-50 min-h-screen"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5 }}
        >
            <h1 className="text-4xl font-bold mb-8 text-gray-800">Doctor's Dashboard</h1>

            <motion.div 
                className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 mb-12"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                <StatCard icon={<Calendar size={24} className="text-white"/>} label="Upcoming Appointments" value={stats.upcomingAppointments} color="bg-blue-500" />
                <StatCard icon={<Clock size={24} className="text-white"/>} label="Appointments Today" value={stats.todayAppointments} color="bg-green-500" />
                <StatCard icon={<Users size={24} className="text-white"/>} label="Total Patients" value={stats.totalPatients} color="bg-indigo-500" />
                <StatCard icon={<Wallet size={24} className="text-white"/>} label="Total Income" value={formatCurrency(stats.totalIncome)} color="bg-emerald-500" />
            </motion.div>

            <motion.div variants={itemVariants}>
                <h2 className="text-2xl font-bold mb-6 text-gray-700">Recent Appointments</h2>
                <div className="bg-white rounded-lg shadow-lg overflow-hidden">
                    <ul className="divide-y divide-gray-200">
                        {recentAppointments.length > 0 ? recentAppointments.map(app => (
                            <li key={app.appointmentId} className="p-4 flex justify-between items-center hover:bg-gray-50 transition-colors">
                                <div>
                                    <p className="font-semibold text-gray-800">{app.patientName}</p>
                                    <p className="text-sm text-gray-500">{new Date(app.appointmentTime).toLocaleString()}</p>
                                </div>
                                <span className="text-sm font-medium text-gray-600">{app.reason}</span>
                            </li>
                        )) : (
                            <li className="p-4 text-center text-gray-500">No recent appointments.</li>
                        )}
                    </ul>
                </div>
            </motion.div>
        </motion.div>
    );
};

export default Dashboard;