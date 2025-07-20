package com.example.storesports.service.client.returnoder;

import com.example.storesports.core.client.returnoder.payload.request.return_request.ReturnRequestRequest;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderDetailResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_history.ReturnHistoryItemResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_history.ReturnHistoryResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_request.ReturnRequestResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ReturnOderService {
List<ReturnOderResponse> finAll();
ReturnOderDetailResponse finDetail(String oderCode);
ReturnRequestResponse createReturnRequest(MultipartFile[] file,ReturnRequestRequest returnRequestRequest) throws IOException;
    List<ReturnHistoryResponse> getAllReturn();
    List<ReturnHistoryItemResponse> finHistory(String oderCode);
}
