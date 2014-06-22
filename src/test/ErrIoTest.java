package test;

import org.junit.Test;

public class ErrIoTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/iocheck/";
  }

  @Test
  public void err1() {
    testForError("err1", "Err1", "Response sends event");
  }

  @Test
  public void err2() {
    testForError("err2", "Err2", "Response writes state");
  }

  @Test
  public void err3() {
    testForError("err3", "Err3", "Response sends event");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "Response writes state");
  }

  @Test
  public void err5() {
    testForError("err5", "Err5", "Response sends event");
  }

  @Test
  public void err6() {
    testForError("err6", "Err6", "Response writes state");
  }

  @Test
  public void err7() {
    testForError("err7", "Err7", "Transition guard sends event");
  }

  @Test
  public void err8() {
    testForError("err8", "Err8", "Transition guard writes state");
  }

}
