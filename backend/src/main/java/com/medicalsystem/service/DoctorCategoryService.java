package com.medicalsystem.service;

import com.medicalsystem.model.DoctorCategory;
import com.medicalsystem.repository.DoctorCategoryRepository;
import com.medicalsystem.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorCategoryService {

    @Autowired
    private DoctorCategoryRepository doctorCategoryRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    public List<DoctorCategory> getAllCategories() {
        return doctorCategoryRepository.findAllByOrderByNameAsc();
    }

    public DoctorCategory createCategory(String name) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (doctorCategoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Category already exists");
        }

        DoctorCategory category = new DoctorCategory();
        category.setName(normalizedName);
        return doctorCategoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        DoctorCategory category = doctorCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        long doctorsUsingCategory = doctorRepository.findAll().stream()
                .filter(doctor -> doctor.getSpecialization() != null
                        && doctor.getSpecialization().trim().equalsIgnoreCase(category.getName()))
                .count();

        if (doctorsUsingCategory > 0) {
            throw new IllegalStateException("Category is assigned to existing doctors and cannot be deleted");
        }

        doctorCategoryRepository.delete(category);
    }
}
