package com.example.storesports.core.client.returnoder.controller;

import com.example.storesports.core.client.returnoder.payload.response.ReturnOderDetailResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderResponse;
import com.example.storesports.service.client.returnoder.ReturnOderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
