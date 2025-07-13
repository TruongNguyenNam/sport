package com.example.storesports.core.client.returnoder.controller;

import com.example.storesports.core.admin.discount.payload.DiscountResponse;
import com.example.storesports.core.client.returnoder.payload.request.return_request.ReturnRequestRequest;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderDetailResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_request.ReturnRequestResponse;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.returnoder.ReturnOderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client/returnorder")
@RequiredArgsConstructor
public class ReturnOrderController {
    private final ReturnOderService returnOderService;
    @GetMapping
    public List<ReturnOderResponse> finAll(){
        return returnOderService.finAll();
    }
    @GetMapping("/finDetail/{code}")
    ReturnOderDetailResponse finDetail(@PathVariable("code") String oderCode){
        return returnOderService.finDetail(oderCode);
    }
    @PostMapping("/create_return_oder")
    ResponseEntity<ResponseData<ReturnRequestResponse>> createReturnRequest(@Valid @RequestBody ReturnRequestRequest returnRequestRequest){
         try {
            ReturnRequestResponse returnRequestResponse= returnOderService.createReturnRequest(returnRequestRequest);
            return ResponseEntity.ok(ResponseData.<ReturnRequestResponse>builder().message("gửi hoàn hàng thành công đợi phản hồi")
                    .data(returnRequestResponse)
                    .status(200).build());

         }
         catch (ErrorException e){
             return  ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body(ResponseData.<ReturnRequestResponse> builder().status(400)
                             .data(null)
                             .message(e.getMessage())
                             .build()
                     );
         }
         catch (Exception e){
             return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body(ResponseData.<ReturnRequestResponse> builder().status(500)
                             .data(null)
                             .message("lỗi hệ thống")
                             .build()
                     );
         }

    }
}
