package com.example.storesports.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
@CreatedBy
protected Integer createdBy;

@CreatedDate
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
@JsonFormat(pattern = "MM/dd/yyyy")
protected LocalDateTime createdDate;

@LastModifiedBy
protected Integer lastModifiedBy;

@LastModifiedDate
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
@JsonFormat(pattern = "MM/dd/yyyy")
protected LocalDateTime lastModifiedDate;
}
