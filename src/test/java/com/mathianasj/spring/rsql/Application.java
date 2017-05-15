package com.mathianasj.spring.rsql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.mathianasj.spring.rsql.RsqlRepositoryCustomImpl;

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = RsqlRepositoryCustomImpl.class)
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
