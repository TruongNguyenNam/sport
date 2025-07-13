package com.example.storesports.service.client.returnoder;

import com.example.storesports.core.client.returnoder.payload.request.return_request.ReturnRequestItemRequest;
import com.example.storesports.core.client.returnoder.payload.request.return_request.ReturnRequestRequest;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderDetailResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnProductResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_request.ReturnRequestItemResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_request.ReturnRequestResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.constant.OrderStatus;
import com.example.storesports.infrastructure.constant.ReturnRequestItemStatus;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnOderServiceIlm implements ReturnOderService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnRequestItemRepository returnRequestItemRepository;

    @Override
    public List<ReturnOderResponse> finAll() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepository.findUserByUsername(user).orElseThrow(() -> new ErrorException("ko có user này"));
        List<Order> orders = orderRepository.findByUserAndStatuses(user1.getId(), List.of(OrderStatus.COMPLETED, OrderStatus.SHIPPED));
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ReturnOderDetailResponse finDetail(String oderCode) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepository.findUserByUsername(user).orElseThrow(() -> new ErrorException("ko có user này"));
        Order order = orderRepository.findOrderByUserAndCodeAndStatuses(user1.getId(), oderCode, List.of(OrderStatus.SHIPPED)).orElseThrow(() -> new ErrorException("ko co oder code này"));
        return maptoReturnOderDetailResponse(order);
    }

    @Override
    public ReturnRequestResponse createReturnRequest(ReturnRequestRequest returnRequestRequest) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepository.findUserByUsername(user).orElseThrow(() -> new ErrorException("ko có user này"));
        Order order=orderRepository.findById(returnRequestRequest.getOrderId()).orElseThrow(()->new ErrorException("ko có oder nay "));

        ReturnRequest returnRequest=new ReturnRequest();

        returnRequest.setNote(returnRequestRequest.getNote());
        returnRequest.setOrder(order);
        returnRequest.setUser(user1);
        returnRequest.setNote(returnRequestRequest.getNote());
        returnRequest =returnRequestRepository.save(returnRequest);
        List<ReturnRequestItem> items = new ArrayList<>();
        for (ReturnRequestItemRequest returnRequestItemRequest:returnRequestRequest.getItems()){
            OrderItem orderItem = orderItemRepository.findById(returnRequestItemRequest.getOrderItemId())
                    .orElseThrow(() -> new ErrorException("Không tìm thấy orderItem"));
            boolean exists = returnRequestItemRepository
                    .existsByOrderItemAndUserAndStatusNotAndDeletedFalse(
                            orderItem.getId(),
                            user1.getId(),
                            ReturnRequestItemStatus.REJECTED
                    );

            if (exists) {
                throw new ErrorException("Sản phẩm : "+orderItem.getProduct().getName()+" này đã gửi yêu cầu hoàn hàng vui lòng đợi phản hồi");
            }
            ReturnRequestItem item = new ReturnRequestItem();
            item.setReturnRequest(returnRequest);
            item.setOrderItem(orderItem);
            item.setQuantity(returnRequestItemRequest.getQuantity());
            item.setReason(returnRequestItemRequest.getReason());
            item.setNote(returnRequestItemRequest.getNote());
            item.setStatus(ReturnRequestItemStatus.PENDING);
            item.setDeleted(false);
            items.add(item);
        }
        returnRequest.setItems(items);
        returnRequestRepository.save(returnRequest);
        returnRequestItemRepository.saveAll(items);
        return mapToReturnRequestResponse(returnRequest);
    }

    @Override
    public List<ReturnRequestResponse> getAllReturn() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepository.findUserByUsername(user).orElseThrow(() -> new ErrorException("ko có user này"));
        return null;
    }

    public ReturnRequestResponse mapToReturnRequestResponse(ReturnRequest returnRequest) {
        ReturnRequestResponse returnRequestResponse=new ReturnRequestResponse();
        returnRequestResponse.setNote(returnRequest.getNote());
        returnRequestResponse.setRequestDate(returnRequest.getRequestDate());
        returnRequestResponse.setUserId(returnRequest.getUser().getId());
        returnRequestResponse.setOrderId(returnRequest.getOrder().getId());

        returnRequestResponse.setRequestDate(returnRequest.getRequestDate());



        List<ReturnRequestItemResponse> returnRequestItemResponses=new ArrayList<>();
        for (ReturnRequestItem returnRequestItem:returnRequest.getItems()){
            ReturnRequestItemResponse returnRequestItemResponse=new ReturnRequestItemResponse();
            returnRequestItemResponse.setReason(returnRequestItem.getReason());
            returnRequestItemResponse.setNote(returnRequestItem.getNote());
            returnRequestItemResponse.setQuantity(returnRequestItem.getQuantity());
            returnRequestItemResponse.setOrderItemId(returnRequestItem.getOrderItem().getId());
            if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.PENDING)){
                returnRequestItemResponse.setStatus("chờ phản hồi");
            }
            else if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.APPROVED)){
                returnRequestItemResponse.setStatus("đã được chấp nhận");
            }
            else if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.REJECTED)){
                returnRequestItemResponse.setStatus("đã bị từ chối");
            }
            returnRequestItemResponses.add(returnRequestItemResponse);


        }

        returnRequestResponse.setItems(returnRequestItemResponses);
        return returnRequestResponse;
    }

    public ReturnOderDetailResponse maptoReturnOderDetailResponse(Order order) {
        ReturnOderDetailResponse response = new ReturnOderDetailResponse();
        response.setOderId(order.getId());
        response.setCode(order.getOrderCode());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getOrderStatus().name());
        response.setPaymentMethod(order.getPayments().get(0).getPaymentMethod().getName());
