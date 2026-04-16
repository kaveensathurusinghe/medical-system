package com.medicalsystem.appointments.service;

import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    public static class SystemConfig {
        private double appointmentFee = 20.0;

        public double getAppointmentFee() {
            return appointmentFee;
        }

        public void setAppointmentFee(double appointmentFee) {
            this.appointmentFee = appointmentFee;
        }
    }

    public SystemConfig getConfig() {
        return new SystemConfig();
    }
}
