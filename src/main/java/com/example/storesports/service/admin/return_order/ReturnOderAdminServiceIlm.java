package com.example.storesports.service.admin.return_order;

import com.example.storesports.core.admin.return_media.payload.ReturnMediaAdminResponse;
import com.example.storesports.core.admin.return_request.response.ReturnRequestItemResponse;
import com.example.storesports.core.admin.return_request.response.ReturnRequestListResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_history.ReturnHistoryItemResponse;
import com.example.storesports.core.client.returnoder.return_media.payload.ReturnMediaResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.constant.ReturnRequestItemStatus;
import com.example.storesports.infrastructure.email.EmailService;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.image.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnOderAdminServiceIlm implements ReturnOderAdminService{
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnRequestItemRepository returnRequestItemRepository;
    private final CloudinaryService cloudinaryService;
    private final ReturnMediaRepository returnMediaRepository;
    private final ProductRepository productRepository;
    private final EmailService service;

    @Override
    public List<ReturnRequestListResponse> returnOrderList() {
        List<ReturnRequest> returnRequestList = returnRequestRepository.findAll();
        return returnRequestList.stream().map(this::mapToReturnRequestListResponse).collect(Collectors.toList());
    }
    public List<ReturnRequestItemResponse> finCodeReturn(String oderCode) {
        List<ReturnRequestItem> returnRequestItems=returnRequestItemRepository.findByReturnRequestCode(oderCode);

        return returnRequestItems.stream().map(this::mapToRequestItem).collect(Collectors.toList());
    }

    @Override
    public ReturnRequestItemResponse returnResponse(Long id,String status) {
        ReturnRequestItem item=returnRequestItemRepository.findById(id).orElseThrow(()->new ErrorException("ko có đơn hàng hoàn hàng này"));
        System.out.println("dau "+item.getStatus());
        System.out.println(status);
        if(status!=null&&!status.isEmpty()){
            System.out.println("day r");
            if(status.equals(ReturnRequestItemStatus.APPROVED.name())){

                item.setStatus(ReturnRequestItemStatus.APPROVED);
                service.sendReturnRequestStatusEmail(item.getReturnRequest().getUser().getEmail(),
                        item.getReturnRequest().getUser().getUsername(),
                        item.getReturnRequest().getOrder().getOrderCode(),
                        item.getReturnRequest().getCode(),
                        "APPROVED",
                        null,
                        "công ty shoesport Hoàng Mai Hà Nội",
                        "0982929518",
                        "Trương Quang Tuấn Khanh",
                        "shoeSport"

                        );

                System.out.println(item.getStatus());
            }
            if(status.equals(ReturnRequestItemStatus.REJECTED.name())){
                item.setStatus(ReturnRequestItemStatus.REJECTED);
            }

        }
        ReturnRequestItem returnRequestItem= returnRequestItemRepository.save(item);
      return   mapToRequestItem(returnRequestItem);
    }

    public ReturnRequestItemResponse mapToRequestItem(ReturnRequestItem returnRequestItem) {
        ReturnRequestItemResponse response = new ReturnRequestItemResponse();

        Map<String,String> map=new HashMap<>();

        Product product = returnRequestItem.getOrderItem().getProduct();

        if (product != null && product.getProductAttributeValues() != null) {
            for (ProductAttributeValue pav : product.getProductAttributeValues()) {
                if (pav.getAttribute() != null && pav.getDeleted() == null) {
                    map.put(pav.getAttribute().getName(), pav.getValue());
                }

            }
        }
        List<ReturnMediaAdminResponse> returnMediaResponses=new ArrayList<>();
        for (ReturnMedia returnMedia:returnRequestItem.getReturnMedias()){
            ReturnMediaAdminResponse returnMediaResponse=new ReturnMediaAdminResponse();
            returnMediaResponse.setReturnRequestItem(returnMedia.getReturnRequestItems().getId());
            returnMediaResponse.setType(returnMedia.getType().name());
            returnMediaResponse.setUrl(returnMedia.getUrl());
            returnMediaResponses.add(returnMediaResponse);
        }
        response.setReturnMediaAdminResponses(returnMediaResponses);
        OrderItem item=returnRequestItem.getOrderItem();
        Double unitPrice = item.getUnitPrice();
        Double totalRefundAmount = returnRequestItem.getQuantity()*unitPrice;
        response.setId(returnRequestItem.getId());
        response.setUnitPrice(unitPrice);
        response.setTotalRefundAmount(totalRefundAmount);
        response.setAttributes(map);
        Product productList=productRepository.findById(returnRequestItem.getOrderItem().getProduct().getId()).orElseThrow();
        response.setProductName(returnRequestItem.getOrderItem().getProduct().getName());
        if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.PENDING)){
            response.setStatus("Chờ phản hồi");
        }
        else if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.REJECTED)){
            response.setStatus("Bị từ chối");
        }
        else if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.APPROVED)){
            response.setStatus("Đã duyệt");
        }

        response.setImageProduct(productList.getImages().get(0).getImageUrl());
        response.setNote(returnRequestItem.getNote());
        response.setReason(returnRequestItem.getReason());
        response.setQuantity(returnRequestItem.getQuantity());
        return response;
    }

    public ReturnRequestListResponse mapToReturnRequestListResponse(ReturnRequest returnRequest) {
        ReturnRequestListResponse returnRequestListResponse = new ReturnRequestListResponse();
        returnRequestListResponse.setUserName(returnRequest.getUser().getUsername());
        returnRequestListResponse.setRequestDate(returnRequest.getRequestDate());
        returnRequestListResponse.setNote(returnRequest.getNote());
        returnRequestListResponse.setTotalProduct(returnRequestRepository.countByCode(returnRequest.getCode()));
        returnRequestListResponse.setCode(returnRequest.getCode());
        for (ReturnRequestItem returnRequestItem : returnRequest.getItems()) {
            Product product=productRepository.findById(returnRequestItem.getOrderItem().getProduct().getId()).orElseThrow();
            returnRequestListResponse.setThumbnailUrl(product.getImages().get(0).getImageUrl());
        }
        return returnRequestListResponse;
    }
}
