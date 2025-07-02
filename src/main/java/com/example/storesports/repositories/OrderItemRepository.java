package com.example.storesports.repositories;


import com.example.storesports.core.admin.orderItem.payload.SellingProductsProjection;
import com.example.storesports.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long>, JpaSpecificationExecutor<OrderItem> {

    @Query("from OrderItem a where a.order.id = :id")
    List<OrderItem> findByOrderId(@Param("id") Long id);

    // Tổng sản phẩm bán thành công hôm nay (ngày, tháng, năm cụ thể)
    @Query("SELECT SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE (o.orderStatus = 'COMPLETED' or o.orderStatus ='SHIPPED') " +
            "AND DATE(o.orderDate) = :date")
    Long getTotalSoldQuantityByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE (o.orderStatus = 'COMPLETED' or o.orderStatus ='SHIPPED') " +
            "AND FUNCTION('MONTH', o.orderDate) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', o.orderDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Long getTotalSoldQuantityByMonth();

    @Query("SELECT SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE (o.orderStatus = 'COMPLETED' or o.orderStatus ='SHIPPED') " +
            "AND FUNCTION('YEAR', o.orderDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Long getTotalSoldQuantityByYear();

    @Query(value = """
    SELECT 
        p.id AS id,
        img.image_url AS imgUrl,
        p.name AS productName,
        SUM(oi.quantity) AS soldQuantity,
        ROUND(SUM(oi.quantity) / NULLIF(total.total_quantity, 0) * 100, 2) AS percentage,
        DATE(o.order_date) AS orderDate
    FROM order_item oi
    JOIN product p ON oi.product_id = p.id
    JOIN `order` o ON oi.order_id = o.id
    JOIN (
        SELECT product_id, MIN(image_url) AS image_url
        FROM product_image
        GROUP BY product_id
    ) img ON img.product_id = p.id
    JOIN (
        SELECT SUM(oi.quantity) AS total_quantity
        FROM order_item oi
        JOIN `order` o ON oi.order_id = o.id
        WHERE (o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED')
          AND DATE(o.order_date) = CURRENT_DATE
    ) total
    WHERE (o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED')
      AND DATE(o.order_date) = CURRENT_DATE
    GROUP BY p.id, p.name, img.image_url, DATE(o.order_date), total.total_quantity
    ORDER BY soldQuantity DESC
    LIMIT 30
""", nativeQuery = true)
    List<SellingProductsProjection> findTop30SellingProductsToday();

    @Query(value = """
    SELECT 
        p.id AS id,
        img.image_url AS imgUrl,
        p.name AS productName,
        SUM(oi.quantity) AS soldQuantity,
        ROUND(SUM(oi.quantity) / NULLIF(total.total_quantity, 0) * 100, 2) AS percentage,
        DATE(MAX(o.order_date)) AS orderDate
    FROM order_item oi
    JOIN product p ON oi.product_id = p.id
    JOIN `order` o ON oi.order_id = o.id
    JOIN (
        SELECT product_id, MIN(image_url) AS image_url
        FROM product_image
        GROUP BY product_id
    ) img ON img.product_id = p.id
    JOIN (
        SELECT SUM(oi.quantity) AS total_quantity
        FROM order_item oi
        JOIN `order` o ON oi.order_id = o.id
        WHERE (o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED')
          AND MONTH(o.order_date) = MONTH(CURRENT_DATE)
          AND YEAR(o.order_date) = YEAR(CURRENT_DATE)
    ) total
    WHERE (o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED')
      AND MONTH(o.order_date) = MONTH(CURRENT_DATE)
      AND YEAR(o.order_date) = YEAR(CURRENT_DATE)
    GROUP BY p.id, p.name, img.image_url, total.total_quantity
    ORDER BY soldQuantity DESC
    LIMIT 30
""", nativeQuery = true)
    List<SellingProductsProjection> findTop30SellingProductsThisMonth();


    @Query(value = """
    SELECT 
        p.id AS id,
        img.image_url AS imgUrl,
        p.name AS productName,
        SUM(oi.quantity) AS soldQuantity,
        ROUND(SUM(oi.quantity) / NULLIF(total.total_quantity, 0) * 100, 2) AS percentage,
        DATE(MAX(o.order_date)) AS orderDate
    FROM order_item oi
    JOIN product p ON oi.product_id = p.id
    JOIN `order` o ON oi.order_id = o.id
    JOIN (
        SELECT product_id, MIN(image_url) AS image_url
        FROM product_image
        GROUP BY product_id
    ) img ON img.product_id = p.id
    JOIN (
        SELECT SUM(oi.quantity) AS total_quantity
        FROM order_item oi
        JOIN `order` o ON oi.order_id = o.id
        WHERE (o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED')
          AND YEAR(o.order_date) = YEAR(CURRENT_DATE)
    ) total
    WHERE (o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED')
      AND YEAR(o.order_date) = YEAR(CURRENT_DATE)
    GROUP BY p.id, p.name, img.image_url, total.total_quantity
    ORDER BY soldQuantity DESC
    LIMIT 30
""", nativeQuery = true)
    List<SellingProductsProjection> findTop30SellingProductsThisYear();

    @Query(value = """
    SELECT 
        p.id AS id,
        img.image_url AS imgUrl,
        p.name AS productName,
        SUM(oi.quantity) AS soldQuantity,
        ROUND(SUM(oi.quantity) / NULLIF(total.total_quantity, 0) * 100, 2) AS percentage,
        DATE(o.order_date) AS orderDate
    FROM order_item oi
    JOIN product p ON oi.product_id = p.id
    JOIN `order` o ON oi.order_id = o.id
    JOIN (
        SELECT product_id, MIN(image_url) AS image_url
        FROM product_image
        GROUP BY product_id
    ) img ON img.product_id = p.id
    JOIN (
        SELECT SUM(oi.quantity) AS total_quantity
        FROM order_item oi
        JOIN `order` o ON oi.order_id = o.id
        WHERE (o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED')
          AND DATE(o.order_date) BETWEEN :startDate AND :endDate
    ) total
    WHERE (o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED')
      AND DATE(o.order_date) BETWEEN :startDate AND :endDate
    GROUP BY p.id, p.name, img.image_url, DATE(o.order_date), total.total_quantity
    ORDER BY soldQuantity DESC
    LIMIT 30
""", nativeQuery = true)
    List<SellingProductsProjection> findTopSellingProductsBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    //
    @Query("""
    SELECT SUM(oi.quantity)
    FROM OrderItem oi
    JOIN oi.order o
    WHERE (o.orderStatus = 'COMPLETED' OR o.orderStatus = 'SHIPPED')
    AND o.orderDate BETWEEN :startDate AND :endDate
""")
    Long getTotalSoldQuantityBetween(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

}
