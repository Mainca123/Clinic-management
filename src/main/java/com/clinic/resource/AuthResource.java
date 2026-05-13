package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.LoginRequest;
import com.clinic.domain.dto.RegisterRequest;
import com.clinic.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Các API xác thực người dùng")
@PermitAll
public class AuthResource {

    @Inject
    AuthService authService;

    @GET
    @Operation(
            summary = "Ping hệ thống",
            description = "API dùng để test"
    )
    @APIResponse(responseCode = "200", description = "Xác thực thành công")
    public RestData<?> hello(){
        return RestData.success("OK");
    }

    @POST
    @Path("/registration")
    @Operation(
            summary = "Đăng ký tài khoản",
            description = "API dùng để tạo tài khoản mới cho người dùng"
    )
    @APIResponse(responseCode = "200", description = "Đăng ký thành công")
    public RestData<?> register(
            @Valid
            @RequestBody(description = "Thông tin đăng ký tài khoản", required = true)
            RegisterRequest request){

        return RestData.success(authService.register(request));
    }

    @POST
    @Path("/authentication")
    @Operation(
            summary = "Đăng nhập",
            description = "API xác thực tài khoản và trả về JWT token"
    )
    @APIResponse(responseCode = "200", description = "Đăng nhập thành công")
    @APIResponse(responseCode = "401", description = "Sai tài khoản hoặc mật khẩu")
    public RestData<?> login(
            @RequestBody(description = "Thông tin đăng nhập", required = true)
            LoginRequest loginRequest){

        return RestData.success(authService.login(loginRequest));
    }

    @GET
    @Path("/verify-email")
    @Operation(
            summary = "Xác thực email",
            description = "API xác thực email người dùng thông qua token gửi qua email"
    )
    @APIResponse(responseCode = "200", description = "Xác thực email thành công")
    public RestData<?> verifyEmail(
            @Parameter(description = "Token xác thực email")
            @QueryParam("token") String token) {

        return RestData.success(authService.verifyEmail(token));
    }

    @PATCH
    @Path("/password-resets")
    @Operation(
            summary = "Đặt lại mật khẩu",
            description = "API gửi email reset mật khẩu cho người dùng"
    )
    @APIResponse(responseCode = "200", description = "Gửi email reset thành công")
    public RestData<?> resetPassword(
            @Parameter(description = "Email tài khoản cần reset mật khẩu")
            @QueryParam("email") String email) {

        return RestData.success(authService.resetPassword(email));
    }
}