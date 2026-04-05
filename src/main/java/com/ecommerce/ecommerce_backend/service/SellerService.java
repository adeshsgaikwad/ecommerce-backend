package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.request.SellerApplyRequest;
import com.ecommerce.ecommerce_backend.dto.response.SellerProfileResponse;
import com.ecommerce.ecommerce_backend.entity.SellerProfile;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.enums.Role;
import com.ecommerce.ecommerce_backend.enums.SellerStatus;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.kafka.producer.SellerEventProducer;
import com.ecommerce.ecommerce_backend.repository.SellerProfileRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;
    private final SellerEventProducer sellerEventProducer;
    private final NotificationService notificationService;

    // Buyer applies to become a seller
    @Transactional
    public SellerProfileResponse apply(Long userId, SellerApplyRequest request) {
        // Prevent duplicate applications
        if (sellerProfileRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("You have already submitted a seller application");
        }

        if (sellerProfileRepository.existsByGstNumber(request.getGstNumber())) {
            throw new BadRequestException("GST number already registered");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SellerProfile profile = SellerProfile.builder()
                .user(user)
                .shopName(request.getShopName())
                .gstNumber(request.getGstNumber())
                .bankAccount(request.getBankAccount())
                .ifscCode(request.getIfscCode())
                .status(SellerStatus.PENDING)
                .build();

        sellerProfileRepository.save(profile);

        // Kafka event → notifies admin asynchronously
        sellerEventProducer.publishSellerApplied(userId, user.getEmail(), request.getShopName());

        return mapToResponse(profile);
    }

    // Admin approves a seller application
    @Transactional
    public SellerProfileResponse approve(Long profileId) {
        SellerProfile profile = sellerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        if (profile.getStatus() != SellerStatus.PENDING) {
            throw new BadRequestException("Only PENDING applications can be approved");
        }

        profile.setStatus(SellerStatus.APPROVED);
        profile.setApprovedAt(LocalDateTime.now());
        sellerProfileRepository.save(profile);

        // Upgrade user role from BUYER to SELLER
        User user = profile.getUser();
        user.setRole(Role.SELLER);
        userRepository.save(user);

        notificationService.sendSellerApprovalEmail(user.getEmail(), profile.getShopName());

        return mapToResponse(profile);
    }

    // Admin rejects a seller application with a reason
    @Transactional
    public SellerProfileResponse reject(Long profileId, String reason) {
        SellerProfile profile = sellerProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        if (profile.getStatus() != SellerStatus.PENDING) {
            throw new BadRequestException("Only PENDING applications can be rejected");
        }

        profile.setStatus(SellerStatus.REJECTED);
        profile.setRejectionReason(reason);
        sellerProfileRepository.save(profile);

        notificationService.sendSellerRejectionEmail(
                profile.getUser().getEmail(),
                profile.getShopName(),
                reason
        );

        return mapToResponse(profile);
    }

    // Admin: fetch all applications by status
    public List<SellerProfileResponse> getByStatus(SellerStatus status) {
        return sellerProfileRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Seller: fetch their own profile
    public SellerProfileResponse getMyProfile(Long userId) {
        SellerProfile profile = sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
        return mapToResponse(profile);
    }

    private SellerProfileResponse mapToResponse(SellerProfile profile) {
        return SellerProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .sellerName(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .shopName(profile.getShopName())
                .gstNumber(profile.getGstNumber())
                .status(profile.getStatus())
                .rejectionReason(profile.getRejectionReason())
                .appliedAt(profile.getAppliedAt())
                .approvedAt(profile.getApprovedAt())
                .build();
    }
}