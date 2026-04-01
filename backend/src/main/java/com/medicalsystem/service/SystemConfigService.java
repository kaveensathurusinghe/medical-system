package com.medicalsystem.service;

import com.medicalsystem.model.SystemConfig;
import com.medicalsystem.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {
    private final SystemConfigRepository configRepository;

    public SystemConfigService(SystemConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public SystemConfig getConfig() {
        return configRepository.getConfig();
    }

    public void updateAppointmentFee(double fee) {
        SystemConfig config = configRepository.getConfig();
        config.setAppointmentFee(fee);
        configRepository.saveConfig(config);
    }
}