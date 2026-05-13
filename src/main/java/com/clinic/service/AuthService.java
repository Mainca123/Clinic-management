package com.clinic.service;

import com.clinic.constant.Message;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.LoginRequest;
import com.clinic.domain.dto.LoginResponse;
import com.clinic.domain.dto.PasswordRequest;
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
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthService {

    @Inject
    TokenUtils tokenUtils;

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    private User findUser(String key){
        User user = userRepository.findByUsernameOrEmail(key).orElseThrow(()
                -> new RuntimeException(ErrorMessage.User.NOT_FOUND_USER));
        return user;
    }

    public LoginResponse login(LoginRequest loginRequest){
        User user = findUser(loginRequest.getUsername());

        if(!user.getIsVerified() && user.getTokenVerified()!=null)
            throw new RuntimeException("account.not.verified");
        if(BcryptUtil.matches(loginRequest.getPassword(), user.getPassword())){
            if(!user.getIsVerified())
                return LoginResponse.builder()
                        .role(user.getRole().name())
                        .checkPass(true)
                        .token(tokenUtils.generateToken(user.id,user.getUsername(),user.getRole().name()))
                        .build();
            return LoginResponse.builder()
                    .role(user.getRole().name())
                    .checkPass(false)
                    .token(tokenUtils.generateToken(user.id,user.getUsername(),user.getRole().name()))
                    .build();
        }
        else
            throw new RuntimeException(ErrorMessage.User.INCORRECT_INFORMATION);
    }


    @Transactional
    public String register(RegisterRequest request){
        try {

            if(userRepository.findByUsernameOrEmail(request.getUsername()).isPresent())
                throw new RuntimeException(ErrorMessage.User.SAVE_INFORMATION);

            User user = userMapper.toUser(request);

            user.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
            user.setRole(RoleType.PATIENT);
            user.setIsVerified(false);
            user.setTokenVerified(tokenUtils.generateVerifyToken(user.getEmail()));

            userRepository.persistAndFlush(user);

            System.out.println("USER SAVED SUCCESS");

            emailService.sendVerificationEmail(
                    user.getEmail(),
                    user.getTokenVerified()
            );

            return Message.User.REGISTER;

        } catch (Exception e) {

            e.printStackTrace();

            System.out.println("ERROR = " + e.getMessage());

            throw e;
        }
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

            User user = findUser(email);

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

    @Transactional
    public String resetPassword(String email){
        User user = findUser(email);
        if(user!=null){
            String newPass = generateEasy();
            user.setPassword(BcryptUtil.bcryptHash(newPass));
            user.setIsVerified(false);
            userRepository.persist(user);
            emailService.sendResetPasswordEmail(email, newPass);
        }
        return "reset.password.success";
    }

    public String generateEasy() {
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String special = "@#$%&!?";
        String all = lower + upper + digits + special;

        SecureRandom rand = new SecureRandom();
        String pwd = ""
                + lower.charAt(rand.nextInt(lower.length()))
                + upper.charAt(rand.nextInt(upper.length()))
                + digits.charAt(rand.nextInt(digits.length()))
                + special.charAt(rand.nextInt(special.length()));

        for (int i = 0; i < 4; i++) {
            pwd += all.charAt(rand.nextInt(all.length()));
        }
        var list = pwd.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(list);

        return list.stream().map(String::valueOf).collect(Collectors.joining());
    }
}
