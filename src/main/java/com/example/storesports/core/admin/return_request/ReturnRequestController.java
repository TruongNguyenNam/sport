package com.example.storesports.core.admin.return_request;

import com.example.storesports.core.admin.return_request.request.ReturnRequestListRequest;
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
    public ReturnRequestItemResponse returnResponse(@RequestBody ReturnRequestListRequest returnRequestListRequest, @PathVariable("id") Long id, @RequestParam String status){
       return returnOderAdminService.returnResponse(returnRequestListRequest,id,status);
    }
    @GetMapping("/return-order-approved")
    public List<ReturnRequestListResponse> returnOrderApproved(){
        return returnOderAdminService.returnOrderApproved();
    }
    @GetMapping("/fin-Code-Return-Approved/{code}")
    public List<ReturnRequestItemResponse> finCodeReturnApproved(@PathVariable("code") String oderCode){
        return returnOderAdminService.finCodeReturnApproved(oderCode);
    }
    @GetMapping("/fin-code-approved/{code}")
    public List<ReturnRequestListResponse> findByCode(@PathVariable("code") String code){
        return returnOderAdminService.findByCode(code);
    }
}
