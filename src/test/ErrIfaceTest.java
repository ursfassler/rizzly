package test;

import org.junit.Test;

public class ErrIfaceTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/iface/";
  }

  @Test
  public void err2() {
    testForError("err2", "Err2", "can not connect from input");
  }

  @Test
  public void err3() {
    testForError("err3", "Err3", "Interface funcA not connected");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "Interface not found: a.funcC");
  }

  @Test
  public void err7() {
    testForError("err7", "Err7", "Interface a.funcA not connected");
  }

  @Test
  public void errHfsm2() {
    testForError("errHfsm2", "ErrHfsm2", "Name not found: funcC");
  }

  @Test
  public void errHfsm3() {
    testForError("errHfsm3", "ErrHfsm3", "transition can only be triggered by slot");
  }

}
