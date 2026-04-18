package com.clinic.service;

import com.clinic.constant.Message;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.LoginRequest;
import com.clinic.domain.dto.LoginResponse;
import com.clinic.domain.dto.RegisterRequest;
import com.clinic.domain.entity.User;
import com.clinic.domain.mapper.UserMapper;
import com.clinic.exception.ErrorMessage;
import com.clinic.repository.UserRepository;
import com.clinic.security.TokenUtils;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    @Inject
    TokenUtils tokenUtils;

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    public LoginResponse login(LoginRequest loginRequest){
        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername()).orElseThrow(()
        -> new RuntimeException(ErrorMessage.User.NOT_FOUND_USER));

        if(BcryptUtil.matches(loginRequest.getPassword(), user.getPassword()))
            return LoginResponse.builder()
                    .role(user.getRole().name())
                    .token(tokenUtils.generateToken(user.getUsername(),user.getRole().name()))
                    .build();
        else
            throw new RuntimeException(ErrorMessage.User.INCORRECT_INFORMATION);
    }


    @Transactional
    public String register(RegisterRequest request){
        if(userRepository.findByUsernameOrEmail(request.getUsername()).isPresent())
            throw new RuntimeException(ErrorMessage.User.SAVE_INFORMATION);
        User user = userMapper.toUser(request);
        user.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
        user.setRole(RoleType.PATIENT);
        userRepository.persist(user);
        return Message.User.REGISTER;
    }

}
