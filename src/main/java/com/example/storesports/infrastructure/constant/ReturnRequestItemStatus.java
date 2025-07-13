package com.example.storesports.infrastructure.constant;

public enum ReturnRequestItemStatus {
    PENDING,         // Đang chờ admin duyệt hoàn hàng

    APPROVED,        // Đã duyệt yêu cầu hoàn sản phẩm

    REJECTED,        // Bị từ chối hoàn

    RECEIVED,        // Đã nhận hàng hoàn từ khách (quan trọng để kiểm tra hàng thật)

    RETURNED_TO_STOCK, // Hàng còn tốt và đã cộng lại kho

    DISCARDED,       // Hàng hỏng, không cộng vào kho

    REFUNDED,        // Đã hoàn tiền cho sản phẩm

    COMPLETED        // Đã xử lý xong tất cả các bước (kết thúc luồng)
}
