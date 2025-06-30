package com.example.storesports.core.client.returnoder.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOderDetailResponse {
    private String code;                  // Mã đơn hàng

    private Date orderDate;              // Ngày đặt hàng

    private String status;
    // Trạng thái đơn hàng

    private String paymentMethod;        // Hình thức thanh toán (COD, MOMO...)

    private String shippingMethod;       // Đơn vị giao hàng (GHN, GHTK...)

    private Double shippingFee;
    // Phí vận chuyển
//    private Double originalTotalAmount;
//
//    private Double discountAmount;       // Tổng giảm giá (coupon + discount)
//
//    private String couponCode;           // Mã coupon nếu có

    private Double totalAmount;          // Tổng tiền sau khi giảm

    private String receiverName;         // Tên người nhận

    private String receiverPhone;        // Số điện thoại

    private String shippingAddress;      // Địa chỉ giao

//    private String note;                 // Ghi chú đơn hàng (nếu có)

    private List<ReturnProductResponse> productDetails; // Danh sách sản phẩm trong đơn
}
