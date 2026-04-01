import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { motion } from 'framer-motion';
import { resolveUserId } from '../../utils/sessionUser';

const PatientHistory = () => {
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchAppointments = async () => {
            try {
                const patientId = await resolveUserId('ROLE_PATIENT');
                const response = await api.get(`/appointments/patient/${patientId}`);
                setAppointments(response.data);
                setLoading(false);
            } catch (err) {
                setError('Failed to fetch appointments history.');
                setLoading(false);
                console.error(err);
            }
        };
        fetchAppointments();
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
            case 'CANCELLED':
                return 'bg-red-100 text-red-800';
            case 'SCHEDULED':
                return 'bg-blue-100 text-blue-800';
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
            <h1 className="text-4xl font-bold mb-8 text-gray-800 border-b pb-4">My Appointment History</h1>
            
            <motion.div 
                className="space-y-6"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                {appointments.map(appointment => (
                    <motion.div 
                        key={appointment.appointmentId} 
                        className="bg-white rounded-lg shadow-lg overflow-hidden transform hover:-translate-y-1 transition-transform duration-300"
                        variants={itemVariants}
                    >
                        <div className="p-6">
                            <div className="flex justify-between items-start">
                                <div>
                                    <h2 className="text-xl font-bold text-gray-900 mb-1">Dr. {appointment.doctorName}</h2>
                                </div>
                                <span className={`px-3 py-1 text-sm font-semibold rounded-full ${getStatusColor(appointment.status)}`}>
                                    {appointment.status}
                                </span>
                            </div>
                            <div className="mt-4 text-gray-600">
                                <p><span className="font-semibold">Date:</span> {new Date(appointment.appointmentTime).toLocaleString()}</p>
                                <p><span className="font-semibold">Reason:</span> {appointment.reason}</p>
                            </div>
                        </div>
                    </motion.div>
                ))}
            </motion.div>
        </motion.div>
    );
};

export default PatientHistory;