package com.supportflow.security;

import com.supportflow.auth.exception.InvalidCredentialsException;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException();
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails userDetails)) {
            throw new InvalidCredentialsException();
        }

        String email = userDetails.getUsername();

        return userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
    }

    public boolean isAdmin(UserEntity user) {
        return user.getRole() == UserRole.ADMIN;
    }

    public boolean isAgent(UserEntity user) {
        return user.getRole() == UserRole.AGENT;
    }

    public boolean isUser(UserEntity user) {
        return user.getRole() == UserRole.USER;
    }
}
