import React, { useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';
import { motion } from 'framer-motion';

const Form = () => {
    const [diagnosis, setDiagnosis] = useState('');
    const [treatment, setTreatment] = useState('');
    const [notes, setNotes] = useState('');
    const [error, setError] = useState(null);
    const location = useLocation();
    const navigate = useNavigate();
    const { appointmentId: appointmentIdFromPath } = useParams();
    const appointmentId = appointmentIdFromPath || new URLSearchParams(location.search).get('appointmentId');

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/records', {
                appointmentId,
                diagnosis,
                treatment,
                notes,
            });
            navigate(`/doctor/appointments`);
        } catch (err) {
            setError('Failed to create medical record.');
            console.error(err);
        }
    };

    return (
        <motion.div 
            className="container mx-auto p-8 max-w-3xl"
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
        >
            <h1 className="text-4xl font-bold mb-8 text-gray-800">Create Medical Record</h1>

            <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg shadow-lg space-y-6">
                <div>
                    <label htmlFor="diagnosis" className="block text-lg font-medium text-gray-700">Diagnosis</label>
                    <textarea
                        id="diagnosis"
                        value={diagnosis}
                        onChange={(e) => setDiagnosis(e.target.value)}
                        rows="3"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                        placeholder="Enter diagnosis..."
                        required
                    ></textarea>
                </div>

                <div>
                    <label htmlFor="treatment" className="block text-lg font-medium text-gray-700">Treatment</label>
                    <textarea
                        id="treatment"
                        value={treatment}
                        onChange={(e) => setTreatment(e.target.value)}
                        rows="5"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                        placeholder="Describe the treatment plan..."
                        required
                    ></textarea>
                </div>

                <div>
                    <label htmlFor="notes" className="block text-lg font-medium text-gray-700">Additional Notes</label>
                    <textarea
                        id="notes"
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        rows="3"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                        placeholder="Any other notes..."
                    ></textarea>
                </div>

                {error && <p className="text-red-500">{error}</p>}

                <motion.button
                    type="submit"
                    className="w-full bg-indigo-600 text-white py-3 px-4 rounded-md font-semibold hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                >
                    Create Record
                </motion.button>
            </form>
        </motion.div>
    );
};

export default Form;