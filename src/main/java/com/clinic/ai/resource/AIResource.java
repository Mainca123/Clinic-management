package com.clinic.ai.resource;

import com.clinic.ai.dto.ChatRequest;
import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.service.AIService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/ai")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AIResource {

    @Inject
    AIService aiService;

    @POST
    @Path("/chat")
    public ChatResponse chat(@Valid ChatRequest request) {
        return aiService.chat(request);
    }
}
