package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.DoctorCreateRequest;
import com.clinic.domain.dto.DoctorUpdateRequest;
import com.clinic.service.DoctorService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/doctors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DoctorResource {

    @Inject
    private DoctorService doctorService;

    @POST
    @RolesAllowed(RoleType.Constants.ADMIN)
    public RestData<?> createDoctor(@RequestBody DoctorCreateRequest request){
        return RestData.success(doctorService.createDoctor(request));
    }

    @PATCH
    @RolesAllowed({RoleType.Constants.ADMIN})
    public RestData<?> updateDoctor(@RequestBody DoctorUpdateRequest doctorUpdateRequest){
        return RestData.success(doctorService.updateDoctor(doctorUpdateRequest));
    }
}
