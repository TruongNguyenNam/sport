package com.example.storesports.repositories;


import com.example.storesports.entity.ReturnRequest;
import com.example.storesports.infrastructure.constant.ReturnRequestItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest,Long> {
@Query("select r from ReturnRequest r where r.user.username=:userName order by r.requestDate desc ")
List<ReturnRequest> findByUserName(@Param("userName")String userName);
@Query ("select count (i)from ReturnRequest r join r.items i where r.code=:code")
    Long countByCode(@Param("code") String code);
//    @Query("select count(i) from ReturnRequestItem i where i.orderItem.order.orderCode = :orderCode and i.status in :status and i.deleted=false ")
//    Long countByOrderCodeAndStatus(@Param("orderCode") String orderCode, @Param("status") List<ReturnRequestItemStatus> status);
//    @Query("""
//    select count(distinct i.orderItem.id)
//    from ReturnRequestItem i
//    where i.orderItem.order.orderCode = :orderCode
//      and i.status in :status
//""")
//    Long countByOrderCodeAndStatus(
//            @Param("orderCode") String orderCode,
//            @Param("status") List<ReturnRequestItemStatus> status
//    );


    @Query("""
    select count(distinct i.orderItem.id)
    from ReturnRequestItem i
    where i.orderItem.order.orderCode = :orderCode
      and i.status in :status
      and (
        select sum(r.quantity)
        from ReturnRequestItem r
        where r.orderItem.id = i.orderItem.id
          and r.status in :status
      ) >= i.orderItem.quantity
""")
    Long countByOrderCodeAndStatus(
            @Param("orderCode") String orderCode,
            @Param("status") List<ReturnRequestItemStatus> status
    );

    @Query("select r from ReturnRequest r join ReturnRequestItem i on r.id=i.returnRequest.id where i.status=:status order by r.requestDate desc ")
    List<ReturnRequest> finByStatusItem(@Param("status")ReturnRequestItemStatus status);
    @Query ("select count (i.status)from ReturnRequest r join r.items i where r.code=:code and i.status=:status")
    Long countByCodeApproved(@Param("code") String code,@Param("status") ReturnRequestItemStatus status);
    @Query("select r from ReturnRequest r join ReturnRequestItem i on r.id=i.returnRequest.id where r.code like %:code% and i.status=:status")
    List<ReturnRequest> findByCode(@Param("code") String code, @Param("status") ReturnRequestItemStatus status);

}
