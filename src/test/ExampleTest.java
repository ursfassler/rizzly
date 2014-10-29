package test;

import org.junit.Test;

public class ExampleTest extends BaseTest {

  public ExampleTest() {
    super();
    setStrict(false);
  }

  @Override
  protected String getRootdir() {
    return "../example/";
  }

  @Test
  public void motor() {
    compileExample("motor/", "main.Controller");
  }

  @Test
  public void alarmclock() {
    compileExample("alarmclock/", "clock.Clock");
  }

}
