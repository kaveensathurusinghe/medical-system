package com.medicalsystem.dto;

import java.time.LocalDateTime;

public class AppointmentDTO {
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private String patientName;
    private String doctorName;
    private LocalDateTime appointmentTime;
    private String status;
    private String reason;

    public AppointmentDTO() {}

    public AppointmentDTO(Long appointmentId, Long patientId, Long doctorId,
                          String patientName, String doctorName,
                          LocalDateTime appointmentTime, String status, String reason) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.reason = reason;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
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
}

