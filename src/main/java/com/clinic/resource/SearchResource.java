package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.constant.RoleType;
import com.clinic.service.SearchService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class SearchResource {

    @Inject
    SearchService searchService;

    @GET
    @RolesAllowed({RoleType.Constants.ADMIN, RoleType.Constants.DOCTOR, RoleType.Constants.PATIENT})
    public RestData<?> search(
            @QueryParam("keyword") String keyword
    ){
        return RestData.success(
                searchService.searchInfo(keyword)
        );
    }
}