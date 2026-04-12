import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { motion } from 'framer-motion';

const Payments = () => {
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchPayments = async () => {
            try {
                const response = await api.get('/admin/payments');
                setPayments(response.data);
                setLoading(false);
            } catch (err) {
                setError('Failed to fetch payments. Please try again later.');
                setLoading(false);
                console.error(err);
            }
        };
        fetchPayments();
    }, []);

    const totalCollected = payments.reduce((sum, payment) => sum + (Number(payment.amount) || 0), 0);
    const totalDoctorCharges = payments.reduce(
        (sum, payment) => sum + (Number(payment.doctorCharge ?? payment.amount) || 0),
        0,
    );

    const containerVariants = {
        hidden: { opacity: 0 },
        visible: {
            opacity: 1,
            transition: {
                staggerChildren: 0.1
            }
        }
    };

    const itemVariants = {
        hidden: { y: 20, opacity: 0 },
        visible: {
            y: 0,
            opacity: 1,
            transition: {
                type: 'spring',
                stiffness: 100
            }
        }
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'COMPLETED':
            case 'PAID':
                return 'bg-green-100 text-green-800';
            case 'FAILED':
            case 'UNPAID':
                return 'bg-red-100 text-red-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

    if (loading) {
        return <div className="flex justify-center items-center h-screen"><div className="text-xl font-semibold">Loading...</div></div>;
    }

    if (error) {
        return <div className="flex justify-center items-center h-screen"><div className="text-red-500 text-xl">{error}</div></div>;
    }

    return (
        <motion.div
            className="container mx-auto p-8"
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
        >
            <h1 className="text-4xl font-bold mb-8 text-gray-800 border-b pb-4">Manage Payments</h1>

            <div className="mb-8 grid grid-cols-1 gap-4 md:grid-cols-3">
                <div className="rounded-lg bg-white p-5 shadow">
                    <p className="text-sm font-medium text-gray-500">Total Payments</p>
                    <p className="mt-2 text-2xl font-bold text-gray-800">{payments.length}</p>
                </div>
                <div className="rounded-lg bg-white p-5 shadow">
                    <p className="text-sm font-medium text-gray-500">Collected Amount</p>
                    <p className="mt-2 text-2xl font-bold text-gray-800">LKR {totalCollected.toFixed(2)}</p>
                </div>
                <div className="rounded-lg bg-white p-5 shadow">
                    <p className="text-sm font-medium text-gray-500">Doctor Charges</p>
                    <p className="mt-2 text-2xl font-bold text-gray-800">LKR {totalDoctorCharges.toFixed(2)}</p>
                </div>
            </div>

            <motion.div
                className="space-y-6"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                {payments.map(payment => (
                    <motion.div
                        key={payment.paymentId}
                        className="bg-white rounded-lg shadow-lg overflow-hidden transform hover:-translate-y-1 transition-transform duration-300"
                        variants={itemVariants}
                    >
                        <div className="p-6">
                            <div className="flex justify-between items-start">
                                <div>
                                    <h2 className="text-xl font-bold text-gray-900 mb-1">Payment #{payment.paymentId}</h2>
                                    <p className="text-md text-gray-700">Patient ID: <span className="font-semibold">{payment.patientId}</span></p>
                                    <p className="text-md text-gray-700">Doctor ID: <span className="font-semibold">{payment.doctorId || 'N/A'}</span></p>
                                    <p className="text-md text-gray-700">Appointment ID: <span className="font-semibold">{payment.appointmentId || 'N/A'}</span></p>
                                </div>
                                <span className={`px-3 py-1 text-sm font-semibold rounded-full ${getStatusColor(payment.status)}`}>
                                    {payment.status}
                                </span>
                            </div>
                            <div className="mt-4 text-gray-600">
                                <p className="text-2xl font-bold text-gray-800">LKR {(Number(payment.amount) || 0).toFixed(2)}</p>
                                <p><span className="font-semibold">Doctor Charge:</span> LKR {(Number(payment.doctorCharge ?? payment.amount) || 0).toFixed(2)}</p>
                                <p><span className="font-semibold">Date:</span> {payment.paymentDate ? new Date(payment.paymentDate).toLocaleString() : 'N/A'}</p>
                                <p><span className="font-semibold">Payment Method:</span> {payment.paymentMethod || 'N/A'}</p>
                            </div>
                        </div>
                    </motion.div>
                ))}
            </motion.div>
        </motion.div>
    );
};

export default Payments;