package com.clinic.ai.assistant.handler;

import com.clinic.ai.dto.ChatResponse;
import com.clinic.ai.dto.IntentResult;
import com.clinic.ai.tool.DoctorTool;
import com.clinic.domain.dto.DepartmentResponse;
import com.clinic.domain.dto.DoctorResponse;
import com.clinic.service.DepartmentService;
import com.clinic.service.DoctorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class FindDoctorHandler {

    @Inject
    DepartmentService departmentService;

    @Inject
    DoctorService doctorService;

    @Inject
    DoctorTool doctorTool;

    public ChatResponse handle(IntentResult result) {

        String symptoms = "";

        if (result.getParams() != null) {
            Object value = result.getParams().get("symptoms");
            if (value != null) {
                symptoms = value.toString();
            }
        }

        List<DepartmentResponse> departments =
                departmentService.getAllForAI();

        String department =
                doctorTool.inferDepartment(symptoms, departments);

        List<DoctorResponse> doctors =
                doctorService.findByDepartment(department);

        return doctorTool.buildResponse(
                symptoms,
                department,
                doctors
        );
    }

}