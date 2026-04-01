import React from 'react';
import { BrowserRouter as Router, Navigate, Route, Routes } from 'react-router-dom';

import Home from './components/Home';
import Login from './components/Login';
import Register from './components/Register';
import AdminLogin from './components/admin/AdminLogin';

import AdminDashboard from './components/admin/Dashboard';
import AdminAppointments from './components/admin/Appointments';
import AdminDoctors from './components/admin/Doctors';
import AdminPatients from './components/admin/Patients';
import AdminPayments from './components/admin/Payments';
import AdminNavbar from './components/admin/Navbar';

import DoctorDashboard from './components/doctor/Dashboard';
import DoctorAppointmentsHistory from './components/doctor/AppointmentsHistory';
import RegisterDoctor from './components/doctor/RegisterDoctor';
import EditDoctorProfile from './components/doctor/Edit';
import DoctorNavbar from './components/doctor/Navbar';

import PatientDashboard from './components/patient/Dashboard';
import PatientHistory from './components/patient/PatientHistory';
import PatientPaymentHistory from './components/patient/PaymentHistory';
import EditPatientProfile from './components/patient/Edit';
import PatientNavbar from './components/patient/Navbar';

import AppointmentBook from './components/appointment/Book';
import AppointmentPatientList from './components/appointment/PatientList';
import AppointmentReschedule from './components/appointment/Reschedule';
import AppointmentUpdate from './components/appointment/Update';
import AppointmentView from './components/appointment/View';

import RecordsForm from './components/records/Form';
import MedicalRecords from './components/records/MedicalRecords';
import PatientRecords from './components/records/PatientRecords';
import ViewRecord from './components/records/ViewRecord';
import DoctorTimeSlots from './components/timeslots/DoctorView';

const LayoutShell = ({ navbar, children }) => (
  <div className="min-h-screen">
    <div className="sticky top-0 z-40">{navbar}</div>
    <main className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">{children}</main>
  </div>
);

const AdminLayout = ({ children }) => <LayoutShell navbar={<AdminNavbar />}>{children}</LayoutShell>;
const DoctorLayout = ({ children }) => <LayoutShell navbar={<DoctorNavbar />}>{children}</LayoutShell>;
const PatientLayout = ({ children }) => <LayoutShell navbar={<PatientNavbar />}>{children}</LayoutShell>;

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
          <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/register" element={<Register />} />
        <Route path="/register-patient" element={<Navigate to="/register" replace />} />

        <Route path="/admin/dashboard" element={<AdminLayout><AdminDashboard /></AdminLayout>} />
        <Route path="/admin/appointments" element={<AdminLayout><AdminAppointments /></AdminLayout>} />
        <Route path="/admin/doctors" element={<AdminLayout><AdminDoctors /></AdminLayout>} />
        <Route path="/admin/patients" element={<AdminLayout><AdminPatients /></AdminLayout>} />
        <Route path="/admin/payments" element={<AdminLayout><AdminPayments /></AdminLayout>} />
        <Route path="/admin/register-doctor" element={<AdminLayout><RegisterDoctor /></AdminLayout>} />

        <Route path="/doctor/dashboard" element={<DoctorLayout><DoctorDashboard /></DoctorLayout>} />
        <Route path="/doctor/appointments-history" element={<DoctorLayout><DoctorAppointmentsHistory /></DoctorLayout>} />
        <Route path="/doctor/edit" element={<DoctorLayout><EditDoctorProfile /></DoctorLayout>} />
        <Route path="/doctor/register" element={<DoctorLayout><RegisterDoctor /></DoctorLayout>} />

        <Route path="/patient/dashboard" element={<PatientLayout><PatientDashboard /></PatientLayout>} />
        <Route path="/patient/edit" element={<PatientLayout><EditPatientProfile /></PatientLayout>} />
        <Route path="/patient/history" element={<PatientLayout><PatientHistory /></PatientLayout>} />
        <Route path="/patient/payment-history" element={<PatientLayout><PatientPaymentHistory /></PatientLayout>} />
        <Route path="/patient/register" element={<PatientLayout><Register /></PatientLayout>} />
        <Route path="/patient/appointments/book" element={<PatientLayout><AppointmentBook /></PatientLayout>} />

        <Route path="/appointment/book" element={<AppointmentBook />} />
        <Route path="/appointment/patient-list" element={<AppointmentPatientList />} />
        <Route path="/appointment/reschedule" element={<Navigate to="/patient/history" replace />} />
        <Route path="/appointment/reschedule/:id" element={<AppointmentReschedule />} />
        <Route path="/appointment/update" element={<AppointmentUpdate />} />
        <Route path="/appointment/view/:id" element={<AppointmentView />} />

        <Route path="/records/form" element={<RecordsForm />} />
        <Route path="/records/medical-records" element={<MedicalRecords />} />
        <Route path="/records/patient-records" element={<PatientRecords />} />
        <Route path="/records/view/:recordId" element={<PatientLayout><ViewRecord /></PatientLayout>} />
        <Route path="/records/new/:appointmentId" element={<DoctorLayout><RecordsForm /></DoctorLayout>} />

        <Route path="/timeslots/doctor" element={<DoctorLayout><DoctorTimeSlots /></DoctorLayout>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
