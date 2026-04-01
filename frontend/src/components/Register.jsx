import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import api from '../services/api';

const initialState = {
  name: '',
  email: '',
  password: '',
  phone: '',
  age: '',
  gender: '',
  specialization: '',
};

const Register = () => {
  const navigate = useNavigate();
  const [userType, setUserType] = useState('patient');
  const [formData, setFormData] = useState(initialState);
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const endpoint = useMemo(
    () => (userType === 'patient' ? '/auth/register/patient' : '/auth/register/doctor'),
    [userType],
  );

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await api.get('/doctor-categories');
        setCategories(response.data);
      } catch (err) {
        console.error('Failed to load doctor categories', err);
      }
    };

    fetchCategories();
  }, []);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const payload = {
        name: formData.name,
        email: formData.email,
        password: formData.password,
        phone: formData.phone,
      };

      if (userType === 'patient') {
        payload.age = Number(formData.age);
        payload.gender = formData.gender;
      } else {
        payload.specialization = formData.specialization;
      }

      await api.post(endpoint, payload);
      navigate('/login');
    } catch (err) {
      const message = err?.response?.data?.content || err?.response?.data?.message || 'Registration failed';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="hero-grid flex min-h-screen items-center justify-center px-4 py-10 sm:px-6 lg:px-8">
      <motion.div
        className="glass-card w-full max-w-xl rounded-3xl p-8 sm:p-10"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45 }}
      >
        <h1 className="text-3xl font-bold text-slate-900">Create Account</h1>
        <p className="mt-2 text-sm text-slate-600">Register as a patient or doctor</p>

        <div className="mt-6 grid grid-cols-2 gap-2 rounded-xl bg-slate-100 p-1">
          <button
            type="button"
            onClick={() => setUserType('patient')}
            className={`rounded-lg px-3 py-2 text-sm font-semibold transition ${
              userType === 'patient' ? 'bg-white text-slate-900 shadow' : 'text-slate-600'
            }`}
          >
            Patient
          </button>
          <button
            type="button"
            onClick={() => setUserType('doctor')}
            className={`rounded-lg px-3 py-2 text-sm font-semibold transition ${
              userType === 'doctor' ? 'bg-white text-slate-900 shadow' : 'text-slate-600'
            }`}
          >
            Doctor
          </button>
        </div>

        {error && <p className="mt-4 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

        <form onSubmit={handleSubmit} className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <InputField label="Full Name" name="name" value={formData.name} onChange={handleChange} required />
          <InputField label="Email" type="email" name="email" value={formData.email} onChange={handleChange} required />
          <InputField label="Password" type="password" name="password" value={formData.password} onChange={handleChange} required />
          <InputField label="Phone" name="phone" value={formData.phone} onChange={handleChange} required />

          {userType === 'patient' && (
            <>
              <InputField label="Age" type="number" name="age" value={formData.age} onChange={handleChange} required />
              <div>
                <label className="mb-1 block text-sm font-semibold text-slate-700" htmlFor="gender">
                  Gender
                </label>
                <select
                  id="gender"
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  className="w-full rounded-xl border border-slate-200 px-3 py-2.5 outline-none ring-cyan-200 transition focus:ring"
                  required
                >
                  <option value="">Select gender</option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other</option>
                </select>
              </div>
            </>
          )}

          {userType === 'doctor' && (
            <div className="sm:col-span-2">
                <label className="mb-1 block text-sm font-semibold text-slate-700" htmlFor="specialization">
                  Specialization
                </label>
                <select
                  id="specialization"
                  name="specialization"
                  value={formData.specialization}
                  onChange={handleChange}
                  className="w-full rounded-xl border border-slate-200 px-3 py-2.5 outline-none ring-cyan-200 transition focus:ring"
                  required
                  disabled={categories.length === 0}
                >
                  <option value="">Select specialization</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.name}>
                      {category.name}
                    </option>
                  ))}
                </select>
                {categories.length === 0 && (
                  <p className="mt-2 text-xs text-amber-700">
                    No specializations are available yet. Please ask admin to add categories.
                  </p>
                )}
            </div>
          )}

          <motion.button
            type="submit"
            className="sm:col-span-2 mt-2 w-full rounded-xl bg-cyan-700 px-4 py-3 font-semibold text-white transition hover:bg-cyan-800 disabled:opacity-60"
            whileHover={{ scale: 1.01 }}
            whileTap={{ scale: 0.99 }}
              disabled={loading || (userType === 'doctor' && categories.length === 0)}
          >
            {loading ? 'Registering...' : 'Register'}
          </motion.button>
        </form>

        <p className="mt-6 text-sm text-slate-600">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-cyan-800 hover:underline">
            Login
          </Link>
        </p>
      </motion.div>
    </div>
  );
};

const InputField = ({ label, type = 'text', ...props }) => (
  <div>
    <label className="mb-1 block text-sm font-semibold text-slate-700" htmlFor={props.name}>
      {label}
    </label>
    <input
      id={props.name}
      type={type}
      className="w-full rounded-xl border border-slate-200 px-3 py-2.5 outline-none ring-cyan-200 transition focus:ring"
      {...props}
    />
  </div>
);

export default Register;
