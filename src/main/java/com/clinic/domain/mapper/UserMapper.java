package com.clinic.domain.mapper;

import com.clinic.domain.dto.DoctorCreateRequest;
import com.clinic.domain.dto.LoginResponse;
import com.clinic.domain.dto.RegisterRequest;
import com.clinic.domain.dto.UserResponse;
import com.clinic.domain.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface UserMapper {
    LoginResponse toLoginResponse(User user);
    User toUser(RegisterRequest request);
    UserResponse toUserResponse(User user);
    User toUser(DoctorCreateRequest request);
    List<UserResponse> toUserResponses(List<User> users);
}
