package com.mathianasj.spring.rsql;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import lombok.Getter;

public class RsqlRepositoryCustomImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
		implements RsqlRepositoryCustom<T, ID> {
	@Getter
	private final Class<T> entityType;

	public RsqlRepositoryCustomImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);

		this.entityType = entityInformation.getJavaType();
	}
}
