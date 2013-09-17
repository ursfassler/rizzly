package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import pir.other.Program;
import pir.traverser.LlvmWriter;
import util.Pair;
import util.SimpleGraph;

import common.Designator;
import common.FuncAttr;

import error.ErrorType;
import error.RError;
import error.RException;
import evl.DefTraverser;
import evl.Evl;
import evl.copy.Copy;
import evl.doc.StreamWriter;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.other.Component;
import evl.other.Named;
import evl.other.RizzlyProgram;
import evl.traverser.CHeaderWriter;
import evl.traverser.DepCollector;
import evl.traverser.EnumReduction;
import evl.type.TypeRef;
import evl.type.base.EnumElement;
import evl.type.composed.NamedElement;
import evl.variable.Variable;
import fun.other.Namespace;
import fun.toevl.FunToEvl;

//TODO implement unions as they are supposed to be (no more waste of memory)
//TODO type check case ranges (do not allow case options out of range)
//TODO pass records by reference to functions (test case rec2)
//TODO add switch to use only C compatible arguments (no i7 types, only i8, i16, i32, etc.) /test case while2)
//TODO remove unused statements (in evl); this hopefully removes (unused) VarDefStmt OR remove VarDefStmt if not defining an composed type
//TODO add compiler self tests:
//TODO -- check that no references to old stuff exists (check that parent of every object is in the namespace tree)
//TODO -- do name randomization and compile to see if references go outside
//TODO add compiler switch to select backend (like --backend=llvm --backend=funHtmlDoc)
public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) {
    ClaOption opt = new ClaOption();
    if( !opt.parse(args) ) {
      System.exit(-2);
      return;
    }

    compile(opt);
    try {
      // compile(opt);
    } catch( RException err ) {
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
      ( new File(debugdir) ).mkdirs();
      ( new File(outdir) ).mkdirs();
      ( new File(docdir) ).mkdirs();
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
    evl.other.RizzlyProgram prg = MainEvl.doEvl(opt, debugdir, aclasses, root,debugNames);

    {
      evl.other.RizzlyProgram head = makeHeader(prg, debugdir);
      evl.traverser.Renamer.process(head);
      printCHeader(outdir, head,debugNames);
    }
    
    EnumReduction.process( prg, debugdir );

    evl.doc.PrettyPrinter.print(prg, debugdir + "beforePir.rzy", true);
    Program prog = (Program) evl.traverser.ToPir.process(prg, debugdir);
    MainPir.doPir(prog, debugdir);
    LlvmWriter.print(prog, outdir + prog.getName() + ".ll", false);

    compileLlvm(outdir + prg.getName() + ".ll", outdir + prg.getName() + ".s");
  }

  private static void compileLlvm(String llvmFile, String asmFile) {
    String cmd = "llc " + llvmFile + " -o " + asmFile;
    //TODO use strict?
    try {
      Process p;
      p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
      if( p.exitValue() != 0 ) {
        printMsg(p);
        throw new RuntimeException("Comile error");
      }
    } catch( InterruptedException e ) {
      e.printStackTrace();
    } catch( IOException e ) {
      e.printStackTrace();
    }
  }

  private static void printMsg(Process p) throws IOException {
    BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
    BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
    String line;
    while( ( line = bri.readLine() ) != null ) {
      System.out.println(line);
    }
    bri.close();
    while( ( line = bre.readLine() ) != null ) {
      System.out.println(line);
    }
    bre.close();
    System.out.println("Compiled: " + p.exitValue());
  }

  private static void printCHeader(String outdir, RizzlyProgram cprog,List<String> debugNames) {
    String cfilename = outdir + cprog.getName() + ".h";
    CHeaderWriter cwriter = new CHeaderWriter(debugNames);
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch( FileNotFoundException e ) {
      e.printStackTrace();
    }
  }

  private static void toposort(List<evl.type.Type> list) {
    SimpleGraph<evl.type.Type> g = new SimpleGraph<evl.type.Type>(list);
    for( evl.type.Type u : list ) {
      Set<evl.type.Type> vs = getDirectUsedTypes(u);
      for( evl.type.Type v : vs ) {
        g.addEdge(u, v);
      }
    }

    ArrayList<evl.type.Type> old = new ArrayList<evl.type.Type>(list);
    int size = list.size();
    list.clear();
    LinkedList<evl.type.Type> nlist = new LinkedList<evl.type.Type>();
    TopologicalOrderIterator<evl.type.Type, Pair<evl.type.Type, evl.type.Type>> itr = new TopologicalOrderIterator<evl.type.Type, Pair<evl.type.Type, evl.type.Type>>(g);
    while( itr.hasNext() ) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<evl.type.Type> diff = new ArrayList<evl.type.Type>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert ( size == list.size() );
  }

  private static Set<evl.type.Type> getDirectUsedTypes(evl.type.Type u) {
    DefTraverser<Void, Set<evl.type.Type>> getter = new DefTraverser<Void, Set<evl.type.Type>>() {

      @Override
      protected Void visitTypeRef(TypeRef obj, Set<evl.type.Type> param) {
        param.add(obj.getRef());
        return null;
      }
    };
    Set<evl.type.Type> vs = new HashSet<evl.type.Type>();
    getter.traverse(u, vs);
    return vs;
  }

  private static RizzlyProgram makeHeader(RizzlyProgram prg, String debugdir) {
    RizzlyProgram ret = new RizzlyProgram(prg.getRootdir(), prg.getName());
    Set<Evl> anchor = new HashSet<Evl>();
    for( FunctionBase func : prg.getFunction() ) {
      if( func.getAttributes().contains(FuncAttr.Public) ) {
        boolean hasBody = func instanceof FuncWithBody;
        assert ( func.getAttributes().contains(FuncAttr.Extern) || hasBody );
        for( Variable arg : func.getParam() ) {
          anchor.add(arg.getType());
        }
        if( func instanceof FuncWithReturn ) {
          anchor.add(( (FuncWithReturn) func ).getRet());
        }
        if( hasBody ) {
          if( func instanceof FuncWithReturn ) {
            FuncProtoRet proto = new FuncProtoRet(func.getInfo(), func.getName(), func.getParam());
            proto.setRet(( (FuncWithReturn) func ).getRet());
            ret.getFunction().add(proto);
          } else {
            FuncProtoVoid proto = new FuncProtoVoid(func.getInfo(), func.getName(), func.getParam());
            ret.getFunction().add(proto);
          }
        } else {
          ret.getFunction().add(func);
        }
      }
    }

    Set<Named> dep = DepCollector.process(anchor);

    for( Named itr : dep ) {
      if( itr instanceof evl.type.Type ) {
        ret.getType().add((evl.type.Type) itr);
      } else if( itr instanceof NamedElement ) {
        // element of record type
      } else if( itr instanceof EnumElement ) {
        // element of enumerator type
      } else {
        RError.err(ErrorType.Fatal, itr.getInfo(), "Object should not be used in header file: " + itr.getClass().getCanonicalName());
      }
    }

    RizzlyProgram cpy = Copy.copy(ret);
    
    toposort(cpy.getType().getList());

    return cpy;
  }

}
