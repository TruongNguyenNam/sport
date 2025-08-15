package com.example.storesports.service.admin.return_order;

import com.example.storesports.core.admin.return_media.payload.ReturnMediaAdminResponse;
import com.example.storesports.core.admin.return_request.request.ReturnRequestListRequest;
import com.example.storesports.core.admin.return_request.response.ReturnPriceResponse;
import com.example.storesports.core.admin.return_request.response.ReturnRequestItemResponse;
import com.example.storesports.core.admin.return_request.response.ReturnRequestListResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_history.ReturnHistoryItemResponse;
import com.example.storesports.core.client.returnoder.return_media.payload.ReturnMediaResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.constant.OrderStatus;
import com.example.storesports.infrastructure.constant.ReturnRequestItemStatus;
import com.example.storesports.infrastructure.constant.ShipmentStatus;
import com.example.storesports.infrastructure.email.EmailService;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.image.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private final ShipmentRepository shipmentRepository;

    @Override
    public List<ReturnRequestListResponse> returnOrderList() {
        List<ReturnRequest> returnRequestList = returnRequestRepository.finByStatusItem(ReturnRequestItemStatus.PENDING);
        return returnRequestList.stream().map(r->mapToReturnRequestListResponse(r,true)).collect(Collectors.toList());
    }
    public List<ReturnRequestItemResponse> finCodeReturn(String oderCode) {
        List<ReturnRequestItem> returnRequestItems=returnRequestItemRepository.findByReturnRequestCode(oderCode);

        return returnRequestItems.stream().map(this::mapToRequestItem).collect(Collectors.toList());
    }

    @Override
    public List<ReturnRequestItemResponse> finCodeReturnApproved(String oderCode) {
        List<ReturnRequestItem> returnRequestItems=returnRequestItemRepository.
                findByReturnRequestCode(oderCode).stream()
                .filter(r->r.getStatus().equals(ReturnRequestItemStatus.APPROVED))
                .collect(Collectors.toList());
        return returnRequestItems.stream().map(this::mapToRequestItem).collect(Collectors.toList());
    }

    @Override
    public ReturnRequestItemResponse returnResponse(ReturnRequestListRequest returnRequestListRequest, Long id, String status) {
        ReturnRequestItem item=returnRequestItemRepository.findById(id).orElseThrow(()->new ErrorException("ko có đơn hàng hoàn hàng này"));
        System.out.println("dau "+item.getStatus());
        System.out.println(status);
        if(status!=null&&!status.isEmpty()){
            System.out.println("day r");
            if(status.equals(ReturnRequestItemStatus.APPROVED.name())){
                item.setRespondedAt(new Date());
                item.setStatus(ReturnRequestItemStatus.APPROVED);
                service.sendReturnRequestStatusEmail(item.getReturnRequest().getUser().getEmail(),
                        item.getReturnRequest().getUser().getUsername(),
                        item.getReturnRequest().getOrder().getOrderCode(),
                        item.getReturnRequest().getCode(),
                        "APPROVED",
                        null,
                        "công ty shoesport Hoàng Mai Hà Nội",
                        "0982929518",
                        "shoeSport"

                        );

                System.out.println(item.getStatus());

            }
            if(status.equals(ReturnRequestItemStatus.REJECTED.name())){
                item.setRespondedAt(new Date());
                item.setAdminNote(returnRequestListRequest.getAdminNote());
                item.setStatus(ReturnRequestItemStatus.REJECTED);
                service.sendReturnRequestStatusEmail(item.getReturnRequest().getUser().getEmail(),
                        item.getReturnRequest().getUser().getUsername(),
                        item.getReturnRequest().getOrder().getOrderCode(),
                        item.getReturnRequest().getCode(),
                        "REJECTED",
                        returnRequestListRequest.getAdminNote(),
                        "công ty shoeSport Hoàng Mai Hà Nội",
                        "0982929518", "shoeSport"

                );
            }
            if (status.equals(ReturnRequestItemStatus.RETURNED_TO_STOCK.name()) ||
                    status.equals(ReturnRequestItemStatus.DISCARDED.name())) {

                OrderItem orderItem = item.getOrderItem();
                Product product = orderItem.getProduct();
                Order order = orderItem.getOrder();

                if (order.getOrderTotal() == null) {
                    throw new ErrorException("OrderTotal null");
                }

                if (status.equals(ReturnRequestItemStatus.RETURNED_TO_STOCK.name())) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }

                item.setStatus(ReturnRequestItemStatus.valueOf(status));
                item.setApprovedAt(new Date());
                orderItem.setDeleted(false);

                orderItemRepository.save(orderItem);
                returnRequestItemRepository.save(item);
                orderRepository.save(order);
            }



//            if(status.equals(ReturnRequestItemStatus.DISCARDED.name())){
//                OrderItem orderItem=item.getOrderItem();
//                Order order=orderItem.getOrder();
//                double priceReturn=item.getQuantity()*orderItem.getUnitPrice();
//                item.setStatus(ReturnRequestItemStatus.DISCARDED);
//                item.setApprovedAt(new Date());
//                returnRequestItemRepository.save(item);
//                Long countReturnOrder=returnRequestRepository.countByOrderCodeAndStatus(order.getOrderCode(),List.of(ReturnRequestItemStatus.APPROVED,ReturnRequestItemStatus.PENDING));
//
//                if(countReturnOrder==0){
//                    order.setOrderStatus(OrderStatus.RETURNED);
//                }
//                order.setTotalRefund(priceReturn);
//
//                orderRepository.save(order);
//            }
        }
        ReturnRequestItem returnRequestItem= returnRequestItemRepository.save(item);
      return   mapToRequestItem(returnRequestItem);
    }

    @Override
    public List<ReturnRequestListResponse> returnOrderApproved() {
        List<ReturnRequest> returnRequests=returnRequestRepository.finByStatusItem(ReturnRequestItemStatus.APPROVED);
        return returnRequests.stream().map(r->mapToReturnRequestListResponse(r,false)).collect(Collectors.toList());
    }

    @Override
    public List<ReturnRequestListResponse> findByCode(String code) {
        List<ReturnRequest> returnRequest=returnRequestRepository.findByCode(code,ReturnRequestItemStatus.APPROVED);
        return returnRequest.stream().map(r->mapToReturnRequestListResponse(r,true)).collect(Collectors.toList());
    }

    @Override
    public List<ReturnPriceResponse> finReturnPrice() {
        List<ReturnRequestItem> returnRequest=returnRequestItemRepository.findByStatus(List.of(ReturnRequestItemStatus.RETURNED_TO_STOCK,ReturnRequestItemStatus.DISCARDED));
        return returnRequest.stream().map(r->mapToReturnPriceResponse(r)).collect(Collectors.toList());
    }

    @Override
    public void updateStatus(Long id) {
        ReturnRequestItem item=returnRequestItemRepository.findById(id).orElseThrow(()->new ErrorException("ko có id đơn hoàn này"));
        item.setStatus(ReturnRequestItemStatus.REFUNDED);
        OrderItem orderItem = item.getOrderItem();
        Order order = orderItem.getOrder();
        for (Shipment shipment:order.getShipments()) {
            shipment.setShipmentStatus(ShipmentStatus.RETURNED);
            shipmentRepository.save(shipment);
        }

        returnRequestItemRepository.save(item);
        double priceReturn = item.getQuantity() * orderItem.getUnitPrice();
        order.setOrderTotal(order.getOrderTotal() - priceReturn);

        Long countReturnOrder = returnRequestRepository.countByOrderCodeAndStatus(
                order.getOrderCode(),
                List.of(ReturnRequestItemStatus.REFUNDED)
        );

        Long countOrderItem = orderItemRepository.countOderItem(order.getOrderCode());

        if (countReturnOrder.equals(countOrderItem)) {
            order.setOrderStatus(OrderStatus.RETURNED);
        }
        orderRepository.save(order);



    }

    ReturnPriceResponse mapToReturnPriceResponse(ReturnRequestItem returnRequestI){
        ReturnPriceResponse returnPriceResponse = new ReturnPriceResponse();
        ReturnRequest returnRequest = returnRequestI.getReturnRequest();
        returnPriceResponse.setCode(returnRequest.getCode());


           OrderItem orderItem= returnRequestI.getOrderItem();
           Product product=orderItem.getProduct();
            returnPriceResponse.setIdReturnRequestItem(returnRequestI.getId());
            returnPriceResponse.setProductName(product.getName());
        returnPriceResponse.setUserName(returnRequest.getUser().getUsername());
        returnPriceResponse.setBankName(returnRequest.getBankName());
        returnPriceResponse.setBankAccountName(returnRequest.getBankAccountName());
        returnPriceResponse.setBankAccountNumber(returnRequest.getBankAccountNumber());
        returnPriceResponse.setTotalPrice(returnRequestI.getOrderItem().getUnitPrice());
        return returnPriceResponse;
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
        else if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.RETURNED_TO_STOCK)){
            response.setStatus("Đợi hoàn tiền");
        }
        else if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.DISCARDED)){
            response.setStatus("Đợi hoàn tiền");
        }
        else{
            response.setStatus("Đã hoàn tiền");
        }
        response.setImageProduct(productList.getImages()==null||productList.getImages().isEmpty()?"":productList.getImages().get(0).getImageUrl());
        response.setNote(returnRequestItem.getNote());
        response.setReason(returnRequestItem.getReason());
        response.setQuantity(returnRequestItem.getQuantity());
        return response;
    }

    public ReturnRequestListResponse mapToReturnRequestListResponse(ReturnRequest returnRequest,boolean onlyApprovedItems) {
        ReturnRequestListResponse returnRequestListResponse = new ReturnRequestListResponse();
        returnRequestListResponse.setUserName(returnRequest.getUser().getUsername());
        returnRequestListResponse.setRequestDate(returnRequest.getRequestDate());
        returnRequestListResponse.setNote(returnRequest.getNote());

            if(!onlyApprovedItems){
                returnRequestListResponse.setTotalProduct(returnRequestRepository.countByCodeApproved(returnRequest.getCode(),ReturnRequestItemStatus.APPROVED));
            }
            else{
                returnRequestListResponse.setTotalProduct(returnRequestRepository.countByCode(returnRequest.getCode()));
            }


        returnRequestListResponse.setCode(returnRequest.getCode());
        for (ReturnRequestItem returnRequestItem : returnRequest.getItems()) {
            Product product=productRepository.findById(returnRequestItem.getOrderItem().getProduct().getId()).orElseThrow();
            if(product.getImages()==null||product.getImages().isEmpty()){
                returnRequestListResponse.setThumbnailUrl("");
            }
            else {
                returnRequestListResponse.setThumbnailUrl(product.getImages().get(0).getImageUrl());
            }
        }
        return returnRequestListResponse;
    }
}
