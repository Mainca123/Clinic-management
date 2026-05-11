package com.clinic.resource;


import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.AvatarUploadRequest;
import com.clinic.domain.dto.PasswordRequest;
import com.clinic.domain.dto.UserUpdateRequest;
import com.clinic.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    @GET
    @Path("/me")
    public RestData<?> getCurrentUser() {
        String identifier = getIdentifierFromToken();
        return RestData.success(userService.getUserDetail(identifier));

    }

    @PATCH
    public RestData<?> updateUser(@Valid UserUpdateRequest request) {
        String identifier = getIdentifierFromToken();
        return RestData.success(userService.updateCurrentUser(identifier, request));
    }


    @PATCH
    @Path("/me/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public RestData<?> uploadAvatar(
            @RestForm("file") FileUpload file
    ) {
        String identifier = getIdentifierFromToken();
        return RestData.success(userService.uploadAvatar(identifier, file));
    }


    @PATCH
    @Path("/password")
    public RestData<?> changePassword(@Valid PasswordRequest request) {
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