package com.example.storesports.service.client.returnoder;

import com.example.storesports.core.client.returnoder.payload.request.return_request.ReturnRequestRequest;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderDetailResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_request.ReturnRequestResponse;

import java.util.List;

public interface ReturnOderService {
List<ReturnOderResponse> finAll();
ReturnOderDetailResponse finDetail(String oderCode);
ReturnRequestResponse createReturnRequest(ReturnRequestRequest returnRequestRequest);
    List<ReturnRequestResponse> getAllReturn();
}
