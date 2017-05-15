package com.mathianasj.spring.rsql;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@TestConfiguration
@ComponentScan("com.mathianasj.spring.rsql")
public class ConfigTest extends WebMvcConfigurerAdapter {

	public ConfigTest() {
		super();
	}

	// API

}