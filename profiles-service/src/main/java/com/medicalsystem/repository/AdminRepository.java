package com.medicalsystem.repository;

import com.medicalsystem.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
}
