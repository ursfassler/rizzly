package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import main.ClaOption;
import main.Main;

import common.Designator;

abstract public class BaseTest {
  public final String outdir = getRootdir() + "output" + File.separator;
  public final String cfile = outdir + "inst.c";
  private boolean strict = true;

  protected abstract String getRootdir();

  public void compile(String file, String comp, boolean compileCfile, boolean debugEvent, boolean lazyModelCheck) {
    cleanup();
    ClaOption opt = new ClaOption();
    opt.init(getRootdir(), new Designator(file, comp), debugEvent, lazyModelCheck);
    Main.compile(opt);
    if (compileCfile) {
      compileC();
    }
  }

  public void setStrict(boolean strict) {
    this.strict = strict;
  }

  public void compileC() {
    // String cmd = "gcc -pedantic -ansi -Werror -Wall -Wextra -c " + cfile + " -o " + outdir + "libinst.a";
    String cmd = "gcc -pedantic -ansi -c " + cfile + " -o " + outdir + "libinst.a";
    if (strict) {
      cmd += " -Werror";
    }
    try {
      Process p;
      p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      if (p.exitValue() != 0) {
        printMsg(p);
        throw new RuntimeException("Comile error");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void printMsg(Process p) throws IOException {
    BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
    BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
    String line;
    while ((line = bri.readLine()) != null) {
      System.out.println(line);
    }
    bri.close();
    while ((line = bre.readLine()) != null) {
      System.out.println(line);
    }
    bre.close();
    System.out.println("Compiled: " + p.exitValue());
  }

  protected String readMsg(Process p) throws IOException {
    BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
    BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
    String line;
    StringBuilder sb = new StringBuilder();
    while ((line = bri.readLine()) != null) {
      sb.append(line);
      sb.append("\n");
    }
    bri.close();
    while ((line = bre.readLine()) != null) {
      sb.append(line);
      sb.append("\n");
    }
    bre.close();
    return sb.toString();
  }

  public void cleanup() {
    File dir = new File(outdir);
    delete(dir);
  }

  private static void delete(File file) {
    if (file.isDirectory()) {
      String files[] = file.list();
      for (String temp : files) {
        File fileDelete = new File(file, temp);
        delete(fileDelete);
      }
    }
    file.delete();
  }

}
