package com.example.storesports.service.client.returnoder;

import com.example.storesports.core.client.returnoder.payload.response.ReturnOderDetailResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderResponse;

import java.util.List;

public interface ReturnOderService {
List<ReturnOderResponse> finAll();
ReturnOderDetailResponse finDetail(String oderCode);
}
