package com.ecommerce.ecommerce_backend.util;

import com.ecommerce.ecommerce_backend.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    // Convenience method — get the currently logged-in user's ID anywhere in the app
    public static Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return principal.getId();
    }

    public static String getCurrentUserEmail() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return principal.getEmail();
    }
}