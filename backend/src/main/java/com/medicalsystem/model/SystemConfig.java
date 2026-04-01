package com.medicalsystem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "system_config")
public class SystemConfig {
    @Id
    private String id;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
