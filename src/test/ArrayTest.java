package test;

import org.junit.Test;

public class ArrayTest extends BaseTest {

  @Override
  protected String getRootdir() {
    return "array/";
  }

  @Test
  public void copy1() {
    compile("copy1", "Copy", true, false, false);
  }

  @Test
  public void copy2() {
    compile("copy2", "Copy", true, false, false);
  }

  @Test
  public void arrayTest() {
    compile("arrayTest", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest1() {
    compile("arrayTest1", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest2() {
    compile("arrayTest2", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest3() {
    compile("arrayTest3", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest4() {
    compile("arrayTest4", TestSteps.COMPILE_TO_BIN, false, false);
  }

  @Test
  public void arrayTest5() {
    compile("arrayTest5", TestSteps.COMPILE_TO_BIN, false, false);
  }

  @Test
  public void arrayTest6() {
    compile("arrayTest6", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest7() {
    compile("arrayTest7", TestSteps.COMPILE_TO_BIN, false, false);
  }

  @Test
  public void reverse() {
    compile("reverse", TestSteps.EXECUTE, false, false);
  }

}