//        response.setNote(order.getNodes());
        response.setReceiverName(order.getUser().getUsername());
        response.setReceiverPhone(order.getUser().getPhoneNumber());
        List<UserAddressMapping> addressMappings = order.getUser().getUserAddressMappings();
        if (addressMappings != null && !addressMappings.isEmpty()) {
            Address address = addressMappings.get(0).getAddress();
            if (address != null) {
                String fullAddress = String.join(", ",
                        address.getStreet(),
                        address.getWard(),
                        address.getDistrict(),
                        address.getProvince()
                );
                response.setShippingAddress(fullAddress);
            }
        }
        List<ReturnProductResponse> productResponses = new ArrayList<>();

        double totalOriginal = 0.0;
        double totalDiscount = 0.0;
        for (OrderItem item : order.getOrderItems()) {

            Product product = item.getProduct();
            Map<String, String> attributes = new HashMap<>();
            double itemTotal = product.getOriginalPrice() == null ? product.getPrice() : product.getOriginalPrice() * item.getQuantity();
            totalOriginal += itemTotal;

            if (product != null && product.getProductAttributeValues() != null) {
                for (ProductAttributeValue pav : product.getProductAttributeValues()) {
                    if (pav.getAttribute() != null && pav.getDeleted() == null) {
                        attributes.put(pav.getAttribute().getName(), pav.getValue());
                    }
                }
            } else {
                throw new ErrorException("bạn chưa có đơn hàng nào");
            }


            String imageUrl = null;
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                imageUrl = product.getImages().get(0).getImageUrl();
            }
            ReturnProductResponse productResponse = new ReturnProductResponse();
            productResponse.setOrderItemId(item.getId());
            productResponse.setProductId(item.getProduct().getId());
            productResponse.setProductName(item.getProduct().getName());
            productResponse.setImageUrl(imageUrl);
            productResponse.setQuantity(item.getQuantity());
            productResponse.setAttributes(attributes);
            productResponse.setUnitPrice(item.getUnitPrice());

            productResponses.add(productResponse);
        }
        response.setTotalAmount(order.getOrderTotal());
        response.setProductDetails(productResponses);
        return response;
//        List<OrderItem> orderItems = order.getOrderItems();
//        for (OrderItem orderItem : orderItems) {
//            Product product = orderItem.getProduct();
//            double itemOriginalPrice = product.getOriginalPrice()==null?product.getPrice():product.getOriginalPrice() * orderItem.getQuantity();
//
//            if (product != null && product.getProductDiscountMappings() != null) {
//                for (ProductDiscountMapping productDiscountMapping : product.getProductDiscountMappings()) {
//                    Discount discount = productDiscountMapping.getDiscount();
//                    LocalDateTime orderDateTime = order.getOrderDate()
//                            .toInstant()
//                            .atZone(ZoneId.systemDefault())
//                            .toLocalDateTime();
//
//                    if (discount != null
//                            && discount.getStatus().equals(DiscountStatus.ACTIVE)
//                            && discount.getStartDate() != null && discount.getEndDate() != null
//                            && !discount.getStartDate().isAfter(orderDateTime)
//                            && !discount.getEndDate().isBefore(orderDateTime)) {
//                        double percent = discount.getDiscountPercentage();
//                        totalDiscount += itemOriginalPrice * (percent / 100.0);
//                        break;
//                    }
//                }
//            }
//        }
//
    }


    public ReturnOderResponse mapToResponse(Order order) {

        List<ReturnProductResponse> productResponses = new ArrayList<>();

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            Map<String, String> attributes = new HashMap<>();


            if (product != null && product.getProductAttributeValues() != null) {
                for (ProductAttributeValue pav : product.getProductAttributeValues()) {
                    if (pav.getAttribute() != null && pav.getDeleted()==null) {
                        attributes.put(pav.getAttribute().getName(), pav.getValue());
                    }
                }
            }
            else {
                throw new ErrorException("bạn chưa có đơn hàng nào");
            }


            String imageUrl = null;
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                imageUrl = product.getImages().get(0).getImageUrl();
            }

            ReturnProductResponse productResponse= new ReturnProductResponse();

                productResponse.setProductName(item.getProduct().getName());
                productResponse.setImageUrl(imageUrl);
                productResponse.setQuantity(item.getQuantity());
                productResponse.setAttributes(attributes);
                productResponse.setUnitPrice(item.getUnitPrice());
                productResponses.add(productResponse);
            }



        ReturnOderResponse returnOderResponse = new ReturnOderResponse();
        returnOderResponse.setCode(order.getOrderCode());
        returnOderResponse.setStatus(order.getOrderStatus().name());
        returnOderResponse.setOrderDate(order.getOrderDate());
        returnOderResponse.setOrderTotal(order.getOrderTotal());
        returnOderResponse.setProductResponses(productResponses);
        return returnOderResponse;
    }
}
