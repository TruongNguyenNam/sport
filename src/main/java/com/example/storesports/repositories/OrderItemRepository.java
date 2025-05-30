package com.example.storesports.repositories;


import com.example.storesports.entity.OrderItem;
import com.example.storesports.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long>, JpaSpecificationExecutor<OrderItem> {

    @Query("from OrderItem a where a.order.id = :id")
    List<OrderItem> findByOrderId(@Param("id") Long id);

    //Tống kê doanh thu theo sản phẩm bán thành công
    @Query(value = "SELECT " +
            "    p.id AS product_id, " +
            "    p.name AS product_name, " +
            "    SUM(oi.quantity) AS total_quantity_sold " +
            "FROM " +
            "    order_item oi " +
            "JOIN " +
            "    `order` o ON oi.order_id = o.id " +
            "JOIN " +
            "    product p ON oi.product_id = p.id " +
            "WHERE " +
            "    o.order_status IN ('COMPLETED', 'SHIPPED') " +
            "GROUP BY " +
            "    p.id, p.name " +
            "ORDER BY " +
            "    total_quantity_sold DESC", nativeQuery = true)
    List<Object[]> getProductSalesStatistics();

}
