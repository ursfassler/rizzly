package test;

import org.junit.Test;

public class ExampleTest extends BaseTest {

  public ExampleTest() {
    super();
    setStrict(false);
  }

  @Override
  protected String getRootdir() {
    return "";
  }

  @Test
  public void motor() {
    compileExample("motor/", "main.Controller");
  }

}
