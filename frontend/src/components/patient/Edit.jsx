import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Edit } from 'lucide-react';
import api from '../../services/api';
import { resolveUserId } from '../../utils/sessionUser';

const EditPatientProfile = () => {
    const [formData, setFormData] = useState({
        name: '',
        dateOfBirth: '',
        gender: '',
        address: '',
        phone: '',
        email: ''
    });
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(true);
    const [patientId, setPatientId] = useState(null);

    useEffect(() => {
        const fetchPatient = async () => {
            try {
                const resolvedPatientId = await resolveUserId('ROLE_PATIENT');
                setPatientId(resolvedPatientId);

                const response = await api.get(`/patients/${resolvedPatientId}`);
                setFormData(response.data);
                setLoading(false);
            } catch (error) {
                console.error("Error fetching patient details:", error);
                setLoading(false);
            }
        };

        fetchPatient();
    }, []);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!patientId) {
            setMessage('Unable to identify patient account. Please login again.');
            return;
        }

        try {
            await api.put(`/patients/${patientId}`, formData);
            setMessage('Profile updated successfully!');
        } catch (error) {
            console.error("Error updating profile:", error);
            setMessage('Error updating profile. Please try again.');
        }
    };

    const formVariants = {
        hidden: { opacity: 0, y: 50 },
        visible: { opacity: 1, y: 0, transition: { duration: 0.5 } }
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
                <Edit className="w-10 h-10 mr-4 text-blue-600" />
                <h1 className="text-4xl font-bold text-gray-800">Edit My Profile</h1>
            </div>

            <motion.form
                className="bg-white shadow-2xl rounded-lg p-8 max-w-lg mx-auto"
                onSubmit={handleSubmit}
                variants={formVariants}
                initial="hidden"
                animate="visible"
            >
                {message && <p className="mb-4 text-center text-green-500">{message}</p>}
                <div className="mb-6">
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="name">
                        Full Name
                    </label>
                    <input
                        className="shadow appearance-none border rounded w-full py-3 px-4 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        id="name"
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="mb-6">
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="dateOfBirth">
                        Date of Birth
                    </label>
                    <input
                        className="shadow appearance-none border rounded w-full py-3 px-4 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        id="dateOfBirth"
                        type="date"
                        name="dateOfBirth"
                        value={formData.dateOfBirth}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="mb-6">
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="gender">
                        Gender
                    </label>
                    <select
                        className="shadow appearance-none border rounded w-full py-3 px-4 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        id="gender"
                        name="gender"
                        value={formData.gender}
                        onChange={handleChange}
                        required
                    >
                        <option value="">Select Gender</option>
                        <option value="Male">Male</option>
                        <option value="Female">Female</option>
                        <option value="Other">Other</option>
                    </select>
                </div>
                <div className="mb-6">
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="address">
                        Address
                    </label>
                    <input
                        className="shadow appearance-none border rounded w-full py-3 px-4 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        id="address"
                        type="text"
                        name="address"
                        value={formData.address}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="mb-6">
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="phone">
                        Phone
                    </label>
                    <input
                        className="shadow appearance-none border rounded w-full py-3 px-4 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        id="phone"
                        type="text"
                        name="phone"
                        value={formData.phone}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="mb-6">
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="email">
                        Email
                    </label>
                    <input
                        className="shadow appearance-none border rounded w-full py-3 px-4 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        id="email"
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className="flex items-center justify-center">
                    <motion.button
                        className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-full focus:outline-none focus:shadow-outline"
                        type="submit"
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                    >
                        Update Profile
                    </motion.button>
                </div>
            </motion.form>
        </motion.div>
    );
};

export default EditPatientProfile;