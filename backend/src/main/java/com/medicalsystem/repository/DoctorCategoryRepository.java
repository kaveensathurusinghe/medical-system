package com.medicalsystem.repository;

import com.medicalsystem.model.DoctorCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorCategoryRepository extends MongoRepository<DoctorCategory, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<DoctorCategory> findByNameIgnoreCase(String name);
    List<DoctorCategory> findAllByOrderByNameAsc();
}
