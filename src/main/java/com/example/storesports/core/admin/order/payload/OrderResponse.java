package com.example.storesports.core.admin.order.payload;

import com.example.storesports.core.admin.orderItem.payload.OrderItemResponse;
import com.example.storesports.core.admin.payment.payload.PaymentResponse;
import com.example.storesports.infrastructure.constant.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderCode;
    private AddressResponse address; // Thông tin địa chỉ giao hàng (nếu không phải đơn hàng tại quầy)
    private String orderStatus;
    private Double orderTotal; // Tổng đơn hàng
    private Boolean isPos; // Đơn hàng tại quầy (true)
    private Boolean Deleted; // Đơn hàng có bị xóa mềm không
    private Date orderDate; // Ngày tạo đơn hàng
    private List<OrderItemResponse> items; // Danh sách sản phẩm trong đơn hàng
    private PaymentResponse payment; // Thông tin thanh toán
    private List<CouponResponse> couponUsages; // Danh sách thông tin sử dụng coupon
    private ShipmentResponse shipment; // Thông tin vận chuyển (nếu có)
    private Integer createdBy; // Người tạo đơn hàng
    private LocalDateTime createdDate; // Thời gian tạo
    private Integer lastModifiedBy; // Người sửa cuối cùng
    private LocalDateTime lastModifiedDate; // Thời gian sửa cuối cùng



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouponResponse {
        private Long id; // ID của bản ghi coupon_usage
        private String couponCode; // Mã coupon
        private Double discountAmount; // Số tiền giảm giá từ coupon
        private Date usedDate; // Thời gian sử dụng coupon
        private Integer createdBy; // Người tạo
        private LocalDateTime createdDate; // Thời gian tạo
        private Integer lastModifiedBy; // Người sửa cuối cùng
        private LocalDateTime lastModifiedDate; // Thời gian sửa cuối cùng
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private Long id;
        private String email;
        private String username;
        private String phoneNumber;
        private String role;
        private String addressStreet; // ok
        private String addressWard; // Phường (Phường Phúc Đồng)
        private String addressCity;  // ok
        private String addressState; // ok
        private String addressCountry;
        private String AddressZipcode;
        private String addressDistrict; // Quận/Huyện (Huyện Vĩnh Tuy)
        private String addressProvince; // Tỉnh (Quận Long Biên)
        private boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentResponse {
        private Long id;
        private Date shipmentDate;
        private String shipmentStatus;
        private String trackingNumber;
        private String carrier;
        private Date estimatedDeliveryDate;

        public ShipmentResponse(Long id, Date shipmentDate, ShipmentStatus shipmentStatus, String trackingNumber, String carrier, LocalDateTime estimatedDeliveryDate) {
        }
        //    private List<Long> orderItemIds; // Link to order_item.id


    }



}
