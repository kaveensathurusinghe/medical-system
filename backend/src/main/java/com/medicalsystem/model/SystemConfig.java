package com.medicalsystem.model;

public class SystemConfig {
    private double appointmentFee;

    public SystemConfig() {
        this.appointmentFee = 2000.00; // Default fee
    }

    public double getAppointmentFee() {
        return appointmentFee;
    }

    public void setAppointmentFee(double appointmentFee) {
        this.appointmentFee = appointmentFee;
    }
}
