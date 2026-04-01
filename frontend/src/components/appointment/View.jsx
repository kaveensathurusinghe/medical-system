import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import api from '../../services/api';
import { motion } from 'framer-motion';

const View = () => {
    const [appointment, setAppointment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { id } = useParams();

    useEffect(() => {
        const fetchAppointment = async () => {
            try {
                const response = await api.get(`/appointments/${id}`);
                setAppointment(response.data);
                setLoading(false);
            } catch (err) {
                setError('Failed to fetch appointment details.');
                setLoading(false);
                console.error(err);
            }
        };

        fetchAppointment();
    }, [id]);

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

    if (loading) return <div className="flex justify-center items-center h-screen"><p>Loading...</p></div>;
    if (error) return <div className="flex justify-center items-center h-screen"><p className="text-red-500">{error}</p></div>;
    if (!appointment) return <div className="flex justify-center items-center h-screen"><p>Appointment not found.</p></div>;

    return (
        <motion.div 
            className="container mx-auto p-8 max-w-3xl"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5 }}
        >
            <div className="bg-white p-8 rounded-lg shadow-2xl">
                <div className="flex justify-between items-start mb-6 pb-6 border-b">
                    <div>
                        <h1 className="text-4xl font-bold text-gray-800">Appointment Details</h1>
                        <p className="text-gray-500">Appointment #{appointment.appointmentId}</p>
                    </div>
                    <span className={`px-4 py-2 text-md font-semibold rounded-full ${getStatusColor(appointment.status)}`}>
                        {appointment.status}
                    </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <motion.div initial={{ x: -20, opacity: 0 }} animate={{ x: 0, opacity: 1 }} transition={{ delay: 0.2 }}>
                        <h2 className="text-2xl font-bold text-gray-700 mb-4">Patient Information</h2>
                        <div className="space-y-3 text-gray-600">
                            <p><span className="font-semibold text-gray-800">Name:</span> {appointment.patientName}</p>
                        </div>
                    </motion.div>
                    
                    <motion.div initial={{ x: 20, opacity: 0 }} animate={{ x: 0, opacity: 1 }} transition={{ delay: 0.3 }}>
                        <h2 className="text-2xl font-bold text-gray-700 mb-4">Doctor Information</h2>
                        <div className="space-y-3 text-gray-600">
                            <p><span className="font-semibold text-gray-800">Name:</span> Dr. {appointment.doctorName}</p>
                        </div>
                    </motion.div>

                    <motion.div className="md:col-span-2" initial={{ y: 20, opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ delay: 0.4 }}>
                        <h2 className="text-2xl font-bold text-gray-700 mb-4">Appointment Information</h2>
                        <div className="space-y-3 text-gray-600">
                            <p><span className="font-semibold text-gray-800">Date & Time:</span> {new Date(appointment.appointmentTime).toLocaleString()}</p>
                            <p><span className="font-semibold text-gray-800">Reason:</span> {appointment.reason}</p>
                        </div>
                    </motion.div>
                </div>
            </div>
        </motion.div>
    );
};

export default View;