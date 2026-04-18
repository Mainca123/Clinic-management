package com.clinic.domain.mapper;

import com.clinic.domain.dto.LoginResponse;
import com.clinic.domain.dto.RegisterRequest;
import com.clinic.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface UserMapper {
    LoginResponse toLoginResponse(User user);
    User toUser(RegisterRequest request);
}
