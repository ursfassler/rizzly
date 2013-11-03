package test;

import main.ClaOption;
import main.Main;

import org.junit.Assert;

import common.Designator;

import error.RException;

public abstract class ErrorTest {

  protected abstract String getRootdir();

  protected void testForError(String file, String comp, String error) {
    try {
      ClaOption opt = new ClaOption();
      opt.init(getRootdir(), new Designator(file, comp), false, false);
      Main.compile(opt);
    } catch (RException err) {
      Assert.assertEquals(error, err.getMsg());
      return;
    }
    throw new RuntimeException("Error not thrown: " + error);
  }

}
