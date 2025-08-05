package com.example.storesports.core.client.returnoder.controller;

import com.example.storesports.core.admin.discount.payload.DiscountResponse;
import com.example.storesports.core.admin.return_request.response.ReturnRequestListResponse;
import com.example.storesports.core.client.returnoder.payload.request.VerifyRequest;
import com.example.storesports.core.client.returnoder.payload.request.return_request.ReturnRequestRequest;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderDetailResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_history.ReturnHistoryItemResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_history.ReturnHistoryResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_request.ReturnRequestResponse;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.returnoder.ReturnOderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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


    @PostMapping(value = "/create_return_oder", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseData<ReturnRequestResponse>> createReturnRequest(
            @RequestPart("files") MultipartFile[] files,
            @RequestPart("returnRequest") String returnRequestJson
    ) {
        try {
            // Parse thủ công JSON thành object
            ObjectMapper mapper = new ObjectMapper();
            ReturnRequestRequest returnRequestRequest = mapper.readValue(returnRequestJson, ReturnRequestRequest.class);

            ReturnRequestResponse response = returnOderService.createReturnRequest(files, returnRequestRequest);

            return ResponseEntity.ok(ResponseData.<ReturnRequestResponse>builder()
                    .status(200)
                    .message("Gửi hoàn hàng thành công, đợi phản hồi")
                    .data(response)
                    .build());

        } catch (ErrorException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseData.<ReturnRequestResponse>builder()
                            .status(400)
                            .message(e.getMessage())
                            .data(null)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.<ReturnRequestResponse>builder()
                            .status(500)
                            .message(e.getMessage())
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/history")
    public List<ReturnHistoryResponse> getAllReturn(){
       return returnOderService.getAllReturn();
    }
    @GetMapping("/history_item/{code}")
    public List<ReturnHistoryItemResponse> finHistory(@PathVariable("code") String oderCode){
        return returnOderService.finHistory(oderCode);
    }


}
