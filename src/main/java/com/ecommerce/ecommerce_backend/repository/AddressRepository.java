package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Fetch all addresses for a user
    List<Address> findByUserId(Long userId);

    // Fetch the default address for a user
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    // Before setting a new default, unset all existing defaults for that user
    // @Modifying marks this as a write operation (UPDATE/DELETE)
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultAddresses(@Param("userId") Long userId);
}