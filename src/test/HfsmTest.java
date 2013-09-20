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
  public void transDist() {
    compile("transDist", "TransDist", true, false, false);
  }

  @Test
  public void entryExit1() {
    compile("entryExit1", "EntryExit1", true, false, false);
  }

  @Test
  public void hfsmFunction3() {
    compile("hfsmFunction3", "HfsmFunction3", true, false, false);
  }

  @Test
  public void hfsmFunction4() {
    compile("hfsmFunction4", "HfsmFunction4", true, false, false);
  }

  @Test
  public void hfsmFunction5() {
    compile("hfsmFunction5", "HfsmFunction5", true, false, false);
  }

  @Test
  public void transition2() {
    compile("transition2", "Transition2", true, false, false);
  }

  @Test
  public void unusedState() {
    compile("unusedState", "UnusedState", true, false, false);
  }

  @Test
  public void stateVariable2() {
    compile("stateVariable2", "StateVariable2", true, false, false);
  }

  @Test
  public void transScope_TransScope() {
    compile("transScope", "TransScope", true, false, false);
  }
}
