package com.medicalsystem.controller;

import com.medicalsystem.model.DoctorCategory;
import com.medicalsystem.service.DoctorCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DoctorCategoryController {

    @Autowired
    private DoctorCategoryService doctorCategoryService;

    @GetMapping("/doctor-categories")
    public ResponseEntity<List<DoctorCategory>> getDoctorCategories() {
        return ResponseEntity.ok(doctorCategoryService.getAllCategories());
    }

    @GetMapping("/admin/doctor-categories")
    public ResponseEntity<List<DoctorCategory>> getDoctorCategoriesForAdmin() {
        return ResponseEntity.ok(doctorCategoryService.getAllCategories());
    }

    @PostMapping("/admin/doctor-categories")
    public ResponseEntity<?> createDoctorCategory(@RequestBody CategoryRequest request) {
        try {
            DoctorCategory category = doctorCategoryService.createCategory(request.getName());
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/doctor-categories/{id}")
    public ResponseEntity<?> deleteDoctorCategory(@PathVariable Long id) {
        try {
            doctorCategoryService.deleteCategory(id);
            return ResponseEntity.ok(new AdminController.Message("SUCCESS", "Category deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AdminController.Message("ERROR", e.getMessage()));
        }
    }

    public static class CategoryRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
