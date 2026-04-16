package com.medicalsystem.dto;

import java.time.LocalDateTime;

public class AppointmentAdminDTO {
    private String appointmentId;
    private String patientName;
    private String doctorName;
    private LocalDateTime appointmentTime;
    private String status;
    private String reason;
    private int urgencyLevel;

    public AppointmentAdminDTO() {}

    public AppointmentAdminDTO(String appointmentId, String patientName, String doctorName, LocalDateTime appointmentTime, String status, String reason, int urgencyLevel) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.reason = reason;
        this.urgencyLevel = urgencyLevel;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(int urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }
}
