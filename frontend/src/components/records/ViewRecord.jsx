import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { FileText, User, Stethoscope, Calendar, Pill } from 'lucide-react';

const ViewRecord = () => {
    const [record, setRecord] = useState(null);
    const [loading, setLoading] = useState(true);
    const { recordId } = useParams();

    useEffect(() => {
        const fetchRecord = async () => {
            try {
                const response = await axios.get(`/api/records/${recordId}`);
                setRecord(response.data);
                setLoading(false);
            } catch (error) {
                console.error("Error fetching medical record:", error);
                setLoading(false);
            }
        };

        fetchRecord();
    }, [recordId]);

    const cardVariants = {
        hidden: { y: 20, opacity: 0 },
        visible: { y: 0, opacity: 1, transition: { duration: 0.5 } }
    };

    if (loading) {
        return <div className="flex justify-center items-center h-screen">
            <div className="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-blue-500"></div>
        </div>;
    }

    if (!record) {
        return <div className="text-center py-12 text-gray-500">
            <p>Medical record not found.</p>
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
                <FileText className="w-10 h-10 mr-4 text-blue-600" />
                <h1 className="text-4xl font-bold text-gray-800">Medical Record Details</h1>
            </div>

            <motion.div
                className="bg-white shadow-2xl rounded-lg p-8"
                variants={cardVariants}
                initial="hidden"
                animate="visible"
            >
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div className="flex items-center">
                        <User className="w-6 h-6 mr-3 text-blue-500" />
                        <p><span className="font-semibold">Patient ID:</span> {record.patientId}</p>
                    </div>
                    <div className="flex items-center">
                        <Stethoscope className="w-6 h-6 mr-3 text-blue-500" />
                        <p><span className="font-semibold">Doctor ID:</span> {record.doctorId}</p>
                    </div>
                    <div className="flex items-center">
                        <Calendar className="w-6 h-6 mr-3 text-blue-500" />
                        <p><span className="font-semibold">Date:</span> {new Date(record.recordDate).toLocaleString()}</p>
                    </div>
                    <div className="flex items-center col-span-1 md:col-span-2">
                        <Pill className="w-6 h-6 mr-3 text-blue-500" />
                        <p><span className="font-semibold">Appointment ID:</span> {record.appointmentId}</p>
                    </div>
                    <div className="col-span-1 md:col-span-2">
                        <h3 className="text-xl font-semibold mb-2 text-gray-700">Diagnosis</h3>
                        <p className="bg-gray-100 p-4 rounded-md">{record.diagnosis}</p>
                    </div>
                    <div className="col-span-1 md:col-span-2">
                        <h3 className="text-xl font-semibold mb-2 text-gray-700">Treatment</h3>
                        <p className="bg-gray-100 p-4 rounded-md">{record.treatment}</p>
                    </div>
                    <div className="col-span-1 md:col-span-2">
                        <h3 className="text-xl font-semibold mb-2 text-gray-700">Notes</h3>
                        <p className="bg-gray-100 p-4 rounded-md">{record.notes}</p>
                    </div>
                </div>
            </motion.div>
        </motion.div>
    );
};

export default ViewRecord;