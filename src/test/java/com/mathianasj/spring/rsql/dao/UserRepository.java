package com.mathianasj.spring.rsql.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.mathianasj.spring.rsql.RsqlRepositoryCustom;
import com.mathianasj.spring.rsql.entity.User;

@RepositoryRestResource
public interface UserRepository extends RsqlRepositoryCustom<User, Long>, JpaSpecificationExecutor<User> {

}
