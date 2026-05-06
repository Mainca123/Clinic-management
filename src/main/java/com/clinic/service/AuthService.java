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
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.smallrye.jwt.auth.principal.ParseException;
import org.eclipse.microprofile.jwt.JsonWebToken;

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

        if(!user.getIsVerified())
            throw new RuntimeException("account.not.verified");
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
        user.setIsVerified(false);
        user.setTokenVerified(tokenUtils.generateVerifyToken(user.getEmail()));
        userRepository.persist(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getTokenVerified());
        return Message.User.REGISTER;
    }

    @Inject
    JWTParser parser;

    @Inject
    EmailService emailService;

    @Transactional
    public String verifyEmail(String token) {

        try {
            token = java.net.URLDecoder.decode(token, java.nio.charset.StandardCharsets.UTF_8);

            JsonWebToken jwt = parser.parse(token);

            String email = jwt.getName();
            String type = jwt.getClaim("type");

            if (type == null || !"verify".equals(type)) {
                throw new RuntimeException("error.token");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("user.not.found"));

            if (user.getIsVerified()) {
                return "account.verified";
            }

            if (!token.equals(user.getTokenVerified())) {
                throw new RuntimeException("error.token");
            }

            user.setIsVerified(true);
            user.setTokenVerified(null);

            return "verified.success";

        } catch (Exception e) {

            User user = userRepository.findByTokenVerified(token)
                    .orElseThrow(() -> new RuntimeException("not.null.token"));

            String newToken = tokenUtils.generateVerifyToken(user.getEmail());
            user.setTokenVerified(newToken);
            userRepository.persist(user);

            emailService.sendVerificationEmail(user.getEmail(), newToken);

            return "resend.token";
        }
    }
}
