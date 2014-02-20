package test;

import org.junit.Test;

public class ErrSyntaxTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/syntax/";
  }

  @Test
  public void err1() {
    testForError("err1", "Err1", "Name not found: foo");
  }

  @Test
  public void err2() {
    testForError("err2", "Err2", "Missing arguments");
  }

}
