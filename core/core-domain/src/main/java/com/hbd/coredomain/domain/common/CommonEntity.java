package com.hbd.coredomain.domain.common;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class CommonEntity {

	@CreatedDate
	private LocalDateTime createdAt;

	@Column(name = "createdUserId", nullable = false)
	private String createdUserId;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	@Column(name = "updateUserId")
	private String updateUserId;

}