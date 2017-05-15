package com.mathianasj.spring.rsql;

public class CustomRsqlVisitorFactory {
	public static <T> CustomRsqlVisitor<T> build(Class<T> type) {
		return new CustomRsqlVisitor<T>();
	}
}
