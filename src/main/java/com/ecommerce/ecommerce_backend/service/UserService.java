package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.request.AddressRequest;
import com.ecommerce.ecommerce_backend.entity.Address;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.exception.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.repository.AddressRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ─── ADDRESS MANAGEMENT ─────────────────────────────────────────────────

    public List<Address> getMyAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Transactional
    public Address addAddress(Long userId, AddressRequest request) {
        User user = getById(userId);

        // If this is the first address, make it default automatically
        boolean hasAddresses = !addressRepository.findByUserId(userId).isEmpty();

        // If caller wants this to be default, clear existing defaults first
        if (request.getIsDefault() || !hasAddresses) {
            addressRepository.clearDefaultAddresses(userId);
        }

        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .label(request.getLabel())
                .isDefault(request.getIsDefault() || !hasAddresses)
                .build();

        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Ownership check — user can only update their own address
        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only update your own addresses");
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultAddresses(userId);
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());
        address.setLabel(request.getLabel());
        address.setIsDefault(request.getIsDefault());

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own addresses");
        }

        addressRepository.delete(address);
    }
}