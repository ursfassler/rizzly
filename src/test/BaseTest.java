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
  public final String cFile = outdir + "inst.c";
  private boolean strict = true;

  protected abstract String getRootdir();

  public void compile(String filename, TestSteps steps, boolean debugEvent, boolean lazyModelCheck) {
    cleanup();
    ClaOption opt = new ClaOption();
    String testcase = String.valueOf(filename.charAt(0)).toUpperCase() + filename.substring(1, filename.length());
    String ns = filename;
    opt.init(getRootdir(), new Designator(ns, testcase), debugEvent, lazyModelCheck);
    Main.compile(opt);

    boolean compileBinary = false;
    boolean execute = false;
    boolean compileNative = false;

    switch (steps) {
    case EXECUTE:
      execute = true;
    case COMPILE_TO_BIN:
      compileBinary = true;
    case COMPILE_TO_ASM:
      compileNative = true;
    }

    if (compileNative) {
      compileLlvm();
    }
    if (compileBinary) {
      compileTest(filename);
    }
    if (execute) {
      executeTest(filename);
    }
  }

  @Deprecated
  public void compile(String namespace, String comp, boolean compileCfile, boolean debugEvent, boolean lazyModelCheck) {
    cleanup();
    ClaOption opt = new ClaOption();
    opt.init(getRootdir(), new Designator(namespace, comp), debugEvent, lazyModelCheck);
    Main.compile(opt);

    if (compileCfile) {
      compileLlvm();
    }
  }

  public void setStrict(boolean strict) {
    this.strict = strict;
  }

  public void compileLlvm() {
    // String cmd = "gcc -pedantic -ansi -Werror -Wall -Wextra -c " + cFile + " -o " + outdir + "libinst.a";
    // TODO use strict again
    String cmd = "gcc -pedantic -ansi -Wall -Wextra -c " + cFile + " -o " + outdir + "libinst.a";
    // TODO use strict? yes
    // TODO use additional code checker
    execute(cmd, "could not compile c file");
  }

  private void compileTest(String testcase) {
    String flags = "-Wall -Werror";
    String cmd = "gcc " + flags + " " + getRootdir() + testcase + ".c" + " " + outdir + "libinst.a" + " -o" + outdir + "inst";
    execute(cmd, "could not compile test case");
  }

  private void executeTest(String testcase) {
    String cmd = outdir + "inst";
    execute(cmd, "test case failed");
  }

  private void execute(String cmd, String msg) throws RuntimeException {
    try {
      Process p;
      p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      if (p.exitValue() != 0) {
        printMsg(p);
        throw new RuntimeException(msg);
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
