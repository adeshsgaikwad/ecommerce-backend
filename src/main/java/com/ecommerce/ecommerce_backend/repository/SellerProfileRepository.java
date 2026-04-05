package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.SellerProfile;
import com.ecommerce.ecommerce_backend.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

    // Check if a user already has a seller profile
    Optional<SellerProfile> findByUserId(Long userId);

    // Admin: fetch all applications by status (PENDING, APPROVED, REJECTED)
    List<SellerProfile> findByStatus(SellerStatus status);

    // Check duplicate GST number during seller application
    boolean existsByGstNumber(String gstNumber);
}