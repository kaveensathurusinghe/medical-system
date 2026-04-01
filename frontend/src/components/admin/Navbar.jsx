import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, Stethoscope, Users, CalendarDays, CreditCard } from 'lucide-react';
import api from '../../services/api';

const links = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/admin/appointments', label: 'Appointments', icon: CalendarDays },
  { to: '/admin/doctors', label: 'Doctors', icon: Stethoscope },
  { to: '/admin/patients', label: 'Patients', icon: Users },
  { to: '/admin/payments', label: 'Payments', icon: CreditCard },
];

const Navbar = () => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error('Admin logout failed', error);
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      localStorage.removeItem('userId');
      navigate('/');
    }
  };

  return (
    <header className="border-b border-slate-200 bg-white/85 backdrop-blur-lg">
      <div className="mx-auto flex w-full max-w-7xl flex-wrap items-center justify-between gap-3 px-4 py-3 sm:px-6 lg:px-8">
        <NavLink to="/" className="flex items-center gap-2 text-lg font-bold text-slate-900">
          <span className="inline-block h-2.5 w-2.5 rounded-full bg-cyan-700" />
          MediCare Admin
        </NavLink>

        <nav className="flex flex-wrap items-center gap-2">
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `inline-flex items-center gap-2 rounded-full px-3 py-2 text-sm font-semibold transition ${
                  isActive ? 'bg-cyan-700 text-white' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                }`
              }
            >
              <Icon size={16} />
              {label}
            </NavLink>
          ))}
          <button
            type="button"
            onClick={handleLogout}
            className="inline-flex items-center gap-2 rounded-full bg-slate-900 px-3 py-2 text-sm font-semibold text-white transition hover:bg-slate-800"
          >
            Logout
          </button>
        </nav>
      </div>
    </header>
  );
};

export default Navbar;
