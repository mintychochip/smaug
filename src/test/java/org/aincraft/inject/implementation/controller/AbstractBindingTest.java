package org.aincraft.inject.implementation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AbstractBindingTest {

  static final class TestBinding extends AbstractBinding {
    @ExposedProperty("test-string")
    private final String testString;

    private TestBinding(String testString) {
      this.testString = testString;
    }
  }

  private static final String TEST_STRING = "test_string";
  private static final TestBinding BINDING = new TestBinding(TEST_STRING);

  @Test
  void testGetProperty() {
    String property = BINDING.getProperty(String.class);
    System.out.println(property);
    assertNotNull(property);
    assertEquals(property,TEST_STRING);
  }
}