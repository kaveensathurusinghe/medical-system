import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { motion } from 'framer-motion';
import { Calendar, Clock } from 'lucide-react';

const DoctorView = () => {
    const [timeslots, setTimeslots] = useState([]);
    const [loading, setLoading] = useState(true);
    // Hardcoded doctor ID for now
    const doctorId = 1;

    useEffect(() => {
        const fetchTimeslots = async () => {
            try {
                const response = await axios.get(`/api/timeslots/doctor/${doctorId}`);
                setTimeslots(response.data);
                setLoading(false);
            } catch (error) {
                console.error("Error fetching timeslots:", error);
                setLoading(false);
            }
        };

        fetchTimeslots();
    }, [doctorId]);

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
                <Calendar className="w-10 h-10 mr-4 text-blue-600" />
                <h1 className="text-4xl font-bold text-gray-800">My Timeslots</h1>
            </div>

            <motion.div
                className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                {timeslots.map(slot => (
                    <motion.div
                        key={slot.id}
                        className={`p-6 rounded-lg shadow-lg text-white ${slot.available ? 'bg-green-500' : 'bg-red-500'}`}
                        variants={itemVariants}
                    >
                        <div className="flex items-center mb-4">
                            <Clock className="w-6 h-6 mr-3" />
                            <h3 className="text-2xl font-bold">{new Date(slot.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - {new Date(slot.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</h3>
                        </div>
                        <p className="text-lg mb-2">{new Date(slot.startTime).toDateString()}</p>
                        <p className="font-semibold text-xl">{slot.available ? 'Available' : 'Booked'}</p>
                    </motion.div>
                ))}
            </motion.div>
            {timeslots.length === 0 && !loading && (
                <div className="text-center py-12 text-gray-500">
                    <p>No timeslots found.</p>
                </div>
            )}
        </motion.div>
    );
};

export default DoctorView;