package com.example.storesports.repositories;

import com.example.storesports.entity.ReturnRequestItem;
import com.example.storesports.infrastructure.constant.ReturnRequestItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRequestItemRepository extends JpaRepository<ReturnRequestItem, Long> {

    @Query("""
    SELECT COUNT(rri) > 0 
    FROM ReturnRequestItem rri 
    JOIN rri.returnRequest rr 
    WHERE rri.orderItem.id = :orderItemId 
      AND rr.user.id = :userId 
      AND rri.status <> :status
      AND rri.deleted = false
""")
    boolean existsByOrderItemAndUserAndStatusNotAndDeletedFalse(
            @Param("orderItemId") Long orderItemId,
            @Param("userId") Long userId,
            @Param("status") ReturnRequestItemStatus status
    );
}
