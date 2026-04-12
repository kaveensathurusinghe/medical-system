import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import { motion } from 'framer-motion';

const Doctors = () => {
    const [doctors, setDoctors] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [categoryName, setCategoryName] = useState('');
    const [categoryMessage, setCategoryMessage] = useState('');
    const [categoryError, setCategoryError] = useState('');
    const [creatingCategory, setCreatingCategory] = useState(false);
    const [deletingCategoryId, setDeletingCategoryId] = useState(null);

    const formatCurrency = (amount) => new Intl.NumberFormat('en-LK', {
        style: 'currency',
        currency: 'LKR',
        minimumFractionDigits: 2,
    }).format(Number(amount) || 0);

    useEffect(() => {
        const fetchDoctorsAndCategories = async () => {
            try {
                const [doctorResponse, categoryResponse] = await Promise.all([
                    api.get('/admin/doctors'),
                    api.get('/admin/doctor-categories')
                ]);
                setDoctors(doctorResponse.data);
                setCategories(categoryResponse.data);
            } catch (err) {
                setError('Failed to fetch doctors. Please try again later.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchDoctorsAndCategories();
    }, []);

    const refreshCategories = async () => {
        const response = await api.get('/admin/doctor-categories');
        setCategories(response.data);
    };

    const handleAddCategory = async (e) => {
        e.preventDefault();
        const trimmedName = categoryName.trim();
        setCategoryMessage('');
        setCategoryError('');

        if (!trimmedName) {
            setCategoryError('Category name is required.');
            return;
        }

        try {
            setCreatingCategory(true);
            await api.post('/admin/doctor-categories', { name: trimmedName });
            setCategoryName('');
            setCategoryMessage('Category added successfully.');
            await refreshCategories();
        } catch (err) {
            const message = err?.response?.data?.content || 'Failed to add category.';
            setCategoryError(message);
        } finally {
            setCreatingCategory(false);
        }
    };

    const handleDeleteCategory = async (category) => {
        setCategoryMessage('');
        setCategoryError('');

        const confirmed = window.confirm(`Delete category \"${category.name}\"?`);
        if (!confirmed) {
            return;
        }

        try {
            setDeletingCategoryId(category.id);
            await api.delete(`/admin/doctor-categories/${category.id}`);
            setCategoryMessage('Category deleted successfully.');
            await refreshCategories();
        } catch (err) {
            const message = err?.response?.data?.content || 'Failed to delete category.';
            setCategoryError(message);
        } finally {
            setDeletingCategoryId(null);
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
            <h1 className="text-4xl font-bold mb-8 text-gray-800 border-b pb-4">Manage Doctors</h1>

            <motion.div
                className="grid grid-cols-1 gap-8 lg:grid-cols-3"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
            >
                <motion.section
                    className="rounded-xl bg-white p-6 shadow-lg"
                    variants={itemVariants}
                >
                    <h2 className="text-2xl font-bold text-gray-900">Doctor Categories</h2>
                    <p className="mt-2 text-sm text-gray-600">Admins can create and delete reusable doctor specializations.</p>

                    <form className="mt-5 flex gap-2" onSubmit={handleAddCategory}>
                        <input
                            type="text"
                            value={categoryName}
                            onChange={(e) => setCategoryName(e.target.value)}
                            placeholder="Add category"
                            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
                        />
                        <button
                            type="submit"
                            disabled={creatingCategory}
                            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            {creatingCategory ? 'Adding...' : 'Add'}
                        </button>
                    </form>

                    {categoryMessage && (
                        <p className="mt-3 rounded-md bg-green-50 px-3 py-2 text-sm text-green-700">{categoryMessage}</p>
                    )}
                    {categoryError && (
                        <p className="mt-3 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{categoryError}</p>
                    )}

                    <div className="mt-5 space-y-2">
                        {categories.length === 0 && (
                            <p className="rounded-md bg-gray-50 px-3 py-2 text-sm text-gray-600">No categories added yet.</p>
                        )}
                        {categories.map((category) => (
                            <div key={category.id} className="flex items-center justify-between rounded-md border border-gray-200 px-3 py-2">
                                <p className="text-sm font-medium text-gray-800">{category.name}</p>
                                <button
                                    type="button"
                                    onClick={() => handleDeleteCategory(category)}
                                    disabled={deletingCategoryId === category.id}
                                    className="text-sm font-semibold text-red-600 transition hover:text-red-800 disabled:cursor-not-allowed disabled:opacity-60"
                                >
                                    {deletingCategoryId === category.id ? 'Deleting...' : 'Delete'}
                                </button>
                            </div>
                        ))}
                    </div>
                </motion.section>

                <div className="lg:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-8">
                    {doctors.map(doctor => (
                        <motion.div
                            key={doctor.id}
                            className="bg-white rounded-lg shadow-lg overflow-hidden transform hover:-translate-y-2 transition-transform duration-300"
                            variants={itemVariants}
                        >
                            <div className="p-6">
                                <h2 className="text-2xl font-bold text-gray-900 mb-2">{doctor.name}</h2>
                                <p className="text-md text-indigo-600 font-semibold mb-4">{doctor.specialization || 'Unassigned'}</p>
                                <div className="text-gray-600 space-y-2">
                                    <p><span className="font-semibold">Email:</span> {doctor.email}</p>
                                    <p><span className="font-semibold">Phone:</span> {doctor.phone}</p>
                                    <p>
                                        <span className="font-semibold">Consultation Fee:</span>{' '}
                                        {doctor.consultationFee != null ? formatCurrency(doctor.consultationFee) : 'Not set'}
                                    </p>
                                </div>
                            </div>
                            <div className="bg-gray-50 px-6 py-4 flex justify-end">
                                <button className="text-sm font-medium text-indigo-600 hover:text-indigo-800 transition-colors">Edit</button>
                                <button className="text-sm font-medium text-red-600 hover:text-red-800 transition-colors ml-4">Delete</button>
                            </div>
                        </motion.div>
                    ))}
                </div>
            </motion.div>
        </motion.div>
    );
};

export default Doctors;