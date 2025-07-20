package com.example.storesports.repositories;


import com.example.storesports.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest,Long> {
@Query("select r from ReturnRequest r where r.user.username=:userName order by r.requestDate desc ")
List<ReturnRequest> findByUserName(@Param("userName")String userName);
@Query ("select count (i)from ReturnRequest r join r.items i where r.code=:code")
    Long countByCode(@Param("code") String code);
@Query("select r from ReturnRequest r order by r.requestDate desc ")
    List<ReturnRequest> findAll();

}
