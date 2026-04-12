package com.medicalsystem.service;

import com.medicalsystem.model.Payment;
import com.medicalsystem.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(String appointmentId, String patientId, String doctorId, double amount,
                                  String paymentMethod, String cardNumber,
                                  String cardHolderName, String expiryDate, String cvv) {
        validatePaymentDetails(appointmentId, patientId, doctorId, amount,
                paymentMethod, cardNumber, cardHolderName, expiryDate, cvv);

        String sanitizedCardNumber = cardNumber.replaceAll("\\s+", "");
        String normalizedPaymentMethod = paymentMethod.trim().toUpperCase(Locale.ROOT);
        String normalizedCardHolderName = cardHolderName.trim().replaceAll("\\s+", " ");

        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        payment.setPatientId(patientId);
        payment.setDoctorId(doctorId);
        payment.setAmount(amount);
        payment.setDoctorCharge(amount);
        payment.setPaymentMethod(normalizedPaymentMethod);
        payment.setCardNumber(maskCardNumber(sanitizedCardNumber));
        payment.setCardHolderName(normalizedCardHolderName);
        payment.setExpiryDate(expiryDate);
        payment.setCvv("***");
        payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8));
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("COMPLETED");

        return paymentRepository.save(payment);
    }

    public List<Payment> getByPatientId(String patientId) {
        return paymentRepository.findByPatientId(patientId);
    }

    public List<Payment> getPaymentsByPatientId(String patientId) {
        return paymentRepository.findByPatientId(patientId);
    }

    public List<Payment> getPaymentsByDoctorId(String doctorId) {
        return paymentRepository.findByDoctorId(doctorId);
    }

    public double getTotal(){
        return paymentRepository.findAll().stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public double getTotalDoctorCharges() {
        return paymentRepository.findAll().stream()
                .mapToDouble(payment -> payment.getDoctorCharge() > 0 ? payment.getDoctorCharge() : payment.getAmount())
                .sum();
    }

    public double getTodayPaymentTotal() {
        LocalDate today = LocalDate.now();
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate() != null)
                .filter(payment -> today.equals(payment.getPaymentDate().toLocalDate()))
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public double getTotalIncomeByDoctorId(String doctorId) {
        return paymentRepository.findByDoctorId(doctorId).stream()
                .mapToDouble(payment -> payment.getDoctorCharge() > 0 ? payment.getDoctorCharge() : payment.getAmount())
                .sum();
    }

    public List<Payment> getRecentPayments(int limit) {
        return paymentRepository.findAllByOrderByPaymentDateDesc().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    private void validatePaymentDetails(String appointmentId, String patientId, String doctorId, double amount,
                                        String paymentMethod, String cardNumber,
                                        String cardHolderName, String expiryDate, String cvv) {
        if (isBlank(appointmentId) || isBlank(patientId) || isBlank(doctorId)) {
            throw new IllegalArgumentException("Appointment, patient, and doctor IDs are required for payment");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (isBlank(paymentMethod)) {
            throw new IllegalArgumentException("Payment method is required");
        }
        String normalizedPaymentMethod = paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!"CARD".equals(normalizedPaymentMethod)) {
            throw new IllegalArgumentException("Only card payments are supported");
        }
        if (isBlank(cardHolderName)) {
            throw new IllegalArgumentException("Card holder name is required");
        }
        String normalizedCardHolderName = cardHolderName.trim().replaceAll("\\s+", " ");
        if (!normalizedCardHolderName.matches("[A-Za-z][A-Za-z\\s'.-]{1,98}")) {
            throw new IllegalArgumentException("Card holder name is invalid");
        }

        String sanitizedCardNumber = cardNumber == null ? "" : cardNumber.replaceAll("\\s+", "");
        if (!sanitizedCardNumber.matches("\\d{12,19}")) {
            throw new IllegalArgumentException("Card number must be between 12 and 19 digits");
        }
        if (!isValidLuhn(sanitizedCardNumber)) {
            throw new IllegalArgumentException("Card number is invalid");
        }
        if (isBlank(expiryDate)) {
            throw new IllegalArgumentException("Expiry date is required");
        }
        validateExpiryDate(expiryDate);
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("CVV must be 3 or 4 digits");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private boolean isValidLuhn(String digitsOnlyCardNumber) {
        int sum = 0;
        boolean doubleDigit = false;

        for (int i = digitsOnlyCardNumber.length() - 1; i >= 0; i--) {
            int digit = digitsOnlyCardNumber.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return sum % 10 == 0;
    }

    private void validateExpiryDate(String expiryDate) {
        try {
            YearMonth expiry = YearMonth.parse(expiryDate, DateTimeFormatter.ofPattern("yyyy-MM"));
            if (expiry.isBefore(YearMonth.now())) {
                throw new IllegalArgumentException("Card has expired");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Expiry date must be in YYYY-MM format");
        }
    }
}