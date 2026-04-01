package com.medicalsystem.repository;

import com.medicalsystem.model.Payment;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PaymentRepository {
    private static final String FILE_PATH = "data/payments.txt";
    private static final String DELIMITER = "|";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public PaymentRepository() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdir();
        File file = new File(FILE_PATH);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create file", e);
        }
    }

    public Payment save(Payment payment) {
        if (payment.getPaymentId() == null) {
            payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            writer.println(convertToFileLine(payment));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save payment", e);
        }

        return payment;
    }

    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        payments.add(convertFromLine(line));
                    } catch (Exception e) {
                        System.err.println("Skipping malformed payment record: " + line);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read payments", e);
        }
        return payments;
    }

    public List<Payment> findByPatientId(String patientId) {
        return findAll().stream()
                .filter(payment -> payment.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    private String convertToFileLine(Payment payment) {
        return String.join(DELIMITER,
                payment.getPaymentId(),
                payment.getAppointmentId(),
                payment.getPatientId(),
                String.valueOf(payment.getAmount()),
                payment.getPaymentMethod(),
                payment.getCardNumber(),
                payment.getCardHolderName(),
                payment.getExpiryDate(),
                payment.getCvv(),
                payment.getPaymentDate().format(formatter),
                payment.getStatus());
    }

    private Payment convertFromLine(String line) {
        String[] parts = line.split("\\" + DELIMITER, -1);
        if (parts.length != 11) {
            throw new RuntimeException("Invalid payment record. Expected 11 fields but got " + parts.length + ": " + line);
        }

        Payment payment = new Payment();
        payment.setPaymentId(parts[0]);
        payment.setAppointmentId(parts[1]);
        payment.setPatientId(parts[2]);
        payment.setAmount(Double.parseDouble(parts[3]));
        payment.setPaymentMethod(parts[4]);
        payment.setCardNumber(parts[5]);
        payment.setCardHolderName(parts[6]);
        payment.setExpiryDate(parts[7]);
        payment.setCvv(parts[8]);
        payment.setPaymentDate(LocalDateTime.parse(parts[9], formatter));
        payment.setStatus(parts[10]);
        return payment;
    }

    public double calculateTotalPayments() {
        return findAll().stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public List<Payment> findRecentPayments(int limit) {
        return findAll().stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}