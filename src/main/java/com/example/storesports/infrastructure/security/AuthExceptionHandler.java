package com.example.storesports.infrastructure.security;



import com.example.storesports.infrastructure.exceptions.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

   // 401 unauthorized
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String messsge = "authenication Failed";
        String detailMessage = exception.getLocalizedMessage();
        int code = 9;
        String moreInformation = "....other information";

        ErrorResponse errorResponse = new ErrorResponse(messsge,detailMessage,
                code,moreInformation,exception);

        addErrorResponseToBodyRespon(errorResponse,response,HttpServletResponse.SC_UNAUTHORIZED);
    }


    // 403 Forbiden
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
        String messsge = "Access is denied";
        String detailMessage = exception.getLocalizedMessage();
        int code = 10;
        String moreInformation = "....other information";

        ErrorResponse errorResponse = new ErrorResponse(messsge,detailMessage,
                code,moreInformation,exception);

        addErrorResponseToBodyRespon(errorResponse,response,HttpServletResponse.SC_FORBIDDEN);


    }

    private void addErrorResponseToBodyRespon (
            ErrorResponse errorResponse,
            HttpServletResponse response,
            int responsestatus) throws IOException{
        // convert objcet to json
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(errorResponse);

        //return json
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(responsestatus);
        response.getWriter().write(json);

    }



}
