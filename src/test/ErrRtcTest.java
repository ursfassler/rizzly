package test;

import org.junit.Test;

public class ErrRtcTest extends ErrorTest {
  @Override
  protected String getRootdir() {
    return "error/rtc/";
  }

  @Test
  public void rtc1() {
    testForError("rtc1", "Rtc1", "Violation of run to completion detected for component: sub");
  }

  @Test
  public void rtc2() {
    testForError("rtc2", "Rtc2", "Violation of run to completion detected for component: sub1");
  }

  @Test
  public void con1() {
    testForError("con1", "Con1", "Violation of run to completion detected for component: sub");
  }

  @Test
  public void con2() {
    testForError("con2", "Con2", "Violation of run to completion detected for component: sub1");
  }

}
