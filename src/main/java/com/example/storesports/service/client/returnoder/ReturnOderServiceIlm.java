package com.example.storesports.service.client.returnoder;

import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.client.returnoder.payload.request.return_request.ReturnRequestItemRequest;
import com.example.storesports.core.client.returnoder.payload.request.return_request.ReturnRequestRequest;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderDetailResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnProductResponse;
import com.example.storesports.core.client.returnoder.payload.response.ReturnOderResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_history.ReturnHistoryItemResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_history.ReturnHistoryResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_request.ReturnRequestItemResponse;
import com.example.storesports.core.client.returnoder.payload.response.return_request.ReturnRequestResponse;
import com.example.storesports.core.client.returnoder.return_media.payload.ReturnMediaRequest;
import com.example.storesports.core.client.returnoder.return_media.payload.ReturnMediaResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.constant.MediaStatus;
import com.example.storesports.infrastructure.constant.OrderStatus;
import com.example.storesports.infrastructure.constant.ReturnRequestItemStatus;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.image.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final CloudinaryService cloudinaryService;
    private final ReturnMediaRepository returnMediaRepository;
    private final ProductRepository productRepository;
    @Override
    public List<ReturnOderResponse> finAll() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepository.findUserByUsername(user).orElseThrow(() -> new ErrorException("ko có user này"));
        List<Order> orders = orderRepository.findByUserAndStatuses(user1.getId(), List.of(OrderStatus.COMPLETED));
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ReturnOderDetailResponse finDetail(String oderCode) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepository.findUserByUsername(user).orElseThrow(() -> new ErrorException("ko có user này"));
        Order order = orderRepository.findOrderByUserAndCodeAndStatuses(user1.getId(), oderCode, List.of(OrderStatus.COMPLETED)).orElseThrow(() -> new ErrorException("ko co oder code này"));
        return maptoReturnOderDetailResponse(order);
    }
    @Override
    public ReturnRequestResponse createReturnRequest(MultipartFile[] file, ReturnRequestRequest returnRequestRequest) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ErrorException("Không tìm thấy người dùng"));

        Order order = orderRepository.findById(returnRequestRequest.getOrderId())
                .orElseThrow(() -> new ErrorException("Không tìm thấy đơn hàng"));

        for (ReturnRequestItemRequest itemRequest : returnRequestRequest.getItems()) {
            OrderItem orderItem = orderItemRepository.findById(itemRequest.getOrderItemId())
                    .orElseThrow(() -> new ErrorException("Không tìm thấy orderItem"));
            Integer quantityOrderItem = orderItem.getQuantity();
            Integer sumReturnRequest=returnRequestItemRepository.sumQuantityByOrderItemIdAndStatusNot(orderItem.getId(),ReturnRequestItemStatus.REJECTED);
            if (sumReturnRequest == null) {
                sumReturnRequest = 0;
            }
            int totalRequested = sumReturnRequest + itemRequest.getQuantity();
            if (totalRequested > quantityOrderItem) {
                throw new ErrorException("Bạn không được gửi sản phẩm hoàn quá số lượng sản phẩm đã đặt");
            }

//            boolean exists = returnRequestItemRepository
//                    .existsByOrderItemAndUserAndStatusNotAndDeletedFalse(
//                            orderItem.getId(),
//                            user.getId(),
//                            ReturnRequestItemStatus.REJECTED
//                    );
//
//            if (exists) {
//                throw new ErrorException("Sản phẩm: " + orderItem.getProduct().getName() + " đã gửi yêu cầu hoàn hàng. Vui lòng đợi phản hồi.");
//            }
        }
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setCode(generateShortReturnCode());
        returnRequest.setOrder(order);
        returnRequest.setUser(user);
        returnRequest.setNote(returnRequestRequest.getNote());
        returnRequest.setBankAccountNumber(returnRequestRequest.getBankAccountNumber());
        returnRequest.setBankAccountName(returnRequestRequest.getBankAccountName());
        returnRequest.setBankName(returnRequestRequest.getBankName());

        returnRequest = returnRequestRepository.save(returnRequest);

        List<ReturnRequestItem> items = new ArrayList<>();

        for (ReturnRequestItemRequest itemRequest : returnRequestRequest.getItems()) {
            OrderItem orderItem = orderItemRepository.findById(itemRequest.getOrderItemId())
                    .orElseThrow(() -> new ErrorException("Không tìm thấy orderItem"));

            ReturnRequestItem item = new ReturnRequestItem();
            item.setReturnRequest(returnRequest);
            item.setOrderItem(orderItem);
            item.setQuantity(itemRequest.getQuantity());

            item.setReason(itemRequest.getReason());
            item.setNote(itemRequest.getNote());
            item.setStatus(ReturnRequestItemStatus.PENDING);
            item.setDeleted(false);

            item = returnRequestItemRepository.save(item);
            List<ReturnMedia> returnMedia = uploadVideoOrImage(file, itemRequest.getMediaRequests(), item);

            item.setReturnMedias(returnMedia);

            items.add(item);
        }

        returnRequest.setItems(items);
        returnRequestRepository.save(returnRequest);
        returnRequestItemRepository.saveAll(items);

        return mapToReturnRequestResponse(returnRequest);
    }

    @Override
    public List<ReturnHistoryResponse> getAllReturn() {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepository.findUserByUsername(user).orElseThrow(() -> new ErrorException("ko có user này"));

        List<ReturnRequest> rq = returnRequestRepository.findByUserName(user1.getUsername());
        return rq.stream().map(this::mapToHistory).collect(Collectors.toList());
    }

    @Override
    public List<ReturnHistoryItemResponse> finHistory(String oderCode) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        User user1 = userRepository.findUserByUsername(user).orElseThrow(() -> new ErrorException("ko có user này"));
        List<ReturnRequestItem> returnRequestItems=returnRequestItemRepository.findByReturnRequestCodeAndUserName(oderCode,user1.getUsername());

        return returnRequestItems.stream().map(this::mapToHistoryItem).collect(Collectors.toList());
    }
    public ReturnHistoryItemResponse mapToHistoryItem(ReturnRequestItem returnRequestItem) {
        ReturnHistoryItemResponse returnHistoryItemResponse = new ReturnHistoryItemResponse();

        Map<String,String> map=new HashMap<>();

            Product product = returnRequestItem.getOrderItem().getProduct();

                if (product != null && product.getProductAttributeValues() != null) {
                    for (ProductAttributeValue pav : product.getProductAttributeValues()) {
                        if (pav.getAttribute() != null && pav.getDeleted() == null) {
                            map.put(pav.getAttribute().getName(), pav.getValue());
                        }

                }
            }
                OrderItem item=returnRequestItem.getOrderItem();
        Double unitPrice = item.getUnitPrice();
        Double totalRefundAmount = returnRequestItem.getQuantity()*unitPrice;

        returnHistoryItemResponse.setUnitPrice(unitPrice);
        returnHistoryItemResponse.setTotalRefundAmount(totalRefundAmount);




        returnHistoryItemResponse.setAttributes(map);
        Product productList=productRepository.findById(returnRequestItem.getOrderItem().getProduct().getId()).orElseThrow();
        returnHistoryItemResponse.setProductName(returnRequestItem.getOrderItem().getProduct().getName());
        if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.PENDING)){
            returnHistoryItemResponse.setStatus("Chờ phản hồi");
        }
        else if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.REJECTED)){
            returnHistoryItemResponse.setStatus("Bị từ chối");
        }
        else if(returnRequestItem.getStatus().equals(ReturnRequestItemStatus.APPROVED)){
            returnHistoryItemResponse.setStatus("Đã duyệt");
        } else if (returnRequestItem.getStatus().equals(ReturnRequestItemStatus.RETURNED_TO_STOCK)) {
            returnHistoryItemResponse.setStatus("đợi hoàn tiền");

        } else if (returnRequestItem.getStatus().equals(ReturnRequestItemStatus.DISCARDED)) {
            returnHistoryItemResponse.setStatus("Đợi hoàn tiền");

        }
        else{
            returnHistoryItemResponse.setStatus("Đã hoàn tiền");
        }

        returnHistoryItemResponse.setImageProduct(productList.getImages()==null||productList.getImages().isEmpty()?"":productList.getImages().get(0).getImageUrl());
        returnHistoryItemResponse.setNote(returnRequestItem.getNote());
        returnHistoryItemResponse.setReason(returnRequestItem.getReason());
        returnHistoryItemResponse.setQuantity(returnRequestItem.getQuantity());
        return returnHistoryItemResponse;
    }


    public ReturnHistoryResponse mapToHistory(ReturnRequest returnRequest) {
        ReturnHistoryResponse returnHistoryResponse = new ReturnHistoryResponse();
        for (ReturnRequestItem returnRequestItem : returnRequest.getItems()) {
            Product product=productRepository.findById(returnRequestItem.getOrderItem().getProduct().getId()).orElseThrow();
            returnHistoryResponse.setThumbnailUrl(product.getImages()==null||product.getImages().isEmpty()?"":product.getImages().get(0).getImageUrl());
        }

        Long totalItem=returnRequestRepository.countByCode(returnRequest.getCode());
        returnHistoryResponse.setCode(returnRequest.getCode());
        returnHistoryResponse.setNote(returnRequest.getNote());
        returnHistoryResponse.setBankAccountName(returnRequest.getBankAccountName());
        returnHistoryResponse.setBankName(returnRequest.getBankName());
        returnHistoryResponse.setBankAccountNumber(returnRequest.getBankAccountNumber());
        returnHistoryResponse.setTotalProduct(totalItem);
        returnHistoryResponse.setRequestDate(returnRequest.getRequestDate());
        return returnHistoryResponse;
    }


    public ReturnRequestResponse mapToReturnRequestResponse(ReturnRequest returnRequest) {
        ReturnRequestResponse returnRequestResponse=new ReturnRequestResponse();
        returnRequestResponse.setNote(returnRequest.getNote());
        returnRequestResponse.setRequestDate(returnRequest.getRequestDate());
        returnRequestResponse.setUserId(returnRequest.getUser().getId());
        returnRequestResponse.setOrderId(returnRequest.getOrder().getId());
        returnRequest.setBankAccountNumber(returnRequest.getBankAccountNumber());
        returnRequest.setBankAccountName(returnRequest.getBankAccountName());
        returnRequest.setBankName(returnRequest.getBankName());
        returnRequestResponse.setRequestDate(returnRequest.getRequestDate());



        List<ReturnRequestItemResponse> returnRequestItemResponses=new ArrayList<>();
        List<ReturnMediaResponse> returnMedias=new ArrayList<>();
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

            for (ReturnMedia returnMedia:returnRequestItem.getReturnMedias()){
                ReturnMediaResponse returnMediaResponse=new ReturnMediaResponse();
                returnMediaResponse.setReturnRequestItemId(returnMedia.getReturnRequestItems().getId());
                returnMediaResponse.setType(returnMedia.getType().name());
                returnMediaResponse.setUrl(returnMedia.getUrl());
              returnMedias.add(returnMediaResponse);

            }

        }
        returnRequestResponse.setReturnMediaResponses(returnMedias);

        returnRequestResponse.setItems(returnRequestItemResponses);
        return returnRequestResponse;
    }

    public ReturnOderDetailResponse maptoReturnOderDetailResponse(Order order) {
        ReturnOderDetailResponse response = new ReturnOderDetailResponse();
        response.setOderId(order.getId());
        response.setCode(order.getOrderCode());
        response.setOrderDate(order.getOrderDate());
//        if (order.getOrderStatus().equals(OrderStatus.COMPLETED)){
//            response.setStatus("Hoàn Thành");
//        }
        response.setStatus(order.getOrderStatus().name());
        response.setPaymentMethod(order.getPayments().get(0).getPaymentMethod().getName());

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
        if(order.getOrderStatus().name().equals("COMPLETED")){
            returnOderResponse.setStatus("Hoàn thành");
        }

        returnOderResponse.setOrderDate(order.getOrderDate());
        returnOderResponse.setOrderTotal(order.getOrderTotal());
        returnOderResponse.setProductResponses(productResponses);
        return returnOderResponse;
    }
    public List<ReturnMedia> uploadVideoOrImage(
            MultipartFile[] files,
            List<ReturnMediaRequest> mediaDTOs,
            ReturnRequestItem returnRequestItem
    ) throws IOException {
        List<ReturnMedia> returnMedias = new ArrayList<>();
        String folder = "return-media";

        if (mediaDTOs == null || mediaDTOs.isEmpty()) {
            throw new ErrorException("Phải có ít nhất một ảnh hoặc video");
        }

        for (ReturnMediaRequest mediaDTO : mediaDTOs) {
            MultipartFile matchedFile = findFileByName(files, mediaDTO.getFileName());
            if (matchedFile == null || matchedFile.isEmpty()) {
                continue; // Không tìm thấy file thì bỏ qua
            }

            String url;
            if (mediaDTO.getType().equalsIgnoreCase("image")) {
                url = cloudinaryService.uploadFile(matchedFile, folder);
            } else if (mediaDTO.getType().equalsIgnoreCase("video")) {
                url = (String) cloudinaryService.uploadVideo(matchedFile, folder).get("secure_url");
            } else {
                continue;
            }

            ReturnMedia media = new ReturnMedia();
            media.setUrl(url);
            media.setType(MediaStatus.valueOf(mediaDTO.getType().toUpperCase()));
            media.setReturnRequestItems(returnRequestItem);
            returnMedias.add(media);
        }

        // ✅ Kiểm tra lại: nếu metadata có nhưng không có file match thực tế
        if (returnMedias.isEmpty()) {
            throw new ErrorException("File ảnh hoặc video không hợp lệ hoặc chưa được đính kèm đúng tên.");
        }

        returnMediaRepository.saveAll(returnMedias);
        return returnMedias;
    }

    private MultipartFile findFileByName(MultipartFile[] files, String fileName) {
        return Arrays.stream(files)
                .filter(f -> f.getOriginalFilename().equals(fileName))
                .findFirst()
                .orElse(null);
    }


    public String generateShortReturnCode() {
        String prefix = "RR";
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // 20250713
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomPart = new StringBuilder();
        Random rnd = new Random();

        for (int i = 0; i < 4; i++) {
            randomPart.append(chars.charAt(rnd.nextInt(chars.length())));
        }

        return prefix + datePart + randomPart;
}}

