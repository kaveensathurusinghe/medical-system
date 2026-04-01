package com.medicalsystem.config;

import com.medicalsystem.model.Admin;
import com.medicalsystem.repository.AdminRepository;
import com.medicalsystem.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Value("${app.admin.name:System Administrator}")
    private String adminName;

    @Value("${app.admin.email:admin@medicalsystem.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    public AdminDataInitializer(AdminRepository adminRepository,
                                PasswordEncoder passwordEncoder,
                                SequenceGeneratorService sequenceGeneratorService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        Admin admin = new Admin();
        admin.setId(sequenceGeneratorService.generateSequence("admin_seq"));
        admin.setName(adminName);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        adminRepository.save(admin);
    }
}
