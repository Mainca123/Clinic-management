package com.clinic.service;

import com.clinic.constant.RoleType;
import com.clinic.domain.dto.LoginRequest;
import com.clinic.domain.dto.LoginResponse;
import com.clinic.domain.entity.User;
import com.clinic.exception.ErrorMessage;
import com.clinic.repository.UserRepository;
import com.clinic.security.TokenUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthService {

    @Inject
    TokenUtils tokenUtils;

    @Inject
    UserRepository userRepository;

    public LoginResponse login(LoginRequest loginRequest){
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(()
        -> new RuntimeException(ErrorMessage.User.NOT_FOUND_USER));

        if(user.getPassword().equals(loginRequest.getPassword()))
            return LoginResponse.builder()
                    .role(user.getRole())
                    .token(tokenUtils.generateToken(user.getUsername(),user.getRole()))
                    .build();
        else
            throw new RuntimeException(ErrorMessage.User.INCORRECT_INFORMATION);
    }
}
