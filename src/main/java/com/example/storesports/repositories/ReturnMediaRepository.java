package com.example.storesports.repositories;

import com.example.storesports.entity.ReturnMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.print.attribute.standard.Media;

@Repository
public interface ReturnMediaRepository extends JpaRepository<ReturnMedia, Long> {
}
