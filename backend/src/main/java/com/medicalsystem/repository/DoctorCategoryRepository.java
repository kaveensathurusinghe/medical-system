package com.medicalsystem.repository;

import com.medicalsystem.model.DoctorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorCategoryRepository extends JpaRepository<DoctorCategory, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<DoctorCategory> findByNameIgnoreCase(String name);
    List<DoctorCategory> findAllByOrderByNameAsc();
}
