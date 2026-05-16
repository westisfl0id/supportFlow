package com.supportflow.user.service;

import com.supportflow.security.CurrentUserService;
import com.supportflow.user.dto.CreateUserRequest;
import com.supportflow.user.dto.UpdateUserRoleRequest;
import com.supportflow.user.dto.UpdateUserStatusRequest;
import com.supportflow.user.dto.UserResponse;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import com.supportflow.user.exception.*;
import com.supportflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        UserEntity user = UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        return map(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByOrderByStatusAscIdAsc()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        UserEntity user = findUserById(id);
        return map(user);
    }

    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        UserEntity targetUser = findUserById(id);
        UserEntity currentUser = currentUserService.getCurrentUser();

        if (targetUser.getId().equals(currentUser.getId())
                && request.status() == UserStatus.BLOCKED) {
            throw new SelfBlockingNotAllowedException();
        }

        if (targetUser.getRole() == UserRole.ADMIN
                && request.status() == UserStatus.BLOCKED) {
            throw new AdminBlockingNotAllowedException();
        }

        UserStatus oldStatus = targetUser.getStatus();

        targetUser.setStatus(request.status());

        log.info(
                "User status changed: targetUserId={}, oldStatus={}, newStatus={}, changedById={}",
                targetUser.getId(),
                oldStatus,
                request.status(),
                currentUser.getId()
        );

        return map(targetUser);
    }

    @Transactional
    public UserResponse updateUserRole(Long id, UpdateUserRoleRequest request) {
        UserEntity targetUser = findUserById(id);
        UserEntity currentUser = currentUserService.getCurrentUser();

        if (targetUser.getId().equals(currentUser.getId())) {
            throw new SelfRoleChangeNotAllowedException();
        }

        UserRole oldRole = targetUser.getRole();

        targetUser.setRole(request.role());

        log.info(
                "User role changed: targetUserId={}, oldRole={}, newRole={}, changedById={}",
                targetUser.getId(),
                oldRole,
                request.role(),
                currentUser.getId()
        );

        return map(targetUser);
    }

    private UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse map(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
