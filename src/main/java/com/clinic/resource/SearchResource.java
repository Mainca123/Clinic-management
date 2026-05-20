package com.clinic.resource;

import com.clinic.base.RestData;
import com.clinic.service.SearchService;
import io.quarkus.security.Authenticated;
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
    public RestData<?> search(
            @QueryParam("keyword") String keyword
    ){
        return RestData.success(
                searchService.searchInfo(keyword)
        );
    }
}