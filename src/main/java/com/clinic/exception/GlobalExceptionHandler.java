package com.clinic.exception;

import com.clinic.base.RestData;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        exception.printStackTrace(); // Giúp bạn debug trong console

        // MẶC ĐỊNH ban đầu
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        String message = exception.getMessage();

        // KIỂM TRA: Nếu là lỗi do mình chủ động bắn ra (ClinicException)
        if (exception instanceof ClinicException) {
            ClinicException clinicEx = (ClinicException) exception;
            status = clinicEx.getStatus(); // Lấy mã 400, 404... mà bạn đã truyền vào
        }

        // Đóng gói dữ liệu trả về theo chuẩn RestData của bạn
        RestData<Object> errorBody = RestData.error(message);

        return Response.status(status)
                .entity(errorBody)
                .build();
    }
}