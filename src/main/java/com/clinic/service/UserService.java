package com.clinic.service;

import com.clinic.constant.Message;
import com.clinic.domain.dto.PasswordRequest;
import com.clinic.domain.dto.UserResponse;
import com.clinic.domain.dto.UserUpdateRequest;
import com.clinic.domain.entity.User;
import com.clinic.domain.mapper.UserMapper;
import com.clinic.exception.ErrorMessage;
import com.clinic.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.file.Files;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper mapper;

    private User getCurrentUser(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.User.NOT_FOUND_USER));
    }

    public UserResponse getUserDetail(String identifier){
        return mapper.toUserResponse(getCurrentUser(identifier));
    }

    @Transactional
    public String updateCurrentUser(String identifier, UserUpdateRequest request) {

        User user = getCurrentUser(identifier);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        return Message.User.SUCCESS;
    }

    @Inject
    FileUploadService fileUploadService;
    @Transactional
    public String uploadAvatar(String identifier, FileUpload file) {

        User user = getCurrentUser(identifier);

        try {
            byte[] bytes = Files.readAllBytes(file.uploadedFile());

            String avatarUrl = fileUploadService.upload(bytes);

            user.setAvatarUrl(avatarUrl);

            return avatarUrl;

        } catch (Exception e) {
            throw new RuntimeException("Upload avatar failed", e);
        }
    }

    @Transactional
    public String changePassword(String identifier, PasswordRequest passwordRequest){
        if(!passwordRequest.getPassword().equals(passwordRequest.getConfirmPassword()))
            throw new RuntimeException("password.not.correct");
        User user = getCurrentUser(identifier);
        user.setPassword(BcryptUtil.bcryptHash(passwordRequest.getConfirmPassword()));
        user.setIsVerified(true);
        userRepository.persist(user);
        return "change.password.success";
    }
}