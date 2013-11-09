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
    testForError("err3", "Err3", "Interface a.funcB not connected");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "Interface not found: a.funcC");
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
    testForError("errHfsm2", "ErrHfsm2", "Name not found: funcC");
  }

  @Test
  public void errHfsm3() {
    testForError("errHfsm3", "ErrHfsm3", "???");
  }

}
