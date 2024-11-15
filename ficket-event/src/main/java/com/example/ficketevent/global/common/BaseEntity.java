package com.example.ficketevent.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    @CreatedDate
    @Column(updatable = false,name = "CREATED_AT")
    private LocalDateTime createdAt; // 생성일

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_AT")
    private LocalDateTime lastModifiedAt; // 수정일

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt; // 삭제일
}