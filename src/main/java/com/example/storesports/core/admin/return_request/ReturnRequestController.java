package com.example.storesports.core.admin.return_request;

import com.example.storesports.core.admin.return_request.response.ReturnRequestItemResponse;
import com.example.storesports.core.admin.return_request.response.ReturnRequestListResponse;
import com.example.storesports.service.admin.return_order.ReturnOderAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/return/request")
@RequiredArgsConstructor
public class ReturnRequestController {
    private final ReturnOderAdminService returnOderAdminService;
    @GetMapping
    List<ReturnRequestListResponse> returnOrderList(){
        return returnOderAdminService.returnOrderList();
    }
    @GetMapping("/{orderCode}")
    public List<ReturnRequestItemResponse> finCodeReturn(@PathVariable("orderCode") String oderCode){
        return returnOderAdminService.finCodeReturn(oderCode);
    }
    @PostMapping("/returnresponse/{id}")
    public ReturnRequestItemResponse returnResponse(@PathVariable("id") Long id,@RequestParam String status){
       return returnOderAdminService.returnResponse(id,status);
    }

}
