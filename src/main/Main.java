package main;

import java.io.File;
import java.util.ArrayList;

import util.Pair;
import cir.other.Program;
import cir.traverser.CWriter;

import common.Designator;

import error.RException;
import evl.other.Component;
import fun.other.Namespace;
import fun.toevl.FunToEvl;

//TODO set tag when writing to union
//TODO allow writing of tag of union
//TODO check that union/record is fully initialized before passing to other function
//TODO pass records by reference to functions (test case rec2)
//TODO add compiler self tests:
//TODO -- check that no references to old stuff exists (check that parent of every object is in the namespace tree)
//TODO -- do name randomization and compile to see if references go outside
//TODO add compiler switch to select backend (like --backend=ansiC --backend=funHtmlDoc)
//TODO check metadata parser
//TODO check for zero before division
//TODO check range by user input
//TODO check if event handling is in progress when starting event handling
//TODO check stuff in joGraph (and remove)

public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) {
    ClaOption opt = new ClaOption();
    if (!opt.parse(args)) {
      System.exit(-2);
      return;
    }

    // compile(opt);
    try {
      compile(opt);
    } catch (RException err) {
      System.err.println(err.getMessage());
      System.exit(-1);
    }
    System.exit(0);
  }

  public static void compile(ClaOption opt) {
    Designator rootfile;
    String debugdir;
    String outdir;
    String docdir;
    {
      debugdir = opt.getRootPath() + "debug" + File.separator;
      outdir = opt.getRootPath() + "output" + File.separator;
      docdir = opt.getRootPath() + "doc" + File.separator;
      (new File(debugdir)).mkdirs();
      (new File(outdir)).mkdirs();
      (new File(docdir)).mkdirs();
    }
    {
      ArrayList<String> nl = opt.getRootComp().toList();
      nl.remove(nl.size() - 1);
      rootfile = new Designator(nl);
    }

    Pair<String, Namespace> fret = MainFun.doFun(opt, rootfile, debugdir, docdir);
    evl.other.Namespace aclasses = FunToEvl.process(fret.second, debugdir);
    evl.traverser.PrettyPrinter.print(aclasses, debugdir + "afterFun.rzy", true);
    evl.other.Component root;
    {
      ArrayList<String> nl = opt.getRootComp().toList();
      nl.remove(nl.size() - 1);
      nl.add(fret.first);
      root = (Component) aclasses.getChildItem(nl);
    }

    ArrayList<String> debugNames = new ArrayList<String>();
    evl.other.RizzlyProgram prg = MainEvl.doEvl(opt, outdir, debugdir, aclasses, root, debugNames);

    evl.traverser.PrettyPrinter.print(prg, debugdir + "beforeCir.rzy", true);

    Program cprog = (cir.other.Program) evl.traverser.ToC.process(prg);

    cprog = MainCir.doCir(cprog, debugdir);

    CWriter.print(cprog, outdir + prg.getName() + ".c", false);
  }

}
