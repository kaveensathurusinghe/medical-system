package com.medicalsystem.repository;

import com.medicalsystem.model.SystemConfig;
import org.springframework.stereotype.Repository;

import java.io.*;

@Repository
public class SystemConfigRepository {
    private static final String FILE_PATH = "data/config.txt";
    private SystemConfig config = new SystemConfig();

    public SystemConfigRepository() {
        loadConfig();
    }

    public SystemConfig getConfig() {
        return config;
    }

    public void saveConfig(SystemConfig config) {
        this.config = config;
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            writer.println(config.getAppointmentFee());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }

    private void loadConfig() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null) {
                    config.setAppointmentFee(Double.parseDouble(line));
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Failed to load config, using default values");
            }
        }
    }
}