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
  public void elementary1() {
    compile("elementary1", "Elementary1", true, false, false);
  }

  @Test
  public void elementary2() {
    compile("elementary2", "Elementary2", true, false, false);
  }

  @Test
  public void elementary3() {
    compile("elementary3", "Elementary3", true, false, false);
  }

  @Test
  public void elementary4() {
    compile("elementary4", "Elementary4", true, false, false);
  }

  @Test
  public void transDist() {
    compile("transDist", "TransDist", true, false, false);
  }

  @Test
  public void transOrder() {
    compile("transOrder", "TransOrder", true, false, false);
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
  public void query_Query() {
    compile("query", "Query", true, false, false);
  }

  @Test
  public void query2_Query2() {
    compile("query2", "Query2", true, false, false);
  }

  @Test
  public void transScope_TransScope() {
    compile("transScope", "TransScope", true, false, false);
  }
}
