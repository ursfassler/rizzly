package test;

import org.junit.Test;

public class HfsmTest extends BaseTest {

  @Override
  protected String getRootdir() {
    return "hfsm/";
  }

  @Test
  public void hfsm() {
    compile("hfsm", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void transOrder() {
    compile("transOrder", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void query() {
    compile("query",  TestSteps.EXECUTE, false, false);
  }

  @Test
  public void query2() {
    compile("query2",  TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void query3() {
    compile("query3",  TestSteps.EXECUTE, false, false);
  }

  @Test
  public void query4() {
    compile("query4",  TestSteps.EXECUTE, false, false);
  }

  @Test
  public void unusedState() {
    compile("unusedState",TestSteps.COMPILE_TO_PIR, false, false);  //TODO check for warnings
  }

  @Test
  public void stateVariable2() {
    compile("stateVariable2", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void transScope() {
    compile("transScope", TestSteps.EXECUTE, false, false);
  }
}
