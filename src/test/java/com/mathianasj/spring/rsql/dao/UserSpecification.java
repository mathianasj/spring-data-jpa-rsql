package com.mathianasj.spring.rsql.dao;

import java.util.List;

import com.mathianasj.spring.rsql.GenericRsqlSpecification;
import com.mathianasj.spring.rsql.entity.User;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;

public class UserSpecification extends GenericRsqlSpecification<User> {

	public UserSpecification(String property, ComparisonOperator operator, List<String> arguments) {
		super(property, operator, arguments);
	}

}
