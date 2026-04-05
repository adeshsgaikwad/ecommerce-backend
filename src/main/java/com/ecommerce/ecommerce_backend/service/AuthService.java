package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.request.LoginRequest;
import com.ecommerce.ecommerce_backend.dto.request.RegisterRequest;
import com.ecommerce.ecommerce_backend.dto.response.AuthResponse;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.enums.Role;
import com.ecommerce.ecommerce_backend.exception.BadRequestException;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import com.ecommerce.ecommerce_backend.security.JwtTokenProvider;
import com.ecommerce.ecommerce_backend.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;
    private final OtpUtil otpUtil;

    private static final String OTP_PREFIX = "otp:";
    private static final long OTP_TTL_MINUTES = 5;

    @Transactional
    public String register(RegisterRequest request) {
        // Check duplicate email before saving
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.BUYER)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        userRepository.save(user);

        // Generate OTP, store in Redis with 5-min TTL
        String otp = otpUtil.generateOtp();
        redisTemplate.opsForValue().set(
                OTP_PREFIX + request.getEmail(),
                otp,
                OTP_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        // Send OTP email asynchronously
        notificationService.sendOtpEmail(request.getEmail(), otp);

        return "Registration successful. Please verify your email with the OTP sent.";
    }

    @Transactional
    public String verifyOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            throw new BadRequestException("OTP expired. Please request a new one.");
        }
        if (!storedOtp.equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }

        // Mark email as verified and delete OTP from Redis
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setIsEmailVerified(true);
        userRepository.save(user);
        redisTemplate.delete(key);

        return "Email verified successfully. You can now log in.";
    }

    public AuthResponse login(LoginRequest request) {
        // Delegates to Spring Security — checks email + password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!user.getIsEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in.");
        }

        if (!user.getIsActive()) {
            throw new BadRequestException("Your account has been deactivated. Contact support.");
        }

        // Generate JWT with userId and role embedded
        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}