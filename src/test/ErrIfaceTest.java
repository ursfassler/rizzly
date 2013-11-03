package test;

import org.junit.Test;

public class ErrIfaceTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/iface/";
  }

  @Test
  public void err1() {
    testForError("err1", "Err1", "Missing function implementation funcB");
  }

  @Test
  public void err3() {
    testForError("err3", "Err3", "Missing callback for component/function a.funcB");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "Component a has no output function funcC");
  }

  @Test
  public void err5() {
    testForError("err5", "Err5", "Missing function implementation funcA");
  }

  @Test
  public void err7() {
    testForError("err7", "Err7", "No callback functions found for component b");
  }

  @Test
  public void errHfsm2() {
    testForError("errHfsm2", "ErrHfsm2", "interface Iface{} has no function funcC");
  }

  @Test
  public void errHfsm3() {
    testForError("errHfsm3", "ErrHfsm3", "???");
  }

}
