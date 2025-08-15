package com.example.storesports.repositories;



import com.example.storesports.entity.ReturnRequestReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRequestReasonRepository extends JpaRepository<ReturnRequestReason,Long> {




}
