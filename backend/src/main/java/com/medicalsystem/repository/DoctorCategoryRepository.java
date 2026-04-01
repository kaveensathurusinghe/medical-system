package com.medicalsystem.repository;

import com.medicalsystem.model.DoctorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorCategoryRepository extends JpaRepository<DoctorCategory, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<DoctorCategory> findAllByOrderByNameAsc();
}
