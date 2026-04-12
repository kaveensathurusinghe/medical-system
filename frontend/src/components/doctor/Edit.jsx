import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Edit } from 'lucide-react';
import api from '../../services/api';
import { resolveUserId } from '../../utils/sessionUser';

const EditDoctorProfile = () => {
    const [formData, setFormData] = useState({
        name: '',
        specialization: '',
        email: '',
        phone: '',
        consultationFee: ''
    });
    const [categories, setCategories] = useState([]);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(true);
    const [doctorId, setDoctorId] = useState(null);

    useEffect(() => {
        const fetchDoctor = async () => {
            try {
                const resolvedDoctorId = await resolveUserId('ROLE_DOCTOR');
                setDoctorId(resolvedDoctorId);

                const [doctorResponse, categoryResponse] = await Promise.all([
                    api.get(`/doctors/${resolvedDoctorId}`),
                    api.get('/doctor-categories')
                ]);
                const doctorData = doctorResponse.data || {};
                setFormData({
                    name: doctorData.name || '',
                    specialization: doctorData.specialization || '',
                    email: doctorData.email || '',
                    phone: doctorData.phone || '',
                    consultationFee: doctorData.consultationFee ?? '',
                });
                setCategories(categoryResponse.data);
            } catch (error) {
                console.error("Error fetching doctor details:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchDoctor();
    }, []);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!doctorId) {
            setMessage('Unable to identify doctor account. Please login again.');
            return;
        }

        const consultationFee = formData.consultationFee === '' ? null : Number(formData.consultationFee);
        if (consultationFee != null && consultationFee <= 0) {
            setMessage('Consultation fee must be greater than zero.');
            return;
        }

        try {
            await api.put(`/doctors/${doctorId}`, {
                ...formData,
                consultationFee,
            });
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
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="specialization">
                        Specialization
                    </label>
                    <input
                        className="shadow appearance-none border rounded w-full py-3 px-4 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        id="specialization"
                        type="text"
                        list="doctor-category-options"
                        name="specialization"
                        value={formData.specialization}
                        onChange={handleChange}
                        required
                    />
                    <datalist id="doctor-category-options">
                        {categories.map((category) => (
                            <option key={category.id} value={category.name} />
                        ))}
                    </datalist>
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
                    <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="consultationFee">
                        Consultation Fee (LKR)
                    </label>
                    <input
                        className="shadow appearance-none border rounded w-full py-3 px-4 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                        id="consultationFee"
                        type="number"
                        min="1"
                        step="0.01"
                        name="consultationFee"
                        value={formData.consultationFee}
                        onChange={handleChange}
                        placeholder="e.g. 2500"
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

export default EditDoctorProfile;