package test;

import org.junit.Test;


public class ArrayTest extends BaseTest {

  @Override
  protected String getRootdir() {
    return "/home/urs/projekte/rizzly/compiler/test/array/";
  }

  @Test
  public void copy1() {
    compile("copy1", "Copy", true,false,false);
  }

  @Test
  public void copy2() {
    compile("copy2", "Copy", true,false,false);
  }

}
