package com.example.storesports.repositories;


import com.example.storesports.core.admin.order.payload.*;
import com.example.storesports.entity.Order;
import com.example.storesports.infrastructure.constant.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {

    @Query("SELECT o FROM Order o WHERE o.orderCode = :key")
    Optional<Order> findByOrderCode(@Param("key") String key);

    @Query("SELECT new com.example.storesports.core.admin.order.payload.OrderStatusCount(o.orderStatus, COUNT(o)) " +
            "FROM Order o " +
            "WHERE o.orderStatus IN :statuses " +
            "GROUP BY o.orderStatus")
    List<OrderStatusCount> countOrdersByStatus(@Param("statuses") List<OrderStatus> statuses);


//    @Query("SELECT o FROM Order o WHERE o.orderStatus = :orderStatus AND o.deleted = false AND o.createdDate < :createdDate")
//    List<Order> findByOrderStatusAndDeletedFalseAndCreatedDateBefore(
//            @Param("orderStatus") OrderStatus orderStatus,
//            @Param("createdDate") LocalDateTime createdDate
//    );

    @Query("""
    SELECT o FROM Order o 
    WHERE o.orderStatus = :orderStatus 
    AND SIZE(o.orderItems) = 0 
    AND o.createdDate < :createdBefore
""")
    List<Order> findEmptyPendingOrdersBefore(
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("createdBefore") LocalDateTime createdBefore
    );


    //danh sách đơn hàng của customer phía client
    @Query("select o from Order o where o.user.id = :customerId and o.isPos = false order by o.id desc ")
    List<Order> findByUserId(@Param("customerId") Long customerId);



    @Query("select p from Order p order by p.id desc")
    List<Order> getAllOrder();


    @Query("select o from  Order o where o.orderStatus = :orderStatus")
    List<Order> findByOrderStatusAndDeletedFalseAndOrderSource
            (@Param("orderStatus") OrderStatus orderStatus);
//
    @Query("select o from Order o WHERE o.orderStatus= :orderStatus")
    List<Order> findByOrderStatus(@Param("orderStatus") OrderStatus orderStatus);


    @Query("select p from Order p where p.deleted = true order by p.id desc")
    List<Order> findAllOrderIsPos();

    //Doanh Thu Theo Ngày
    @Query(
            value = "SELECT DATE(o.order_date) AS day, SUM(o.order_total) AS total_revenue " +
                    "FROM `order` o " +
                    "WHERE o.order_status = 'COMPLETED' " +
                    "AND DATE(o.order_date) = CURDATE() " +
                    "GROUP BY day",
            nativeQuery = true
    )
    DailyRevenueProjection getTodayRevenue();


    //Theo Tháng
    @Query(
            value = "SELECT DATE_FORMAT(o.order_date, '%Y-%m') AS month, SUM(o.order_total) AS total_revenue " +
                    "FROM `order` o " +
                    "WHERE (o.order_status = 'COMPLETED') " +
                    "AND YEAR(o.order_date) = YEAR(CURDATE()) " +
                    "AND MONTH(o.order_date) = MONTH(CURDATE()) " +
                    "GROUP BY month",
            nativeQuery = true
    )
    MonthlyRevenueProjection getCurrentMonthRevenue();


    //Theo Năm
    @Query(
            value = "SELECT YEAR(o.order_date) AS year, SUM(o.order_total) AS total_revenue " +
                    "FROM `order` o " +
                    "WHERE (o.order_status = 'COMPLETED') " +
                    "AND YEAR(o.order_date) = YEAR(CURDATE()) " +
                    "GROUP BY year",
            nativeQuery = true
    )
    YearlyRevenueProjection getCurrentYearRevenue();

    // Hàng Huỷ theo ngày
    @Query("SELECT COUNT(o) FROM Order o WHERE (o.orderStatus = 'CANCELLED' OR o.deleted = false) AND DATE(o.createdDate) = CURRENT_DATE")
    Long countCancelledOrdersToday();

    // Hàng Huỷ theo tháng
    @Query("SELECT COUNT(o) FROM Order o WHERE (o.orderStatus = 'CANCELLED' OR o.deleted = false) " +
            "AND FUNCTION('MONTH', o.createdDate) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', o.createdDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Long countCancelledOrdersThisMonth();


    // Hàng Huỷ theo năm
    @Query("SELECT COUNT(o) FROM Order o WHERE (o.orderStatus = 'CANCELLED' OR o.deleted = false) " +
            "AND FUNCTION('YEAR', o.createdDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Long countCancelledOrdersThisYear();



    //đơn Hàng đã hoàn thành theo ngày
    @Query("SELECT COUNT(o) FROM Order o WHERE (o.orderStatus = 'COMPLETED' or o.deleted = true) AND DATE(o.orderDate) = CURRENT_DATE")
    Long countCompletedOrdersToday();

    @Query("SELECT COUNT(o) FROM Order o WHERE (o.orderStatus = 'COMPLETED' or o.deleted = true) " +
            "AND FUNCTION('MONTH', o.orderDate) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', o.orderDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Long countCompletedOrdersThisMonth();

    @Query("SELECT COUNT(o) FROM Order o WHERE (o.orderStatus = 'COMPLETED' or o.deleted = true ) " +
            "AND FUNCTION('YEAR', o.orderDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Long countCompletedOrdersThisYear();


    //
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'RETURNED' AND DATE(o.orderDate) = CURRENT_DATE")
    Long countReturnedOrdersToday();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'RETURNED' " +
            "AND FUNCTION('MONTH', o.orderDate) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', o.orderDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Long countReturnedOrdersThisMonth();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'RETURNED' " +
            "AND FUNCTION('YEAR', o.orderDate) = FUNCTION('YEAR', CURRENT_DATE)")
    Long countReturnedOrdersThisYear();

    //test
    @Query(value = "SELECT SUM(o.order_total) FROM `order` o " +
            "WHERE (o.order_status = 'COMPLETED' ) " +
            "AND o.order_date BETWEEN :startDate AND :endDate", nativeQuery = true)
    Double getRevenueBetweenDates(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE (o.orderStatus = 'CANCELLED' or o.deleted = false )" +
            "AND o.createdDate BETWEEN :startDate AND :endDate")
    Long countCancelledOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE (o.orderStatus = 'COMPLETED' or o.deleted = true) " +
            "AND o.orderDate BETWEEN :startDate AND :endDate")
    Long countCompletedOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'RETURNED' " +
            "AND o.orderDate BETWEEN :startDate AND :endDate")
    Long countReturnedOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MONTH(o.orderDate) AS month, o.isPos AS isPos, COUNT(o) AS totalOrders " +
            "FROM Order o " +
            "WHERE o.deleted = true AND o.orderDate IS NOT NULL " +
            "GROUP BY MONTH(o.orderDate), o.isPos " +
            "ORDER BY MONTH(o.orderDate)")
    List<MonthlyOrderTypeProjection> getMonthlyOrderTypeStats();




    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderStatus IN :statuses and o.isPos=false and o.deleted=true order by o.createdDate desc ")
    List<Order> findByUserAndStatuses(@Param("userId") Long userId,
                                      @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderCode = :orderCode AND o.orderStatus IN :statuses and o.isPos=false and o.deleted=true")
    Optional<Order> findOrderByUserAndCodeAndStatuses(@Param("userId") Long userId,
                                                      @Param("orderCode") String orderCode,
                                                      @Param("statuses") List<OrderStatus> statuses);




}
