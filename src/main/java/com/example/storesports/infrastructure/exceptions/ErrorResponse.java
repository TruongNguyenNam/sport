package com.example.storesports.infrastructure.exceptions;


import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ErrorResponse {
//    @NonNull
    private String message;
//    @NonNull
    private String detailMessage;
  //  @NonNull
    private Integer code;
 //   @NonNull
    private String moreInformation;

    private Exception exception;

//    public ErrorResponse(@NonNull String message, @NonNull String detailMessage, @NonNull Integer code, Exception exception) {
//        this.message = message;
//        this.detailMessage = detailMessage;
//        this.code = code;
//        this.exception = exception;
//    }
}
