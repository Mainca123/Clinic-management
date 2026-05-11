package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.domain.dto.DepartmentRequest;
import com.clinic.service.DepartmentService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("/departments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DepartmentResource {

    @Inject
    private DepartmentService departmentService;

    @POST
    @RolesAllowed(RoleType.Constants.ADMIN)
    public RestData<?> createDepartment(@RequestBody DepartmentRequest departmentRequest){
        return RestData.success(departmentService.createDepartment(departmentRequest));
    }
}
