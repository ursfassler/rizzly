package test;

import org.junit.Test;

public class ErrTypeTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/type/";
  }

  @Test
  public void err1() {
    testForError("err1", "Err1", "Data type to big or incompatible in assignment: R{0,10} := Weekday");
  }

  @Test
  public void err2() {
    testForError("err2", "Err2", "Data type to big or incompatible in assignment: Weekday := R{0,10}");
  }

  @Test
  public void err3() {
    testForError("err3", "Err3", "Expected range type, got Weekday");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "Data type to big or incompatible (argument 1, R{0,10} := Weekday)");
  }

  @Test
  public void err5() {
    testForError("err5", "Err5", "need integer type to index array, got: Void");
  }

  @Test
  public void err6() {
    testForError("err6", "Err6", "Elements not initialized: [2, 3, 4, 5, 6, 7]");
  }
}
