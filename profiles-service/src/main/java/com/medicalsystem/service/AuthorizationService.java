package com.medicalsystem.service;

import com.medicalsystem.model.Appointment;
import com.medicalsystem.repository.DoctorRepository;
import com.medicalsystem.repository.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthorizationService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AuthorizationService(DoctorRepository doctorRepository,
                                PatientRepository patientRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN");
    }

    public boolean isDoctor(Authentication authentication) {
        return hasRole(authentication, "ROLE_DOCTOR");
    }

    public boolean isPatient(Authentication authentication) {
        return hasRole(authentication, "ROLE_PATIENT");
    }

    public Long getCurrentDoctorId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        return doctorRepository.findByEmail(authentication.getName())
                .map(doctor -> doctor.getId())
                .orElse(null);
    }

    public Long getCurrentPatientId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        return patientRepository.findByEmail(authentication.getName())
                .map(patient -> patient.getId())
                .orElse(null);
    }

    public boolean canAccessDoctor(Authentication authentication, Long doctorId) {
        if (doctorId == null) {
            return false;
        }

        return isAdmin(authentication)
                || (isDoctor(authentication) && Objects.equals(getCurrentDoctorId(authentication), doctorId));
    }

    public boolean canAccessPatient(Authentication authentication, Long patientId) {
        if (patientId == null) {
            return false;
        }

        return isAdmin(authentication)
                || (isPatient(authentication) && Objects.equals(getCurrentPatientId(authentication), patientId));
    }

    public boolean canAccessAppointment(Authentication authentication, Appointment appointment) {
        if (appointment == null) {
            return false;
        }

        if (isAdmin(authentication)) {
            return true;
        }

        if (isDoctor(authentication)) {
            return Objects.equals(getCurrentDoctorId(authentication), appointment.getDoctorId());
        }

        if (isPatient(authentication)) {
            return Objects.equals(getCurrentPatientId(authentication), appointment.getPatientId());
        }

        return false;
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }

        return false;
    }
}
