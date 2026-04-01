package com.medicalsystem.service;

import com.medicalsystem.model.SystemConfig;
import com.medicalsystem.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {
    private static final String CONFIG_ID = "system-config";

    private final SystemConfigRepository configRepository;

    public SystemConfigService(SystemConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public SystemConfig getConfig() {
        return configRepository.findById(CONFIG_ID)
                .orElseGet(() -> {
                    SystemConfig config = new SystemConfig();
                    config.setId(CONFIG_ID);
                    return configRepository.save(config);
                });
    }

    public void updateAppointmentFee(double fee) {
        SystemConfig config = getConfig();
        config.setAppointmentFee(fee);
        configRepository.save(config);
    }
}