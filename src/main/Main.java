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
import pir.traverser.BooleanReplacer;
import pir.traverser.CaserangeReduction;
import pir.traverser.ComplexWriterReduction;
import pir.traverser.EnumElementConstPropagation;
import pir.traverser.GlobalReadExtracter;
import pir.traverser.GlobalWriteExtracter;
import pir.traverser.LlvmWriter;
import pir.traverser.PirPrinter;
import pir.traverser.RangeReplacer;
import pir.traverser.ReferenceReadReduction;
import pir.traverser.Renamer;
import pir.traverser.ToCEnum;
import pir.traverser.ValueConverter;
import pir.traverser.ValueExtender;
import util.Pair;
import util.SimpleGraph;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType;
import common.Designator;
import common.FuncAttr;

import error.RException;
import evl.doc.StreamWriter;
import evl.other.Component;
import fun.other.Namespace;
import fun.toevl.FunToEvl;

//TODO remove unused statements (in evl); this hopefully removes (unused) VarDefStmt

//TODO add compiler self tests:
//TODO -- check that no references to old stuff exists (check that parent of every object is in the namespace tree)
//TODO -- do name randomization and compile to see if references go outside

//TODO add compiler switch to select backend (like --backend=ansic --backend=funHtmlDoc)

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
    evl.other.Component root;
    {
      ArrayList<String> nl = opt.getRootComp().toList();
      nl.remove(nl.size() - 1);
      nl.add(fret.first);
      root = (Component) aclasses.getChildItem(nl);
    }

    evl.other.RizzlyProgram prg = MainEvl.doEvl(opt, debugdir, aclasses, root);

    evl.doc.PrettyPrinter.print(prg, debugdir + "beforePir.rzy");
    Program prog = (Program) evl.traverser.ToPir.process(prg);

    ValueConverter.process(prog);
    RangeReplacer.process(prog);
    BooleanReplacer.process(prog);
    ComplexWriterReduction.process(prog);
    ReferenceReadReduction.process(prog);
    GlobalReadExtracter.process(prog);
    GlobalWriteExtracter.process(prog);
    ValueExtender.process(prog);
    
    Renamer.process(prog);
    
    LlvmWriter.print(prog, outdir + prg.getName() + ".ll");

//    cir.other.Program cprog = makeC(debugdir, prog);
//
//    printC(outdir, prg.getName(), cprog);
//    printFpcHeader(outdir, prg.getName(), cprog);
  }

  private static void makeCLibrary(List<CLibrary> list) {
    list.add(makeClibString());
  }

  private static CLibrary makeClibString() {
    Set<FuncAttr> attr = new HashSet<FuncAttr>();
    attr.add(FuncAttr.Public);

    CLibrary ret = new CLibrary("string");

    { // memcpy
      List<FuncVariable> arg = new ArrayList<FuncVariable>();
      Pointer voidp = new Pointer("voidp_t", new VoidType());
      arg.add(new FuncVariable("dstp", voidp));
      arg.add(new FuncVariable("srcp", voidp));
      arg.add(new FuncVariable("size", new IntType(false, 4)));
      LibFunction memcpy = new LibFunction("memcpy", voidp, arg, new HashSet<FuncAttr>(attr));
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
      String cfilename = outdir + name + ".h";
      CHeaderWriter cwriter = new CHeaderWriter();
      try {
        cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  private static void toposort(List<Type> list) {
    SimpleGraph<Type> g = new SimpleGraph<Type>();
    for (Type u : list) {
      g.addVertex(u);
      List<Type> vs = getDirectUsedTypes(u);
      for (Type v : vs) {
        g.addEdge(u, v);
      }
    }

    ArrayList<Type> old = new ArrayList<Type>(list);
    int size = list.size();
    list.clear();
    LinkedList<Type> nlist = new LinkedList<Type>();
    TopologicalOrderIterator<Type, Pair<Type, Type>> itr = new TopologicalOrderIterator<Type, Pair<Type, Type>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<Type> diff = new ArrayList<Type>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static List<Type> getDirectUsedTypes(Type u) {
    cir.traverser.Getter<Type, Void> getter = new cir.traverser.Getter<Type, Void>() {
      @Override
      protected Void visitNamedElement(NamedElement obj, Void param) {
        return add(obj.getType());
      }

      @Override
      protected Void visitTypeAlias(TypeAlias obj, Void param) {
        return add(obj.getRef());
      }

      @Override
      protected Void visitArrayType(ArrayType obj, Void param) {
        return add(obj.getType());
      }
    };
    List<Type> vs = getter.get(u, null);
    return vs;
  }

}
