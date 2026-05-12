package com.clinic.resource;


import com.clinic.base.RestData;
import com.clinic.domain.dto.PasswordRequest;
import com.clinic.domain.dto.UserUpdateRequest;
import com.clinic.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;


import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "User", description = "Quản lý thông tin người dùng")
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    @GET
    @Path("/me")
    @Operation(
            summary = "Lấy thông tin người dùng hiện tại",
            description = "API dùng để lấy thông tin cá nhân từ JWT token"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lấy thông tin người dùng thành công"
    )
    public RestData<?> getCurrentUser() {
        String identifier = getIdentifierFromToken();
        return RestData.success(userService.getUserDetail(identifier));

    }

    @PATCH
    @Operation(
            summary = "Cập nhật thông tin cá nhân",
            description = "API dùng để cập nhật thông tin của người dùng hiện tại"
    )
    @APIResponse(
            responseCode = "200",
            description = "Cập nhật thông tin thành công"
    )
    public RestData<?> updateUser(
            @Valid
            @RequestBody(
                    description = "Thông tin người dùng cần cập nhật",
                    required = true
            )
            UserUpdateRequest request) {

        String identifier = getIdentifierFromToken();
        return RestData.success(userService.updateCurrentUser(identifier, request));
    }


    @PATCH
    @Path("/me/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(
            summary = "Upload ảnh đại diện",
            description = "API dùng để tải ảnh đại diện cho người dùng hiện tại"
    )
    @APIResponse(
            responseCode = "200",
            description = "Upload avatar thành công"
    )
    public RestData<?> uploadAvatar(

            @Parameter(
                    description = "File ảnh đại diện cần upload"
            )
            @RestForm("file") FileUpload file
    ) {
        String identifier = getIdentifierFromToken();
        return RestData.success(userService.uploadAvatar(identifier, file));
    }


    @PATCH
    @Path("/password")
    @Operation(
            summary = "Đổi mật khẩu",
            description = "API dùng để thay đổi mật khẩu của người dùng hiện tại"
    )
    @APIResponse(
            responseCode = "200",
            description = "Đổi mật khẩu thành công"
    )
    @APIResponse(
            responseCode = "400",
            description = "Mật khẩu cũ không chính xác"
    )
    public RestData<?> changePassword(

            @Valid
            @RequestBody(
                    description = "Thông tin đổi mật khẩu",
                    required = true
            )
            PasswordRequest request) {

        String identifier = getIdentifierFromToken();
        return RestData.success(userService.changePassword(identifier, request));
    }


    private String getIdentifierFromToken() {
        if (jwt.getSubject() != null) {
            return jwt.getSubject();
        }
        return jwt.getClaim("upn");
    }
}