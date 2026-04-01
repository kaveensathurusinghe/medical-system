package com.medicalsystem.service;

import com.medicalsystem.model.Doctor;
import com.medicalsystem.model.Patient;
import com.medicalsystem.repository.DoctorRepository;
import com.medicalsystem.repository.PatientRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDetailsServiceImpl(PatientRepository patientRepository,
                                  DoctorRepository doctorRepository,
                                  PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Patient> patient = patientRepository.findByEmail(email);
        if (patient.isPresent()) {
            return User.builder()
                    .username(patient.get().getEmail())
                    .password(patient.get().getPassword())
                    .roles("PATIENT")
                    .build();
        }

        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        if (doctor.isPresent()) {
            return User.builder()
                    .username(doctor.get().getEmail())
                    .password(doctor.get().getPassword())
                    .roles("DOCTOR")
                    .build();
        }

        if ("admin@gmail.com".equals(email)) {
            String encodedPassword = passwordEncoder.encode("admin123");
            return User.builder()
                    .username("admin@gmail.com")
                    .password(encodedPassword)
                    .roles("ADMIN")
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}