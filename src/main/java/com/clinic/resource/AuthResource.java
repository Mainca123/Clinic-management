package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.LoginRequest;
import com.clinic.domain.dto.RegisterRequest;
import com.clinic.service.AuthService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @GET
    @RolesAllowed(RoleType.Constants.ADMIN)
    public RestData<?> hello(){
        return RestData.success("OK");
    }

    @POST
    @Path("/registration")
    public RestData<?> register(@Valid RegisterRequest request){
        return RestData.success(authService.register(request));
    }

    @POST
    @Path("/authentication")
    public RestData<?> login(@RequestBody LoginRequest loginRequest){
        return RestData.success(authService.login(loginRequest));
    }

    @GET
    @Path("/verify-email")
    public RestData<?> verifyEmail(@QueryParam("token") String token) {
        return RestData.success(authService.verifyEmail(token));
    }

    @PATCH
    @Path("/password-resets")
    public RestData<?> resetPassword(@QueryParam("email") String email) {
        return RestData.success(authService.resetPassword(email));
    }
}
