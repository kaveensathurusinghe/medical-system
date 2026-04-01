package com.medicalsystem.repository;

import com.medicalsystem.model.TimeSlot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends MongoRepository<TimeSlot, Long> {
    List<TimeSlot> findByDoctorIdAndIsAvailable(Long doctorId, boolean isAvailable);
    List<TimeSlot> findByDoctorId(Long doctorId);
}