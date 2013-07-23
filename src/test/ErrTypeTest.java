package test;

import org.junit.Test;

public class ErrTypeTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "/home/urs/projekte/rizzly/compiler/test/error/type/";
  }

  @Test
  public void err1() {
    testForError("err1", "Err1", "Data type to big or incompatible in assignment: U{8} := Weekday{}");
  }

  @Test
  public void err2() {
    testForError("err2", "Err2", "Data type to big or incompatible in assignment: Weekday{} := U{8}");
  }

  @Test
  public void err3() {
    testForError("err3", "Err3", "Arithmetic operation not possible on enumerator");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "Data type to big or incompatible (argument 1, U{8} := Weekday{})");
  }

}
