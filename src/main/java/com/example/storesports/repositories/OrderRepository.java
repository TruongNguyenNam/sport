package com.example.storesports.repositories;


import com.example.storesports.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {

    @Query("SELECT o FROM Order o WHERE o.orderCode = :key")
    Optional<Order> findByOrderCode(@Param("key") String key);


    @Query("select p from Order p where p.isPos = false")
    List<Order> findAllOrderIsPos();

    //Doanh Thu Theo Ngày
    @Query(
            value = "SELECT DATE(o.order_date) AS day, SUM(o.order_total) AS total_revenue " +
                    "FROM `order` o " +
                    "WHERE o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED' " +
                    "GROUP BY day " +
                    "ORDER BY day",
            nativeQuery = true
    )
    List<Object[]> getDailyRevenue();

    //Theo Tháng
    @Query(
            value = "SELECT DATE_FORMAT(o.order_date, '%Y-%m') AS month, SUM(o.order_total) AS total_revenue " +
                    "FROM `order` o " +
                    "WHERE o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED' " +
                    "GROUP BY month " +
                    "ORDER BY month",
            nativeQuery = true
    )
    List<Object[]> getMonthlyRevenue();

    //Theo Năm
    @Query(
            value = "SELECT YEAR(o.order_date) AS year, SUM(o.order_total) AS total_revenue " +
                    "FROM `order` o " +
                    "WHERE o.order_status = 'COMPLETED' OR o.order_status = 'SHIPPED' " +
                    "GROUP BY year " +
                    "ORDER BY year",
            nativeQuery = true
    )
    List<Object[]> getYearlyRevenue();


}
