import React from 'react';
import { BrowserRouter as Router, Navigate, Route, Routes } from 'react-router-dom';
import api from './services/api';

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

const roleHomeRoute = {
  ROLE_ADMIN: '/admin/dashboard',
  ROLE_DOCTOR: '/doctor/dashboard',
  ROLE_PATIENT: '/patient/dashboard',
};

const RequireRole = ({ allowedRoles, children, loginPath = '/login' }) => {
  const [resolvedRole, setResolvedRole] = React.useState(null);
  const [checkingAuth, setCheckingAuth] = React.useState(true);

  React.useEffect(() => {
    let active = true;

    const checkSession = async () => {
      try {
        const response = await api.get('/auth/me');
        const role = response?.data?.role;

        if (!active) {
          return;
        }

        if (role) {
          localStorage.setItem('role', role);
          const userId = response?.data?.userId;
          if (userId !== null && userId !== undefined) {
            localStorage.setItem('userId', String(userId));
          } else {
            localStorage.removeItem('userId');
          }
          setResolvedRole(role);
        } else {
          localStorage.removeItem('token');
          localStorage.removeItem('role');
          localStorage.removeItem('userId');
          setResolvedRole(null);
        }
      } catch (error) {
        if (!active) {
          return;
        }
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        localStorage.removeItem('userId');
        setResolvedRole(null);
      } finally {
        if (active) {
          setCheckingAuth(false);
        }
      }
    };

    checkSession();

    return () => {
      active = false;
    };
  }, []);

  if (checkingAuth) {
    return null;
  }

  if (!resolvedRole) {
    return <Navigate to={loginPath} replace />;
  }

  if (!allowedRoles.includes(resolvedRole)) {
    return <Navigate to={roleHomeRoute[resolvedRole] || '/'} replace />;
  }

  return children;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/register" element={<Register />} />
        <Route path="/register-patient" element={<Navigate to="/register" replace />} />

        <Route path="/admin/dashboard" element={<RequireRole allowedRoles={['ROLE_ADMIN']} loginPath="/admin/login"><AdminLayout><AdminDashboard /></AdminLayout></RequireRole>} />
        <Route path="/admin/appointments" element={<RequireRole allowedRoles={['ROLE_ADMIN']} loginPath="/admin/login"><AdminLayout><AdminAppointments /></AdminLayout></RequireRole>} />
        <Route path="/admin/doctors" element={<RequireRole allowedRoles={['ROLE_ADMIN']} loginPath="/admin/login"><AdminLayout><AdminDoctors /></AdminLayout></RequireRole>} />
        <Route path="/admin/patients" element={<RequireRole allowedRoles={['ROLE_ADMIN']} loginPath="/admin/login"><AdminLayout><AdminPatients /></AdminLayout></RequireRole>} />
        <Route path="/admin/payments" element={<RequireRole allowedRoles={['ROLE_ADMIN']} loginPath="/admin/login"><AdminLayout><AdminPayments /></AdminLayout></RequireRole>} />
        <Route path="/admin/register-doctor" element={<RequireRole allowedRoles={['ROLE_ADMIN']} loginPath="/admin/login"><AdminLayout><RegisterDoctor /></AdminLayout></RequireRole>} />

        <Route path="/doctor/dashboard" element={<RequireRole allowedRoles={['ROLE_DOCTOR']}><DoctorLayout><DoctorDashboard /></DoctorLayout></RequireRole>} />
        <Route path="/doctor/appointments-history" element={<RequireRole allowedRoles={['ROLE_DOCTOR']}><DoctorLayout><DoctorAppointmentsHistory /></DoctorLayout></RequireRole>} />
        <Route path="/doctor/edit" element={<RequireRole allowedRoles={['ROLE_DOCTOR']}><DoctorLayout><EditDoctorProfile /></DoctorLayout></RequireRole>} />
        <Route path="/doctor/register" element={<RequireRole allowedRoles={['ROLE_ADMIN']} loginPath="/admin/login"><AdminLayout><RegisterDoctor /></AdminLayout></RequireRole>} />

        <Route path="/patient/dashboard" element={<RequireRole allowedRoles={['ROLE_PATIENT']}><PatientLayout><PatientDashboard /></PatientLayout></RequireRole>} />
        <Route path="/patient/edit" element={<RequireRole allowedRoles={['ROLE_PATIENT']}><PatientLayout><EditPatientProfile /></PatientLayout></RequireRole>} />
        <Route path="/patient/history" element={<RequireRole allowedRoles={['ROLE_PATIENT']}><PatientLayout><PatientHistory /></PatientLayout></RequireRole>} />
        <Route path="/patient/payment-history" element={<RequireRole allowedRoles={['ROLE_PATIENT']}><PatientLayout><PatientPaymentHistory /></PatientLayout></RequireRole>} />
        <Route path="/patient/register" element={<Navigate to="/register" replace />} />
        <Route path="/patient/appointments/book" element={<RequireRole allowedRoles={['ROLE_PATIENT']}><PatientLayout><AppointmentBook /></PatientLayout></RequireRole>} />

        <Route path="/appointment/book" element={<RequireRole allowedRoles={['ROLE_PATIENT']}><AppointmentBook /></RequireRole>} />
        <Route path="/appointment/patient-list" element={<RequireRole allowedRoles={['ROLE_PATIENT']}><AppointmentPatientList /></RequireRole>} />
        <Route path="/appointment/reschedule" element={<Navigate to="/patient/history" replace />} />
        <Route path="/appointment/reschedule/:id" element={<RequireRole allowedRoles={['ROLE_PATIENT']}><AppointmentReschedule /></RequireRole>} />
        <Route path="/appointment/update" element={<RequireRole allowedRoles={['ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN']}><AppointmentUpdate /></RequireRole>} />
        <Route path="/appointment/view/:id" element={<RequireRole allowedRoles={['ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN']}><AppointmentView /></RequireRole>} />

        <Route path="/records/form" element={<RequireRole allowedRoles={['ROLE_DOCTOR', 'ROLE_ADMIN']}><RecordsForm /></RequireRole>} />
        <Route path="/records/medical-records" element={<RequireRole allowedRoles={['ROLE_DOCTOR', 'ROLE_ADMIN']}><MedicalRecords /></RequireRole>} />
        <Route path="/records/patient-records" element={<RequireRole allowedRoles={['ROLE_PATIENT', 'ROLE_ADMIN']}><PatientRecords /></RequireRole>} />
        <Route path="/records/view/:recordId" element={<RequireRole allowedRoles={['ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN']}><ViewRecord /></RequireRole>} />
        <Route path="/records/new/:appointmentId" element={<RequireRole allowedRoles={['ROLE_DOCTOR', 'ROLE_ADMIN']}><DoctorLayout><RecordsForm /></DoctorLayout></RequireRole>} />

        <Route path="/timeslots/doctor" element={<RequireRole allowedRoles={['ROLE_DOCTOR', 'ROLE_ADMIN']}><DoctorLayout><DoctorTimeSlots /></DoctorLayout></RequireRole>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
