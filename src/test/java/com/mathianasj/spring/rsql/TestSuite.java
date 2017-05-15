package com.mathianasj.spring.rsql;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		// @formatter:off
		PersistenceTestSuite.class, LiveTestSuite.class }) //
public class TestSuite {

}