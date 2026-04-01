package com.medicalsystem.dto;

public class DoctorDashboardStats {
    private long upcomingAppointments;
    private long todayAppointments;
    private long totalPatients;

    public DoctorDashboardStats() {
    }

    public DoctorDashboardStats(long upcomingAppointments, long todayAppointments, long totalPatients) {
        this.upcomingAppointments = upcomingAppointments;
        this.todayAppointments = todayAppointments;
        this.totalPatients = totalPatients;
    }

    public long getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(long upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }
}
