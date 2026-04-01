import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { motion } from 'framer-motion';

const Patients = () => {
    const [patients, setPatients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchPatients = async () => {
            try {
                const response = await api.get('/admin/patients');
                setPatients(response.data);
                setLoading(false);
            } catch (err) {
                setError('Failed to fetch patients. Please try again later.');
                setLoading(false);
                console.error(err);
            }
        };
        fetchPatients();
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
            <h1 className="text-4xl font-bold mb-8 text-gray-800 border-b pb-4">Manage Patients</h1>

            <motion.div
                className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                {patients.map(patient => (
                    <motion.div
                        key={patient.id}
                        className="bg-white rounded-lg shadow-lg overflow-hidden transform hover:-translate-y-2 transition-transform duration-300"
                        variants={itemVariants}
                    >
                        <div className="p-6">
                            <h2 className="text-2xl font-bold text-gray-900 mb-2">{patient.name}</h2>
                            <div className="text-gray-600 space-y-2">
                                <p><span className="font-semibold">Email:</span> {patient.email}</p>
                                <p><span className="font-semibold">Phone:</span> {patient.phone}</p>
                                <p><span className="font-semibold">Address:</span> {patient.address}</p>
                                <p><span className="font-semibold">Date of Birth:</span> {patient.dateOfBirth ? new Date(patient.dateOfBirth).toLocaleDateString() : 'N/A'}</p>
                            </div>
                        </div>
                        <div className="bg-gray-50 px-6 py-4 flex justify-end">
                            <button className="text-sm font-medium text-indigo-600 hover:text-indigo-800 transition-colors">Edit</button>
                            <button className="text-sm font-medium text-red-600 hover:text-red-800 transition-colors ml-4">Delete</button>
                        </div>
                    </motion.div>
                ))}
            </motion.div>
        </motion.div>
    );
};

export default Patients;