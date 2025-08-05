package com.example.storesports.service.admin.return_order;

import com.example.storesports.core.admin.return_media.payload.ReturnMediaAdminResponse;
import com.example.storesports.core.admin.return_request.request.ReturnRequestListRequest;
import com.example.storesports.core.admin.return_request.response.ReturnRequestItemResponse;
import com.example.storesports.core.admin.return_request.response.ReturnRequestListResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_history.ReturnHistoryItemResponse;
import com.example.storesports.core.client.returnoder.return_media.payload.ReturnMediaResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.constant.OrderStatus;
import com.example.storesports.infrastructure.constant.ReturnRequestItemStatus;
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

    @Override
    public List<ReturnRequestListResponse> returnOrderList() {
        List<ReturnRequest> returnRequestList = returnRequestRepository.findAll();
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
                item.setAdminNote(returnRequestListRequest.getAdminNote());
                item.setStatus(ReturnRequestItemStatus.REJECTED);
            }
            if(status.equals(ReturnRequestItemStatus.RETURNED_TO_STOCK.name())){
                ReturnRequest returnRequest=item.getReturnRequest();

                OrderItem orderItem=item.getOrderItem();

                Product product=orderItem.getProduct();


                //lấy hóa đơn cha
                Order order=orderItem.getOrder();


                double priceReturn=item.getQuantity()*orderItem.getUnitPrice();
                product.setStockQuantity(product.getStockQuantity()+item.getQuantity());
                if(order.getOrderTotal()!=null){
                    order.setOrderTotal(order.getOrderTotal()-priceReturn);
                }

                else{
                   throw  new ErrorException("OrderTotal null");
                }
                item.setStatus(ReturnRequestItemStatus.RETURNED_TO_STOCK);
                orderItem.setDeleted(false);
                orderItemRepository.save(orderItem);
                returnRequestItemRepository.save(item);

                Long countReturnOrder=returnRequestRepository.countByCodeAndStatus(returnRequest.getCode(),ReturnRequestItemStatus.RETURNED_TO_STOCK);
                System.out.println("đơn hoàn có trạng thái .. stock "+countReturnOrder);
                Long countOrderItem=orderItemRepository.countOderItem(orderItem.getOrder().getOrderCode());
                System.out.println("số lượng đơn hàng đặt "+countOrderItem);
                //kiểm tra xem đơn hoàn có sâttus ...stock có bằng với sản phẩm con ko nếu bằng thì chuyển trạng thái
                if(countReturnOrder.equals(countOrderItem)){
                    order.setOrderStatus(OrderStatus.RETURNED);
                }
                productRepository.save(product);
                orderRepository.save(order);
            }
            if(status.equals(ReturnRequestItemStatus.DISCARDED.name())){
                OrderItem orderItem=item.getOrderItem();
                Order order=orderItem.getOrder();
                double priceReturn=item.getQuantity()*orderItem.getUnitPrice();
                item.setStatus(ReturnRequestItemStatus.DISCARDED);
                returnRequestItemRepository.save(item);
                order.setTotalRefund(priceReturn);
                orderRepository.save(order);
            }
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
            response.setStatus("đợi hoàn tiền");
        }
        else if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.DISCARDED)){
            response.setStatus("đợi hoàn tiền");
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
