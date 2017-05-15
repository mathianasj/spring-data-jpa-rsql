package com.mathianasj.spring.rsql;

import java.io.Serializable;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface RsqlRepositoryCustom<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {
	public Class<T> getEntityType();
}
