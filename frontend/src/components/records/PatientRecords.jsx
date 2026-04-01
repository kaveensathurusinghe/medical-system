import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useParams, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { FileText, User } from 'lucide-react';

const PatientRecords = () => {
    const [records, setRecords] = useState([]);
    const [loading, setLoading] = useState(true);
    const { patientId } = useParams(); // Assuming patient ID is in the URL

    useEffect(() => {
        const fetchRecords = async () => {
            try {
                // Hardcoded patient ID for now, will be replaced with dynamic ID
                const response = await axios.get(`/api/records/patient/1`);
                setRecords(response.data);
                setLoading(false);
            } catch (error) {
                console.error("Error fetching patient records:", error);
                setLoading(false);
            }
        };

        fetchRecords();
    }, [patientId]);

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
        visible: { y: 0, opacity: 1 }
    };

    if (loading) {
        return <div className="flex justify-center items-center h-screen">
            <div className="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-blue-500"></div>
        </div>;
    }

    return (
        <motion.div
            className="container mx-auto p-8 bg-gray-50 min-h-screen"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5 }}
        >
            <div className="flex items-center mb-8">
                <User className="w-10 h-10 mr-4 text-blue-600" />
                <h1 className="text-4xl font-bold text-gray-800">Patient Medical Records</h1>
            </div>

            <motion.div
                className="bg-white shadow-2xl rounded-lg overflow-hidden"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                <div className="overflow-x-auto">
                    <table className="min-w-full bg-white">
                        <thead className="bg-blue-600 text-white">
                            <tr>
                                <th className="text-left py-4 px-6">Record ID</th>
                                <th className="text-left py-4 px-6">Doctor ID</th>
                                <th className="text-left py-4 px-6">Date</th>
                                <th className="text-left py-4 px-6">Diagnosis</th>
                                <th className="text-left py-4 px-6">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="text-gray-700">
                            {records.map((record, index) => (
                                <motion.tr
                                    key={record.id}
                                    className={`border-b border-gray-200 ${index % 2 === 0 ? 'bg-gray-50' : 'bg-white'} hover:bg-blue-100 transition-colors duration-200`}
                                    variants={itemVariants}
                                >
                                    <td className="py-4 px-6">{record.id}</td>
                                    <td className="py-4 px-6">{record.doctorId}</td>
                                    <td className="py-4 px-6">{new Date(record.recordDate).toLocaleDateString()}</td>
                                    <td className="py-4 px-6">{record.diagnosis}</td>
                                    <td className="py-4 px-6">
                                        <Link to={`/records/view/${record.id}`} className="text-blue-600 hover:underline">
                                            View Details
                                        </Link>
                                    </td>
                                </motion.tr>
                            ))}
                        </tbody>
                    </table>
                </div>
                {records.length === 0 && !loading && (
                    <div className="text-center py-12 text-gray-500">
                        <p>No medical records found for this patient.</p>
                    </div>
                )}
            </motion.div>
        </motion.div>
    );
};

export default PatientRecords;