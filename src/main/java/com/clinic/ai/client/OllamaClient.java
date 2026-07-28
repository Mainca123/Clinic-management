package com.clinic.ai.client;

import com.clinic.ai.dto.OllamaRequest;
import com.clinic.ai.dto.OllamaResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "ollama")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface OllamaClient {

    @POST
    @Path("/chat")
    OllamaResponse chat(OllamaRequest request);

}