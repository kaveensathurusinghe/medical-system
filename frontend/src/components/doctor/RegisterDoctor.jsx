import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { UserPlus } from 'lucide-react';
import api from '../../services/api';

const RegisterDoctor = () => {
    const [formData, setFormData] = useState({
        name: '',
        specialization: '',
        email: '',
        phone: ''
    });
    const [categories, setCategories] = useState([]);
    const [message, setMessage] = useState('');

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const response = await api.get('/doctor-categories');
                setCategories(response.data);
            } catch (error) {
                console.error('Error fetching doctor categories:', error);
            }
        };

        fetchCategories();
    }, []);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/doctors/register', formData);
            setMessage('Doctor registered successfully!');
            setFormData({ name: '', specialization: '', email: '', phone: '' });
        } catch (error) {
            console.error("Error registering doctor:", error);
            setMessage('Error registering doctor. Please try again.');
        }
    };

    const formVariants = {
        hidden: { opacity: 0, y: 50 },
        visible: { opacity: 1, y: 0, transition: { duration: 0.5 } }
    };

    return (
        <motion.div
            className="container mx-auto p-8 bg-gray-50 min-h-screen"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5 }}
        >
            <div className="flex items-center mb-8">
                <UserPlus className="w-10 h-10 mr-4 text-blue-600" />
                <h1 className="text-4xl font-bold text-gray-800">Register New Doctor</h1>
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
                    {categories.length > 0 && (
                        <p className="mt-2 text-xs text-gray-500">Use one of the admin-defined categories, or type a custom specialization.</p>
                    )}
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
                <div className="flex items-center justify-center">
                    <motion.button
                        className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-full focus:outline-none focus:shadow-outline"
                        type="submit"
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                    >
                        Register Doctor
                    </motion.button>
                </div>
            </motion.form>
        </motion.div>
    );
};

export default RegisterDoctor;