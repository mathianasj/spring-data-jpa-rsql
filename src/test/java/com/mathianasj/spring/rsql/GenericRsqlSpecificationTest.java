package com.mathianasj.spring.rsql;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.junit.Test;
import org.mockito.Mockito;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;

public class GenericRsqlSpecificationTest {
  enum TestEnum {
    TEST
  }

  class TestObj {
    TestEnum enumValue;
  }

  @Test
  public void testEnumArgument() {
    final String property = "enumValue";
    ComparisonOperator operator = RSQLOperators.EQUAL;
    List<String> arguments = Arrays.asList("TEST");
    @SuppressWarnings("unchecked")
    Root<TestObj> root = Mockito.mock(Root.class);
    Path path = Mockito.mock(Path.class);
    CriteriaBuilder builder = Mockito.mock(CriteriaBuilder.class);
    Predicate expected = Mockito.mock(Predicate.class);

    Mockito.when(root.get("enumValue")).thenReturn(path);
    Mockito.when(path.getJavaType()).thenReturn(TestEnum.class);
    Mockito.when(builder.equal(path, TestEnum.TEST)).thenReturn(expected);

    GenericRsqlSpecification<TestObj> underTest =
        new GenericRsqlSpecification<>(property, operator, arguments);

    Predicate predicate = underTest.toPredicate(root, Mockito.mock(CriteriaQuery.class), builder);

    assertEquals(expected, predicate);
  }
}
