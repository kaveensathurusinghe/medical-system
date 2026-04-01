package com.medicalsystem.repository;

import com.medicalsystem.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByDoctorIdAndIsAvailable(Long doctorId, boolean isAvailable);
    List<TimeSlot> findByDoctorId(Long doctorId);
}