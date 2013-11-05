package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import pir.other.Program;
import pir.traverser.PirPrinter;
import pir.traverser.RangeReplacer;
import pir.traverser.ToC;
import util.Pair;
import util.SimpleGraph;
import cir.function.LibFunction;
import cir.library.CLibrary;
import cir.other.FuncVariable;
import cir.traverser.BlockReduction;
import cir.traverser.BoolToEnum;
import cir.traverser.CArrayCopy;
import cir.traverser.CWriter;
import cir.traverser.FpcHeaderWriter;
import cir.traverser.Renamer;
import cir.traverser.VarDeclToTop;
import cir.type.PointerType;
import cir.type.Type;
import cir.type.TypeRef;
import cir.type.UIntType;
import cir.type.VoidType;

import common.Designator;
import common.FuncAttr;

import error.RException;
import evl.doc.StreamWriter;
import evl.other.Component;
import fun.other.Namespace;
import fun.toevl.FunToEvl;

//TODO type check case ranges (do not allow case options out of range)
//TODO pass records by reference to functions (test case rec2)
//TODO add compiler self tests:
//TODO -- check that no references to old stuff exists (check that parent of every object is in the namespace tree)
//TODO -- do name randomization and compile to see if references go outside
//TODO add compiler switch to select backend (like --backend=ansiC --backend=funHtmlDoc)
//TODO check metadata parser
//TODO check for zero bevore division
//TODO check range by user input
//TODO check if event handling is in progress when starting event handling
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

    compile(opt);
    try {
      // compile(opt);
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
    evl.doc.PrettyPrinter.print(aclasses, debugdir + "afterFun.rzy", true);
    evl.other.Component root;
    {
      ArrayList<String> nl = opt.getRootComp().toList();
      nl.remove(nl.size() - 1);
      nl.add(fret.first);
      root = (Component) aclasses.getChildItem(nl);
    }

    ArrayList<String> debugNames = new ArrayList<String>();
    evl.other.RizzlyProgram prg = MainEvl.doEvl(opt, outdir, debugdir, aclasses, root, debugNames);

    evl.doc.PrettyPrinter.print(prg, debugdir + "beforePir.rzy", true);
    Program prog = (Program) evl.traverser.ToPir.process(prg);

    cir.other.Program cprog = makeC(debugdir, prog);

    printC(outdir, prg.getName(), cprog);

    // TODO reimplement
    // printFpcHeader(outdir, prg.getName(), cprog);
  }

  private static cir.other.Program makeC(String debugdir, Program prog) {
    // Changes needed to convert into C

    // XXX probably not really needed
    // CaserangeReduction.process(prog);

    // XXX no enums at this stage?
    // ToCEnum.process(prog);

    RangeReplacer.process(prog);

    // XXX no enums at this stage?
    // EnumElementConstPropagation.process(prog);

    PirPrinter.print(prog, debugdir + "beforeToC.rzy");

    cir.other.Program cprog = (cir.other.Program) ToC.process(prog);
    BlockReduction.process(cprog);

    makeCLibrary(cprog.getLibrary());

    BoolToEnum.process(cprog);
    CArrayCopy.process(cprog);
    VarDeclToTop.process(cprog);

    Renamer.process(cprog);

    toposort(cprog.getType());
    return cprog;
  }

  private static void makeCLibrary(List<CLibrary> list) {
    list.add(makeClibString());
  }

  private static CLibrary makeClibString() {
    Set<FuncAttr> attr = new HashSet<FuncAttr>();
    attr.add(FuncAttr.Public);

    CLibrary ret = new CLibrary("string");

    { // memcpy
      UIntType uint4 = new UIntType(4); // FIXME get singleton

      List<FuncVariable> arg = new ArrayList<FuncVariable>();
      PointerType voidp = new PointerType("voidp_t", new TypeRef(new VoidType())); // FIXME get void singleton
      arg.add(new FuncVariable("dstp", new cir.type.TypeRef(voidp)));
      arg.add(new FuncVariable("srcp", new cir.type.TypeRef(voidp)));
      arg.add(new FuncVariable("size", new cir.type.TypeRef(uint4)));
      LibFunction memcpy = new LibFunction("memcpy", new cir.type.TypeRef(voidp), arg, new HashSet<FuncAttr>(attr));
      ret.getFunction().add(memcpy);
    }

    return ret;
  }

  private static void printFpcHeader(String outdir, String name, cir.other.Program cprog) {
    String cfilename = outdir + name + ".pas";
    FpcHeaderWriter cwriter = new FpcHeaderWriter();
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void printC(String outdir, String name, cir.other.Program cprog) {
    // output c prog
    {
      String cfilename = outdir + name + ".c";
      CWriter cwriter = new CWriter();
      try {
        cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }

    // output c header file
    {
      // FIXME we already wrote a (better) header at EVL level
      /*
       * String cfilename = outdir + name + ".h"; cir.traverser.CHeaderWriter cwriter = new
       * cir.traverser.CHeaderWriter(); try { cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename))); }
       * catch (FileNotFoundException e) { e.printStackTrace(); }
       */
    }
  }

  private static void toposort(List<cir.type.Type> list) {
    SimpleGraph<cir.type.Type> g = new SimpleGraph<cir.type.Type>();
    for (cir.type.Type u : list) {
      g.addVertex(u);
      Set<cir.type.Type> vs = getDirectUsedTypes(u);
      for (Type v : vs) {
        g.addVertex(v);
        g.addEdge(u, v);
      }
    }

    ArrayList<cir.type.Type> old = new ArrayList<cir.type.Type>(list);
    int size = list.size();
    list.clear();
    LinkedList<cir.type.Type> nlist = new LinkedList<cir.type.Type>();
    TopologicalOrderIterator<cir.type.Type, Pair<cir.type.Type, cir.type.Type>> itr = new TopologicalOrderIterator<cir.type.Type, Pair<cir.type.Type, cir.type.Type>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<cir.type.Type> diff = new ArrayList<cir.type.Type>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static Set<cir.type.Type> getDirectUsedTypes(cir.type.Type u) {
    cir.DefTraverser<Void, Set<cir.type.Type>> getter = new cir.DefTraverser<Void, Set<cir.type.Type>>() {
      @Override
      protected Void visitTypeRef(cir.type.TypeRef obj, Set<cir.type.Type> param) {
        param.add(obj.getRef());
        return null;
      }
    };
    Set<cir.type.Type> vs = new HashSet<cir.type.Type>();
    getter.traverse(u, vs);
    return vs;
  }

}
