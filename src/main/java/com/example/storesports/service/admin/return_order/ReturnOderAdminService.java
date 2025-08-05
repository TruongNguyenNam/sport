package com.example.storesports.service.admin.return_order;

import com.example.storesports.core.admin.return_request.request.ReturnRequestListRequest;
import com.example.storesports.core.admin.return_request.response.ReturnRequestItemResponse;
import com.example.storesports.core.admin.return_request.response.ReturnRequestListResponse;

import java.util.List;

public interface ReturnOderAdminService {
    List<ReturnRequestListResponse> returnOrderList();
     List<ReturnRequestItemResponse> finCodeReturn(String oderCode);
    List<ReturnRequestItemResponse> finCodeReturnApproved(String oderCode);
    ReturnRequestItemResponse returnResponse(ReturnRequestListRequest returnRequestListRequest,Long id, String status);
    List<ReturnRequestListResponse>  returnOrderApproved();
     List<ReturnRequestListResponse> findByCode(String code);
}
