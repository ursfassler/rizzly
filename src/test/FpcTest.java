package test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class FpcTest extends BaseTest {

  @Override
  protected String getRootdir() {
    return "fpc/";
  }

  @Test
  public void nop() {
    test("nop", "Nop", "start nop\nok\nend nop\n", false);
  }

  @Test
  public void arr() {
    test("arr", "Arr", "start arr\nok\nend arr\n", false);
  }

  @Test
  public void structt() {
    test("structt", "Structt", "start structt\nok\nok\nend structt\n", false);
  }

  @Test
  public void enumt() {
    test("enumt", "Enumt", "start enumt\nenumt_Color_Red\nenumt_Color_Green\nenumt_Color_Blue\nend enumt\n", false);
  }

  @Test
  public void stringt() {
    test("stringt", "Stringt", "start stringt\n1:Hello\n2:Hallo\n1:World\n2:Hallo\nend stringt\n", false);
  }

  @Test
  public void arrayValue() {
    test("arrayValue", "ArrayValue", "start arrayValue\n10\n20\n1\n2\n3\n4\n100\n5\n12\n7\nend arrayValue\n", false);
  }

  @Test
  public void debug() {
    String fooTrace = "send foo\n";
    fooTrace += "debug: in foo \n";
    fooTrace += "debug: a in foo \n";
    fooTrace += "debug: a out foo \n";
    fooTrace += "debug: b in foo \n";
    fooTrace += "debug: b out foo \n";
    fooTrace += "debug: out foo \n";
    fooTrace += "foo\n";
    fooTrace += "debug: b out bar \n";
    fooTrace += "debug: out bar \n";
    fooTrace += "bar\n";
    fooTrace += "debug: a out bar \n";
    fooTrace += "debug: b in bar \n";
    fooTrace += "debug: b out bar \n";
    fooTrace += "debug: out bar \n";
    fooTrace += "bar\n";
    fooTrace += "debug: b out poh \n";
    fooTrace += "debug: out poh \n";
    fooTrace += "poh\n";
    fooTrace += "debug: b in foo \n";
    fooTrace += "debug: b out foo \n";
    fooTrace += "debug: out foo \n";
    fooTrace += "foo\n";
    fooTrace += "debug: b out bar \n";
    fooTrace += "debug: out bar \n";
    fooTrace += "bar\n";

    String barTrace = "send bar\n";
    barTrace += "debug: in bar \n";
    barTrace += "debug: a in bar \n";
    barTrace += "debug: a out bar \n";
    barTrace += "debug: b in bar \n";
    barTrace += "debug: b out bar \n";
    barTrace += "debug: out bar \n";
    barTrace += "bar\n";
    barTrace += "debug: b out poh \n";
    barTrace += "debug: out poh \n";
    barTrace += "poh\n";
    barTrace += "debug: a out poh \n";
    barTrace += "debug: b in poh \n";
    barTrace += "debug: b out bar \n";
    barTrace += "debug: out bar \n";
    barTrace += "bar\n";
    barTrace += "debug: b in bar \n";
    barTrace += "debug: b out bar \n";
    barTrace += "debug: out bar \n";
    barTrace += "bar\n";
    barTrace += "debug: b out poh \n";
    barTrace += "debug: out poh \n";
    barTrace += "poh\n";

    String pohTrace = "send poh\n";
    pohTrace += "debug: in poh \n";
    pohTrace += "debug: a in poh \n";
    pohTrace += "debug: a out bar \n";
    pohTrace += "debug: b in bar \n";
    pohTrace += "debug: b out bar \n";
    pohTrace += "debug: out bar \n";
    pohTrace += "bar\n";
    pohTrace += "debug: b out poh \n";
    pohTrace += "debug: out poh \n";
    pohTrace += "poh\n";
    pohTrace += "debug: b in poh \n";
    pohTrace += "debug: b out bar \n";
    pohTrace += "debug: out bar \n";
    pohTrace += "bar\n";

    test("debug", "Debug", "start debug\n" + fooTrace + barTrace + pohTrace + "end debug\n", true);
  }


  @Test
  public void jump() {
    String trace = "";
    trace += "construct\n";
    trace += "in 1\n";
    trace += "in 2\n";
    trace += "in 3\n";
    trace += "out\n";
    trace += "in 4\n";
    trace += "in 5\n";
    trace += "in 6\n";
    trace += "out\n";
    trace += "destruct\n";

    test("jump", "Jump", "start jump\n" + trace + "end jump\n", false);
  }

  @Test
  public void transOrder2() {
    String trace = "";
    trace += "construct\n";
    trace += "tick\n";
    trace += "0\n";
    trace += "destruct\n";

    test("transOrder2", "TransOrder2", "start transOrder2\n" + trace + "end transOrder2\n", false);
  }

  public void test(String file, String comp, String output, boolean debugMsg) {
    compile(file, comp, true, debugMsg, false);
    compilePas(file);
    run(file, output);
  }

  private void run(String name, String output) {
    String cmd = outdir + name;
    try {
      Process p;
      p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      Assert.assertEquals(readMsg(p), output);
      assert (p.exitValue() == 0);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void compilePas(String name) {
    String cmd = "fpc -Mobjfpc " + getRootdir() + name + ".pas -FE" + outdir + " -Fu" + outdir + " -Fl" + outdir + " -Fi" + outdir;
    try {
      Process p;
      p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      if (p.exitValue() != 0) {
        printMsg(p);
        throw new RuntimeException("FPC comile error");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
