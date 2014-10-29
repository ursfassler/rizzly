package test;

import org.junit.Test;

public class ErrRtcTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/rtc/";
  }

  @Test
  public void con1() {
    testForError("con1", "Con1", "Violation of run to completion detected");
  }

  @Test
  public void con2() {
    testForError("con2", "Con2", "Violation of run to completion detected");
  }

}
