package com.example.storesports.core.admin.discount.controller;

import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.core.admin.discount.payload.DiscountRequest;
import com.example.storesports.core.admin.discount.payload.DiscountResponse;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.discount.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/discount")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;
    @PostMapping("/create")
    public ResponseEntity<ResponseData<DiscountResponse>> create(@Valid @RequestBody DiscountRequest discountRequest){


        try {
            DiscountResponse discountResponse=discountService.create(discountRequest);
            return ResponseEntity.ok(ResponseData.<DiscountResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("add thành công")
                    .data(discountResponse)
                    .build());
        }
        catch (ErrorException e){
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.<DiscountResponse>builder()
                    .status(400)
                    .message(e.getMessage())
                    .data(null)
                    .build());

        }
    }
    @GetMapping
    public ResponseData<List<DiscountResponse>> getAll(){
        List<DiscountResponse> discountResponses = discountService.getAll();
        return ResponseData.<List<DiscountResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách discount thành công")
                .data(discountResponses)
                .build();
    }
    @GetMapping("/{id}")
    public DiscountResponse finByDiscountId(@PathVariable("id") Long id){
        return discountService.finByDiscountId(id);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseData<DiscountResponse>> update(@PathVariable("id") Long id, @RequestBody DiscountRequest discountRequest) {
        try {
            DiscountResponse discountResponse = discountService.update(id, discountRequest);
            return ResponseEntity.ok(
                    ResponseData.<DiscountResponse>builder()
                            .status(HttpStatus.OK.value())
                            .message("update thành công")
                            .data(discountResponse)
                            .build()
            );
        } catch (ErrorException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseData.<DiscountResponse>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("" + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<ResponseData<DiscountResponse>> updateStatus(@Valid @PathVariable("id") Long id){

        try {
            DiscountResponse discountResponse=discountService.updateStatus(id);
       return ResponseEntity.ok(
                    ResponseData.<DiscountResponse> builder().status(200)
                            .data(discountResponse)
                            .message("update status thành công")
                            .build()
                    );

        }
        catch (ErrorException e){
          return  ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseData.<DiscountResponse> builder().status(400)
                            .data(null)
                            .message(e.getMessage())
                            .build()
                    );
        }
        catch (Exception e){
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.<DiscountResponse> builder().status(500)
                            .data(null)
                            .message("lỗi hệ thống")
                            .build()
                    );
        }
    }
    @GetMapping("/finbyname/{name}")
    public  ResponseData<List<DiscountResponse>> finByName(@PathVariable("name") String name){
        List<DiscountResponse> discountResponses = discountService.finByName(name);
        return ResponseData.<List<DiscountResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("tim kiem thanh cong")
                .data(discountResponses)
                .build();
    }
    @GetMapping("/filterStatus/{discountStatus}")
    public List<DiscountResponse> filterStatus(@PathVariable("discountStatus") String discountStatus){
        return discountService.filterStatus(discountStatus);
    }
}
