/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import main.ClaOption;
import main.Main;

import common.Designator;

abstract public class BaseTest {
  private boolean strict = true;

  protected abstract String getRootdir();

  public void compile(String filename, TestSteps steps, boolean debugEvent, boolean lazyModelCheck) {
    cleanup();
    ClaOption opt = new ClaOption();
    String testcase = String.valueOf(filename.charAt(0)).toUpperCase() + filename.substring(1, filename.length());
    String ns = filename;
    opt.init(getRootdir(), new Designator(ns, testcase), debugEvent, lazyModelCheck);
    String outdir = Main.compile(opt);

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
      compileLlvm(outdir);
    }
    if (compileBinary) {
      compileTest(filename, outdir);
    }
    if (execute) {
      executeTest(filename, outdir);
    }
  }

  public void compileExample(String dir, String testcase) {
    cleanup();
    ClaOption opt = new ClaOption();
    StringTokenizer st = new StringTokenizer(testcase, ".");
    ArrayList<String> tc = new ArrayList<String>();
    while (st.hasMoreElements()) {
      tc.add(st.nextToken());
    }
    opt.init(getRootdir() + dir, new Designator(tc), false, false);
    String outdir = Main.compile(opt);
    compileLlvm(outdir);
  }

  @Deprecated
  public void compile(String namespace, String comp, boolean compileCfile, boolean debugEvent, boolean lazyModelCheck) {
    cleanup();
    ClaOption opt = new ClaOption();
    opt.init(getRootdir(), new Designator(namespace, comp), debugEvent, lazyModelCheck);
    String outdir = Main.compile(opt);

    if (compileCfile) {
      compileLlvm(outdir);
    }
  }

  public void setStrict(boolean strict) {
    this.strict = strict;
  }

  public void compileLlvm(String outdir) {
    // String cmd = "gcc -pedantic -ansi -Werror -Wall -Wextra -c " + cFile + " -o " + outdir + "libinst.a";
    // TODO use strict again
    String cmd = "gcc -std=c11 -pedantic -Wall -Wextra -g -c " + outdir + "inst.c -o " + outdir + "libinst.a";
    // TODO use strict? yes
    // TODO use additional code checker
    execute(cmd, "could not compile c file");
  }

  private void compileTest(String testcase, String outdir) {
    String flags = "-std=c11 -Wall -pedantic -Wall -Wextra -g";
    String cmd = "gcc " + flags + " " + getRootdir() + testcase + ".c" + " " + outdir + "libinst.a" + " -o" + outdir + "inst";
    execute(cmd, "could not compile test case");
  }

  private void executeTest(String testcase, String outdir) {
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
    File dir = new File(getRootdir() + "/output/");
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
