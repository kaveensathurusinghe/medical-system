import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Calendar, Clock, Trash2 } from 'lucide-react';
import api from '../../services/api';
import { resolveUserId } from '../../utils/sessionUser';

const DoctorView = () => {
    const [doctorId, setDoctorId] = useState('');
    const [timeslots, setTimeslots] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [form, setForm] = useState({
        date: '',
        startTime: '',
        endTime: '',
        slotDurationMinutes: 30,
    });

    const fetchTimeslots = async (currentDoctorId) => {
        if (!currentDoctorId) {
            setTimeslots([]);
            return;
        }

        try {
            const response = await api.get(`/timeslots/doctor/${currentDoctorId}`);
            setTimeslots(Array.isArray(response.data) ? response.data : []);
            setError('');
        } catch (fetchError) {
            setError('Error fetching timeslots.');
            console.error('Error fetching timeslots:', fetchError);
        }
    };

    useEffect(() => {
        const bootstrap = async () => {
            try {
                const resolvedDoctorId = String(await resolveUserId('ROLE_DOCTOR'));

                setDoctorId(resolvedDoctorId);
                await fetchTimeslots(resolvedDoctorId);
                setLoading(false);
            } catch (bootstrapError) {
                setError('Failed to initialize doctor timeslot management.');
                console.error('Failed to initialize timeslots view:', bootstrapError);
                setLoading(false);
            }
        };

        bootstrap();
    }, []);

    const buildDateTime = (date, time) => {
        return `${date}T${time}:00`;
    };

    const handleGenerateSlots = async (e) => {
        e.preventDefault();
        setMessage('');
        setError('');

        if (!doctorId) {
            setError('Doctor ID is missing. Please log in again.');
            return;
        }

        const { date, startTime, endTime, slotDurationMinutes } = form;
        if (!date || !startTime || !endTime) {
            setError('Please select date, start time, and end time.');
            return;
        }

        const startDateTime = buildDateTime(date, startTime);
        const endDateTime = buildDateTime(date, endTime);

        if (new Date(startDateTime) >= new Date(endDateTime)) {
            setError('End time must be later than start time.');
            return;
        }

        try {
            setSubmitting(true);
            const response = await api.post(`/timeslots/doctor/${doctorId}/generate`, {
                startDateTime,
                endDateTime,
                slotDurationMinutes: Number(slotDurationMinutes),
            });

            const generatedCount = Array.isArray(response.data) ? response.data.length : 0;
            setMessage(`Generated ${generatedCount} slot(s) successfully.`);
            await fetchTimeslots(doctorId);
        } catch (generateError) {
            const serverMessage = generateError?.response?.data?.content || generateError?.response?.data?.message;
            setError(serverMessage || 'Failed to generate slots.');
            console.error('Error generating slots:', generateError);
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeleteSlot = async (slotId) => {
        try {
            await api.delete(`/timeslots/${slotId}`);
            setMessage('Time slot deleted.');
            setError('');
            await fetchTimeslots(doctorId);
        } catch (deleteError) {
            const serverMessage = deleteError?.response?.data?.content || deleteError?.response?.data?.message;
            setError(serverMessage || 'Failed to delete selected slot.');
            console.error('Error deleting slot:', deleteError);
        }
    };

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

    const getSlotTheme = (isAvailable) => {
        if (isAvailable) {
            return {
                card: 'border-cyan-200 bg-gradient-to-br from-cyan-50 via-white to-sky-50 text-slate-800',
                icon: 'text-cyan-700',
                date: 'text-slate-600',
                status: 'bg-cyan-100 text-cyan-800',
            };
        }

        return {
            card: 'border-slate-200 bg-gradient-to-br from-slate-100 via-white to-slate-50 text-slate-700',
            icon: 'text-slate-500',
            date: 'text-slate-500',
            status: 'bg-slate-200 text-slate-700',
        };
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

            <div className="bg-white rounded-lg shadow-lg p-6 mb-8">
                <h2 className="text-2xl font-semibold text-gray-800 mb-4">Set Availability</h2>
                <p className="text-sm text-gray-600 mb-4">Pick a time range and slot duration. The system will split it into equal appointment slots.</p>

                {message && <p className="mb-3 rounded-lg bg-cyan-50 px-3 py-2 text-sm text-cyan-700">{message}</p>}
                {error && <p className="mb-3 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">{error}</p>}

                <form onSubmit={handleGenerateSlots} className="grid grid-cols-1 md:grid-cols-4 gap-4">
                    <div>
                        <label htmlFor="date" className="block text-sm font-medium text-gray-700 mb-1">Date</label>
                        <input
                            id="date"
                            type="date"
                            value={form.date}
                            onChange={(e) => setForm((prev) => ({ ...prev, date: e.target.value }))}
                            className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="startTime" className="block text-sm font-medium text-gray-700 mb-1">Start Time</label>
                        <input
                            id="startTime"
                            type="time"
                            value={form.startTime}
                            onChange={(e) => setForm((prev) => ({ ...prev, startTime: e.target.value }))}
                            className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="endTime" className="block text-sm font-medium text-gray-700 mb-1">End Time</label>
                        <input
                            id="endTime"
                            type="time"
                            value={form.endTime}
                            onChange={(e) => setForm((prev) => ({ ...prev, endTime: e.target.value }))}
                            className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="duration" className="block text-sm font-medium text-gray-700 mb-1">Slot Duration (min)</label>
                        <select
                            id="duration"
                            value={form.slotDurationMinutes}
                            onChange={(e) => setForm((prev) => ({ ...prev, slotDurationMinutes: e.target.value }))}
                            className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                        >
                            <option value={15}>15</option>
                            <option value={20}>20</option>
                            <option value={30}>30</option>
                            <option value={45}>45</option>
                            <option value={60}>60</option>
                        </select>
                    </div>

                    <div className="md:col-span-4">
                        <button
                            type="submit"
                            className="rounded-md bg-indigo-600 px-4 py-2 text-white font-semibold hover:bg-indigo-700 disabled:bg-gray-400"
                            disabled={submitting}
                        >
                            {submitting ? 'Generating...' : 'Generate Equal Time Slots'}
                        </button>
                    </div>
                </form>
            </div>

            <motion.div
                className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                {timeslots.map((slot) => {
                    const theme = getSlotTheme(slot.available);

                    return (
                        <motion.div
                            key={slot.id}
                            className={`rounded-xl border p-6 shadow-sm ${theme.card}`}
                            variants={itemVariants}
                        >
                            <div className="flex items-center mb-4">
                                <Clock className={`w-6 h-6 mr-3 ${theme.icon}`} />
                                <h3 className="text-2xl font-bold">{new Date(slot.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - {new Date(slot.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</h3>
                            </div>
                            <p className={`mb-3 text-sm ${theme.date}`}>{new Date(slot.startTime).toDateString()}</p>
                            <span className={`inline-flex rounded-full px-3 py-1 text-sm font-semibold ${theme.status}`}>
                                {slot.available ? 'Available' : 'Booked'}
                            </span>
                            {slot.available && (
                                <button
                                    type="button"
                                    onClick={() => handleDeleteSlot(slot.id)}
                                    className="mt-4 inline-flex items-center gap-2 rounded-md bg-cyan-700 px-3 py-2 text-sm font-semibold text-white transition hover:bg-cyan-800"
                                >
                                    <Trash2 size={16} />
                                    Delete
                                </button>
                            )}
                        </motion.div>
                    );
                })}
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