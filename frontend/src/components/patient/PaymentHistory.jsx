import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { motion } from 'framer-motion';

const PaymentHistory = () => {
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchPayments = async () => {
            try {
                // Hardcoded patient ID, replace with auth context
                const patientId = 1;
                const response = await api.get(`/payments/patient/${patientId}`);
                setPayments(response.data);
                setLoading(false);
            } catch (err) {
                setError('Failed to fetch payment history.');
                setLoading(false);
                console.error(err);
            }
        };
        fetchPayments();
    }, []);

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
                return 'bg-green-100 text-green-800';
            case 'FAILED':
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
            <h1 className="text-4xl font-bold mb-8 text-gray-800 border-b pb-4">My Payment History</h1>
            
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
                                    <p className="text-lg font-semibold text-gray-800">${payment.amount.toFixed(2)}</p>
                                </div>
                                <span className={`px-3 py-1 text-sm font-semibold rounded-full ${getStatusColor(payment.status)}`}>
                                    {payment.status}
                                </span>
                            </div>
                            <div className="mt-4 text-gray-600">
                                <p><span className="font-semibold">Date:</span> {new Date(payment.paymentDate).toLocaleString()}</p>
                                <p><span className="font-semibold">Payment Method:</span> {payment.paymentMethod}</p>
                            </div>
                        </div>
                    </motion.div>
                ))}
            </motion.div>
        </motion.div>
    );
};

export default PaymentHistory;