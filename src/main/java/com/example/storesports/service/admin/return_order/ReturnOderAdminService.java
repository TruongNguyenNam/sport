package com.example.storesports.service.admin.return_order;

import com.example.storesports.core.admin.return_request.response.ReturnRequestItemResponse;
import com.example.storesports.core.admin.return_request.response.ReturnRequestListResponse;

import java.util.List;

public interface ReturnOderAdminService {
    List<ReturnRequestListResponse> returnOrderList();
     List<ReturnRequestItemResponse> finCodeReturn(String oderCode);
    ReturnRequestItemResponse returnResponse(Long id,String status);
//    List<ReturnRequestListResponse>  returnOrder
}
