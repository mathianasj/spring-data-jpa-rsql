package com.mathianasj.spring.rsql;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.mathianasj.spring.rsql.dao.UserRepository;
import com.mathianasj.spring.rsql.entity.User;
import com.mathianasj.spring.rsql.spring.PersistenceConfig;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Import({ ConfigTest.class, PersistenceConfig.class })
public class RsqlJPASpecificationLiveTest {
	@Autowired
	private UserRepository repository;

	private User userJohn;

	private User userTom;

	@Before
	public void init() {
		userJohn = new User();
		userJohn.setFirstName("john");
		userJohn.setLastName("doe");
		userJohn.setEmail("john@doe.com");
		userJohn.setAge(22);
		repository.save(userJohn);

		userTom = new User();
		userTom.setFirstName("tom");
		userTom.setLastName("doe");
		userTom.setEmail("tom@doe.com");
		userTom.setAge(26);
		repository.save(userTom);
	}

	private final String URL_PREFIX = "http://localhost:8082/users/search/rsql?query=";

	@Test
	public void givenFirstOrLastName_whenGettingListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "firstName==john,lastName==doe");
		final String result = response.body().asString();
		assertTrue(result.contains(userJohn.getEmail()));
		assertTrue(result.contains(userTom.getEmail()));
	}

	@Test
	public void givenFirstAndLastName_whenGettingListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "firstName==john and lastName==doe");
		final String result = response.body().asString();

		assertTrue(result.contains(userJohn.getEmail()));
		assertFalse(result.contains(userTom.getEmail()));
	}

	@Test
	public void givenFirstNameInverse_whenGettingListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "firstName!=john");
		final String result = response.body().asString();

		assertTrue(result.contains(userTom.getEmail()));
		assertFalse(result.contains(userJohn.getEmail()));
	}

	@Test
	public void givenMinAge_whenGettingListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "age>25");
		final String result = response.body().asString();

		assertTrue(result.contains(userTom.getEmail()));
		assertFalse(result.contains(userJohn.getEmail()));
	}

	@Test
	public void givenFirstNamePrefix_whenGettingListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "firstName==jo*");
		final String result = response.body().asString();

		assertTrue(result.contains(userJohn.getEmail()));
		assertFalse(result.contains(userTom.getEmail()));
	}

	@Test
	public void givenFirstNameSuffix_whenGettingListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "firstName==*n");
		final String result = response.body().asString();

		assertTrue(result.contains(userJohn.getEmail()));
		assertFalse(result.contains(userTom.getEmail()));
	}

	@Test
	public void givenFirstNameSubstring_whenGettingListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "firstName==*oh*");
		final String result = response.body().asString();

		assertTrue(result.contains(userJohn.getEmail()));
		assertFalse(result.contains(userTom.getEmail()));
	}

	@Test
	public void givenAgeRange_whenGettingListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "age>20 and age<25");
		final String result = response.body().asString();

		assertTrue(result.contains(userJohn.getEmail()));
		assertFalse(result.contains(userTom.getEmail()));
	}

	@Test
	public void givenFirstOrLastName_whenGettingAdvListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "firstName==john or lastName==doe");
		final String result = response.body().asString();
		assertTrue(result.contains(userJohn.getEmail()));
		assertTrue(result.contains(userTom.getEmail()));
	}

	@Test
	public void givenFirstOrFirstNameAndAge_whenGettingAdvListOfUsers_thenCorrect() {
		final Response response = givenAuth().get(URL_PREFIX + "( firstName==john,firstName==tom ) ; age>22");
		final String result = response.body().asString();
		assertFalse(result.contains(userJohn.getEmail()));
		assertTrue(result.contains(userTom.getEmail()));
	}

	private final RequestSpecification givenAuth() {
		return RestAssured.given().auth().preemptive().basic("user1", "user1Pass");
	}
}
