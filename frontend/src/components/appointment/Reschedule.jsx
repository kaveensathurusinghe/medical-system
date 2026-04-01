import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';
import { motion } from 'framer-motion';

const Reschedule = () => {
    const [timeSlots, setTimeSlots] = useState([]);
    const [selectedSlot, setSelectedSlot] = useState('');
    const [appointment, setAppointment] = useState(null);
    const [doctor, setDoctor] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { id } = useParams();
    const navigate = useNavigate();

    useEffect(() => {
        const fetchData = async () => {
            try {
                const appointmentRes = await api.get(`/appointments/${id}`);
                setAppointment(appointmentRes.data);

                const doctorId = appointmentRes.data.doctorId;
                const doctorPromise = api.get(`/doctors/${doctorId}`);
                const slotsPromise = api.get(`/timeslots/doctor/${doctorId}/available`);
                
                const [doctorRes, slotsRes] = await Promise.all([doctorPromise, slotsPromise]);

                setDoctor(doctorRes.data);
                setTimeSlots(slotsRes.data);
                setLoading(false);
            } catch (err) {
                setError('Failed to fetch appointment details for rescheduling.');
                setLoading(false);
                console.error(err);
            }
        };

        fetchData();
    }, [id]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedSlot) {
            setError("Please select a new time slot.");
            return;
        }
        try {
            const selectedSlotDetails = timeSlots.find(slot => slot.id === selectedSlot);
            await api.put(`/appointments/reschedule`, {
                id: appointment.appointmentId,
                newAppointmentTime: selectedSlotDetails.startTime,
            });
            navigate('/patient/history');
        } catch (err) {
            setError('Failed to reschedule appointment. The slot might have just been taken.');
            console.error(err);
        }
    };

    if (loading) return <div className="flex justify-center items-center h-screen"><p>Loading...</p></div>;
    if (error) return <div className="flex justify-center items-center h-screen"><p className="text-red-500">{error}</p></div>;
    if (!appointment || !doctor) return <div className="flex justify-center items-center h-screen"><p>Appointment or Doctor not found.</p></div>;

    return (
        <motion.div 
            className="container mx-auto p-8 max-w-3xl"
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
        >
            <h1 className="text-4xl font-bold mb-4 text-gray-800">Reschedule Appointment</h1>
            <h2 className="text-2xl font-semibold mb-2 text-indigo-600">with Dr. {doctor.name}</h2>
            <p className="text-gray-600 mb-8">
                Current Appointment: {new Date(appointment.appointmentTime).toLocaleString()}
            </p>

            <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg shadow-lg space-y-6">
                <div>
                    <label htmlFor="time-slot" className="block text-lg font-medium text-gray-700 mb-2">Select a New Time Slot</label>
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {timeSlots.map(slot => (
                            <motion.div
                                key={slot.id}
                                whileHover={{ scale: 1.05 }}
                            >
                                <input 
                                    type="radio" 
                                    id={`slot-${slot.id}`} 
                                    name="time-slot" 
                                    value={slot.id}
                                    checked={selectedSlot === slot.id}
                                    onChange={(e) => setSelectedSlot(e.target.value)}
                                    className="hidden"
                                />
                                <label 
                                    htmlFor={`slot-${slot.id}`} 
                                    className={`block text-center p-4 rounded-lg cursor-pointer transition-colors ${
                                        selectedSlot === slot.id 
                                            ? 'bg-indigo-600 text-white shadow-md' 
                                            : 'bg-gray-100 hover:bg-indigo-100'
                                    }`}
                                >
                                    {new Date(slot.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </label>
                            </motion.div>
                        ))}
                    </div>
                    {timeSlots.length === 0 && <p className="text-gray-500 mt-4">No available slots for this doctor.</p>}
                </div>

                <motion.button
                    type="submit"
                    className="w-full bg-indigo-600 text-white py-3 px-4 rounded-md font-semibold hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400"
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    disabled={!selectedSlot}
                >
                    Reschedule Appointment
                </motion.button>
            </form>
        </motion.div>
    );
};

export default Reschedule;