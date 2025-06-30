package com.example.storesports.core.client.returnoder.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOderRequest {
    private String orderCode;        // Mã đơn hàng cần hoàn
    private Long productId;          // ID sản phẩm cần hoàn (null nếu hoàn toàn bộ đơn)
    private String reason;           // Lý do hoàn hàng (vd: damaged, wrong_item, etc.)
    private String note;             // Ghi chú thêm từ khách hàng (tùy chọn)
    private Boolean returnAll;
}
