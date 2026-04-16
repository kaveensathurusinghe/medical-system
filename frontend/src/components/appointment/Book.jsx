import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { motion } from 'framer-motion';
import { resolveUserId } from '../../utils/sessionUser';

const Book = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const initialDoctorId = new URLSearchParams(location.search).get('doctorId') || '';

    const [doctors, setDoctors] = useState([]);
    const [selectedDoctorId, setSelectedDoctorId] = useState(initialDoctorId);
    const [timeSlots, setTimeSlots] = useState([]);
    const [selectedSlot, setSelectedSlot] = useState('');
    const [reason, setReason] = useState('');
    const [urgency, setUrgency] = useState(3);
    const [appointmentFee, setAppointmentFee] = useState(null);
    const [feeSource, setFeeSource] = useState('default');
    const [paymentMethod, setPaymentMethod] = useState('CARD');
    const [cardNumber, setCardNumber] = useState('');
    const [cardHolderName, setCardHolderName] = useState('');
    const [expiryDate, setExpiryDate] = useState('');
    const [cvv, setCvv] = useState('');
    const [doctor, setDoctor] = useState(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const currentMonthValue = (() => {
        const now = new Date();
        return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    })();

    const formatCurrency = (amount) => new Intl.NumberFormat('en-LK', {
        style: 'currency',
        currency: 'LKR',
        minimumFractionDigits: 2,
    }).format(Number(amount) || 0);

    const sanitizeCardNumber = (value) => value.replace(/\D/g, '');

    const formatCardNumberForInput = (value) => (
        sanitizeCardNumber(value)
            .slice(0, 19)
            .replace(/(.{4})/g, '$1 ')
            .trim()
    );

    const isValidLuhnCardNumber = (digitsOnlyValue) => {
        let sum = 0;
        let shouldDouble = false;

        for (let i = digitsOnlyValue.length - 1; i >= 0; i -= 1) {
            let digit = Number(digitsOnlyValue[i]);
            if (shouldDouble) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            shouldDouble = !shouldDouble;
        }

        return sum % 10 === 0;
    };

    const isValidExpiryDate = (value) => {
        if (!/^\d{4}-\d{2}$/.test(value)) {
            return false;
        }

        const [year, month] = value.split('-').map(Number);
        if (!year || !month || month < 1 || month > 12) {
            return false;
        }

        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;

        return year > currentYear || (year === currentYear && month >= currentMonth);
    };

    useEffect(() => {
        const fetchDoctors = async () => {
            try {
                const response = await api.get('/doctors');
                const doctorsData = Array.isArray(response.data) ? response.data : [];
                setDoctors(doctorsData);

                if (!selectedDoctorId && doctorsData.length > 0) {
                    setSelectedDoctorId(String(doctorsData[0].id));
                }
            } catch (err) {
                setError('Failed to load doctors.');
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchDoctors();
    }, []);

    useEffect(() => {
        if (!selectedDoctorId) {
            setDoctor(null);
            setTimeSlots([]);
            setSelectedSlot('');
            setAppointmentFee(null);
            setFeeSource('default');
            return;
        }

        const fetchBookingDetails = async () => {
            try {
                const [doctorRes, slotsRes, feeRes] = await Promise.all([
                    api.get(`/doctors/${selectedDoctorId}`),
                    api.get(`/timeslots/doctor/${selectedDoctorId}/available`),
                    api.get(`/appointments/booking-fee?doctorId=${selectedDoctorId}`),
                ]);

                setDoctor(doctorRes.data);
                setTimeSlots(Array.isArray(slotsRes.data) ? slotsRes.data : []);
                setAppointmentFee(feeRes?.data?.appointmentFee ?? null);
                setFeeSource(feeRes?.data?.feeSource ?? 'default');
                setSelectedSlot('');
                setError(null);
            } catch (err) {
                setError('Failed to load booking details for this doctor.');
                setDoctor(null);
                setTimeSlots([]);
                setSelectedSlot('');
                setAppointmentFee(null);
                setFeeSource('default');
                console.error(err);
            }
        };

        fetchBookingDetails();
    }, [selectedDoctorId]);

    const resolveLoggedInPatientId = async () => {
        const userId = await resolveUserId('ROLE_PATIENT');
        return String(userId);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        if (!selectedDoctorId) {
            setError('Please select a doctor.');
            return;
        }

        if (!selectedSlot) {
            setError('Please select a time slot.');
            return;
        }

        if (appointmentFee == null || appointmentFee <= 0) {
            setError('Unable to determine booking fee. Please refresh and try again.');
            return;
        }

        if (!cardNumber || !cardHolderName || !expiryDate || !cvv) {
            setError('Please complete payment details to book the appointment.');
            return;
        }

        const sanitizedCardNumber = sanitizeCardNumber(cardNumber);
        const normalizedCardHolderName = cardHolderName.trim().replace(/\s+/g, ' ');
        const sanitizedCvv = cvv.replace(/\D/g, '');

        if (!/^\d{12,19}$/.test(sanitizedCardNumber)) {
            setError('Card number must be between 12 and 19 digits.');
            return;
        }

        if (!isValidLuhnCardNumber(sanitizedCardNumber)) {
            setError('Card number is invalid. Please check and try again.');
            return;
        }

        if (!/^[A-Za-z][A-Za-z\s'.-]{1,98}$/.test(normalizedCardHolderName)) {
            setError('Card holder name is invalid.');
            return;
        }

        if (!isValidExpiryDate(expiryDate)) {
            setError('Card expiry date is invalid or already expired.');
            return;
        }

        if (!/^\d{3,4}$/.test(sanitizedCvv)) {
            setError('CVV must be 3 or 4 digits.');
            return;
        }

        try {
            setSubmitting(true);
            const patientId = await resolveLoggedInPatientId();

            await api.post('/appointments/book', {
                patientId,
                doctorId: selectedDoctorId,
                slotId: selectedSlot,
                reason,
                urgencyLevel: Number(urgency),
                paymentMethod,
                cardNumber: sanitizedCardNumber,
                cardHolderName: normalizedCardHolderName,
                expiryDate,
                cvv: sanitizedCvv,
            });
            navigate('/patient/history');
        } catch (err) {
            const serverMessage = err?.response?.data?.content || err?.response?.data?.message;
            setError(serverMessage || 'Failed to book appointment. The slot might have just been taken.');
            console.error(err);
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div className="flex justify-center items-center h-screen"><p>Loading...</p></div>;
    if (!doctor && doctors.length === 0) {
        return (
            <div className="flex justify-center items-center h-screen">
                <p className="text-red-500">{error || 'No doctors found.'}</p>
            </div>
        );
    }

    return (
        <motion.div 
            className="container mx-auto p-8 max-w-3xl"
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
        >
            <h1 className="text-4xl font-bold mb-4 text-gray-800">Book Appointment</h1>
            {doctor && (
                <h2 className="text-2xl font-semibold mb-8 text-indigo-600">with Dr. {doctor.name} ({doctor.specialization})</h2>
            )}

            {error && <p className="mb-4 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}

            <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg shadow-lg space-y-6">
                <div>
                    <label htmlFor="doctor" className="block text-lg font-medium text-gray-700 mb-2">Select Doctor</label>
                    <select
                        id="doctor"
                        value={selectedDoctorId}
                        onChange={(e) => setSelectedDoctorId(e.target.value)}
                        className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                        required
                    >
                        <option value="" disabled>Select an available doctor</option>
                        {doctors.map((doctorItem) => (
                            <option key={doctorItem.id} value={doctorItem.id}>
                                Dr. {doctorItem.name} ({doctorItem.specialization})
                                {doctorItem.consultationFee != null ? ` - ${formatCurrency(doctorItem.consultationFee)}` : ''}
                            </option>
                        ))}
                    </select>
                </div>

                <div>
                    <label htmlFor="time-slot" className="block text-lg font-medium text-gray-700 mb-2">Select a Time Slot</label>
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
                                    value={String(slot.id)}
                                    checked={selectedSlot === String(slot.id)}
                                    onChange={(e) => setSelectedSlot(e.target.value)}
                                    className="hidden"
                                />
                                <label 
                                    htmlFor={`slot-${slot.id}`} 
                                    className={`block text-center p-4 rounded-lg cursor-pointer transition-colors ${
                                        selectedSlot === String(slot.id) 
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

                <div>
                    <label htmlFor="reason" className="block text-lg font-medium text-gray-700">Reason for Visit</label>
                    <textarea
                        id="reason"
                        value={reason}
                        onChange={(e) => setReason(e.target.value)}
                        rows="4"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                        placeholder="Briefly describe the reason for your visit..."
                        required
                    ></textarea>
                </div>

                <div>
                    <label htmlFor="urgency" className="block text-lg font-medium text-gray-700">Urgency Level</label>
                    <input
                        id="urgency"
                        type="range"
                        min="1"
                        max="5"
                        value={urgency}
                        onChange={(e) => setUrgency(e.target.value)}
                        className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                    />
                    <div className="flex justify-between text-sm text-gray-500 mt-1">
                        <span>Low</span>
                        <span>Medium</span>
                        <span>High</span>
                    </div>
                </div>

                <div className="rounded-lg border border-indigo-200 bg-indigo-50 p-4">
                    <p className="text-sm font-semibold text-indigo-700">Appointment Fee (Doctor Charge)</p>
                    <p className="mt-1 text-2xl font-bold text-indigo-900">
                        {appointmentFee != null ? formatCurrency(appointmentFee) : 'Loading...'}
                    </p>
                    <p className="mt-1 text-xs text-indigo-700">
                        {feeSource === 'doctor' ? 'Using doctor-specific consultation fee' : 'Using default clinic fee'}
                    </p>
                </div>

                <div className="space-y-4 rounded-lg border border-gray-200 p-4">
                    <h3 className="text-lg font-semibold text-gray-800">Payment Details</h3>

                    <div>
                        <label htmlFor="paymentMethod" className="block text-sm font-medium text-gray-700">Payment Method</label>
                        <select
                            id="paymentMethod"
                            value={paymentMethod}
                            onChange={(e) => setPaymentMethod(e.target.value)}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                            required
                        >
                            <option value="CARD">Card</option>
                        </select>
                    </div>

                    <div>
                        <label htmlFor="cardNumber" className="block text-sm font-medium text-gray-700">Card Number</label>
                        <input
                            id="cardNumber"
                            type="text"
                            inputMode="numeric"
                            autoComplete="cc-number"
                            value={cardNumber}
                            onChange={(e) => setCardNumber(formatCardNumberForInput(e.target.value))}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                            placeholder="1234 5678 9012 3456"
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="cardHolderName" className="block text-sm font-medium text-gray-700">Card Holder Name</label>
                        <input
                            id="cardHolderName"
                            type="text"
                            autoComplete="cc-name"
                            value={cardHolderName}
                            onChange={(e) => setCardHolderName(e.target.value)}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                            placeholder="Name on card"
                            required
                        />
                    </div>

                    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                        <div>
                            <label htmlFor="expiryDate" className="block text-sm font-medium text-gray-700">Expiry Date</label>
                            <input
                                id="expiryDate"
                                type="month"
                                min={currentMonthValue}
                                autoComplete="cc-exp"
                                value={expiryDate}
                                onChange={(e) => setExpiryDate(e.target.value)}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                                required
                            />
                        </div>
                        <div>
                            <label htmlFor="cvv" className="block text-sm font-medium text-gray-700">CVV</label>
                            <input
                                id="cvv"
                                type="password"
                                inputMode="numeric"
                                autoComplete="cc-csc"
                                maxLength={4}
                                value={cvv}
                                onChange={(e) => setCvv(e.target.value.replace(/\D/g, '').slice(0, 4))}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                                placeholder="123"
                                required
                            />
                        </div>
                    </div>
                </div>

                <motion.button
                    type="submit"
                    className="w-full bg-indigo-600 text-white py-3 px-4 rounded-md font-semibold hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400"
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.98 }}
                    disabled={!selectedSlot || !reason || !selectedDoctorId || !cardNumber || !cardHolderName || !expiryDate || !cvv || appointmentFee == null || submitting}
                >
                    {submitting ? 'Processing Payment & Booking...' : 'Pay & Book Appointment'}
                </motion.button>
            </form>
        </motion.div>
    );
};

export default Book;