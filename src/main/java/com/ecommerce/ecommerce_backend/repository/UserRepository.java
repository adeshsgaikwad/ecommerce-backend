package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used on every login attempt
    Optional<User> findByEmail(String email);

    // Used during registration to check duplicate email
    boolean existsByEmail(String email);

    // Admin: fetch all users by role
    List<User> findByRole(Role role);

    // Admin: fetch all active/inactive users
    List<User> findByIsActive(Boolean isActive);
}