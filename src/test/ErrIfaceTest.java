package test;

import org.junit.Test;


public class ErrIfaceTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "/home/urs/projekte/rizzly/compiler/test/error/iface/";
  }

  @Test
  public void err1() {
    testForError("err1", "Err1", "missing function implementation in.funcB");
  }

  @Test
  public void err2() {
    testForError("err2", "Err2", "interface in has no function funcC");
  }

  @Test
  public void err3() {
    testForError("err3", "Err3", "missing function implementation a.out.funcB");
  }

  @Test
  public void err4() {
    testForError("err4", "Err4", "interface a.out has no function funcC");
  }

  @Test
  public void err5() {
    testForError("err5", "Err5", "Missing functions of interface in");
  }

  @Test
  public void err6() {
    testForError("err6", "Err6", "No interface with the name in2");
  }

  @Test
  public void err7() {
    testForError("err7", "Err7", "No callback functions found for component b");
  }

  @Test
  public void err8() {
    testForError("err8", "Err8", "No component defined with name b");
  }

  @Test
  public void err9() {
    testForError("err9", "Err9", "Missing callback for component/interface a.out");
  }

  @Test
  public void err10() {
    testForError("err10", "Err10", "Component a has no output interface out2");
  }

}
