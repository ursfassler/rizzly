package test;

import org.junit.Test;


public class HfsmTest extends BaseTest {
  private static final boolean DEBUG_EVENT = true;

  @Override
  protected String getRootdir() {
    return "/home/urs/projekte/rizzly/compiler/test/hfsm/";
  }

  @Test
  public void elementary1() {
    compile("elementary1", "Elementary1", true, DEBUG_EVENT, false);
  }

  @Test
  public void elementary2() {
    compile("elementary2", "Elementary2", true, DEBUG_EVENT, false);
  }

  @Test
  public void elementary3() {
    compile("elementary3", "Elementary3", true, DEBUG_EVENT, false);
  }

  @Test
  public void elementary4() {
    compile("elementary4", "Elementary4", true, DEBUG_EVENT, false);
  }

  @Test
  public void elementary5() {
    compile("elementary5", "Elementary5", true, DEBUG_EVENT, false);   //FIXME implement upcasts
  }

  @Test
  public void hfsm() {
    compile("hfsm", "Hfsm", true, DEBUG_EVENT, false);
  }

  @Test
  public void transDist() {
    compile("transDist", "TransDist", true, DEBUG_EVENT, false);
  }

  @Test
  public void transOrder() {
    compile("transOrder", "TransOrder", true, DEBUG_EVENT, false);
  }

  @Test
  public void entryExit01() {
    compile("entryExit01", "EntryExit01", true, DEBUG_EVENT, false);
  }

  @Test
  public void hfsmFunction3() {
    compile("hfsmFunction3", "HfsmFunction3", true, DEBUG_EVENT, false);
  }

  @Test
  public void hfsmFunction4() {
    compile("hfsmFunction4", "HfsmFunction4", true, DEBUG_EVENT, false);
  }

  @Test
  public void hfsmFunction5() {
    compile("hfsmFunction5", "HfsmFunction5", true, DEBUG_EVENT, false);
  }

  @Test
  public void transition2() {
    compile("transition2", "Transition2", true, DEBUG_EVENT, false);
  }

  @Test
  public void unusedState() {
    compile("unusedState", "UnusedState", true, DEBUG_EVENT, false);
  }

  @Test
  public void stateVariable2() {
    compile("stateVariable2", "StateVariable2", true, DEBUG_EVENT, false);
  }

  @Test
  public void query_Query() {
    compile("query", "Query", true, DEBUG_EVENT, false);
  }

  @Test
  public void query2_Query2() {
    compile("query2", "Query2", true, DEBUG_EVENT, false);
  }

  @Test
  public void transScope_TransScope() {
    compile("transScope", "TransScope", true, DEBUG_EVENT, false);
  }
}
