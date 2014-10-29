package test;

import org.junit.Test;

public class QueueTest extends BaseTest {

  public QueueTest() {
    super();
    setStrict(false);
  }

  @Override
  protected String getRootdir() {
    return "queued/";
  }

  @Test
  public void composition1() {
    compile("composition1", TestSteps.EXECUTE, true, false);
  }

  @Test
  public void simple() {
    compile("simple", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void simple2() {
    compile("simple2", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void simple3() {
    compile("simple3", TestSteps.EXECUTE, false, false);
  }
}
