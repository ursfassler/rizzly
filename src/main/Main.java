package main;

import evl.other.RizzlyProgram;
import evl.type.TypeRef;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import joGraph.HtmlGraphWriter;
import joGraph.Writer;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import pir.PirObject;
import pir.cfg.CaseGoto;
import pir.cfg.Goto;
import pir.cfg.IfGoto;
import pir.cfg.ReturnExpr;
import pir.cfg.ReturnVoid;
import pir.know.KnowledgeBase;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.passes.CaseReduction;
import pir.passes.ConstPropagator;
import pir.passes.GlobalReadExtracter;
import pir.passes.LlvmIntTypeReplacer;
import pir.passes.RangeConverter;
import pir.passes.RangeReplacer;
import pir.passes.StmtSignSetter;
import pir.passes.TypecastReplacer;
import pir.passes.VarPropagator;
import pir.statement.CallAssignment;
import pir.statement.CallStmt;
import pir.statement.Statement;
import pir.statement.StoreStmt;
import pir.traverser.DependencyGraphMaker;
import pir.traverser.LlvmWriter;
import pir.traverser.OwnerMap;
import pir.traverser.Renamer;
import pir.traverser.StmtRemover;
import pir.traverser.TyperefCounter;
import pir.type.Type;
import util.GraphHelper;
import util.Pair;
import util.SimpleGraph;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType;
import common.Designator;
import common.FuncAttr;

import error.ErrorType;
import error.RError;
import error.RException;
import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.doc.StreamWriter;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.other.Component;
import evl.other.Named;
import evl.traverser.CHeaderWriter;
import evl.traverser.ClassGetter;
import evl.traverser.DepCollector;
import evl.type.base.ArrayType;
import evl.type.composed.NamedElement;
import evl.type.special.VoidType;
import evl.variable.FuncVariable;
import evl.variable.Variable;
import fun.other.Namespace;
import fun.toevl.FunToEvl;
import fun.type.base.TypeAlias;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

    evl.other.RizzlyProgram prg = MainEvl.doEvl(opt, debugdir, aclasses, root);

    {
      evl.other.RizzlyProgram head = makeHeader(prg);
      evl.traverser.Renamer.process(head);
      printCHeader(outdir,head);
    }

    evl.doc.PrettyPrinter.print(prg, debugdir + "beforePir.rzy", true);
    Program prog = (Program) evl.traverser.ToPir.process(prg, debugdir);

    LlvmWriter.print(prog, debugdir + "afterEvl.ll", true);


    KnowledgeBase kb = new KnowledgeBase(prog, debugdir);
    { // reducing Range and boolean to nosign type
      RangeConverter.process(prog, kb);
      RangeReplacer.process(prog);
      TypecastReplacer.process(prog);
      StmtSignSetter.process(prog);
      LlvmIntTypeReplacer.process(prog, kb);
    }

    LlvmWriter.print(prog, debugdir + "typeext.ll", true);

    CaseReduction.process(prog);

    GlobalReadExtracter.process(prog);
    // GlobalWriteExtracter.process(prog); //TODO do it during translation to PIR?
    // RangeExtender.process(prog);

    VarPropagator.process(prog);
    ConstPropagator.process(prog);

    { // remove unused statements
      HashMap<SsaVariable, Statement> owner = OwnerMap.make(prog);
      SimpleGraph<PirObject> g = DependencyGraphMaker.make(prog, owner);

      PirObject rootDummy = new PirObject() {
      };

      Set<Class<? extends Statement>> keep = new HashSet<Class<? extends Statement>>();
      keep.add(CallAssignment.class);
      keep.add(CallStmt.class);
      keep.add(StoreStmt.class);

      keep.add(CaseGoto.class);
      keep.add(Goto.class);
      keep.add(IfGoto.class);
      keep.add(ReturnExpr.class);
      keep.add(ReturnVoid.class);

      Set<Statement> removable = new HashSet<Statement>();

      g.addVertex(rootDummy);
      for( PirObject obj : g.vertexSet() ) {
        if( keep.contains(obj.getClass()) ) {
          g.addEdge(rootDummy, obj);
        }
        if( obj instanceof Statement ) {
          removable.add((Statement) obj);
        }
      }

      GraphHelper.doTransitiveClosure(g);
      printGraph(g, debugdir + "pirdepstmt.gv");

      removable.removeAll(g.getOutVertices(rootDummy));

      StmtRemover.process(prog, removable);
    }

    { // remove unused types
      // FIXME use dependency graph and transitive closure to remove all unused
      Map<Type, Integer> count = TyperefCounter.process(prog);
      Set<Type> removable = new HashSet<Type>();
      for( Type type : count.keySet() ) {
        if( count.get(type) == 0 ) {
          removable.add(type);
        }
      }
      prog.getType().removeAll(removable);
    }

    Renamer.process(prog);

    LlvmWriter.print(prog, outdir + prg.getName() + ".ll", false);

    compileLlvm(outdir + prg.getName() + ".ll", outdir + prg.getName() + ".s");

    // cir.other.Program cprog = makeC(debugdir, prog);
    //
    // printC(outdir, prg.getName(), cprog);
    // printFpcHeader(outdir, prg.getName(), cprog);
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

  private static void printGraph(Graph<PirObject, Pair<PirObject, PirObject>> g, String filename) {
    HtmlGraphWriter<PirObject, Pair<PirObject, PirObject>> hgw;
    try {
      hgw = new HtmlGraphWriter<PirObject, Pair<PirObject, PirObject>>(new Writer(new PrintStream(filename))) {

        @Override
        protected void wrVertex(PirObject v) {
          wrVertexStart(v);
          wrRow(v.toString());
          wrVertexEnd();
        }
      };
      hgw.print(g);
    } catch( FileNotFoundException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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

  private static void printFpcHeader(String outdir, RizzlyProgram cprog) {
    String cfilename = outdir + name + ".pas";
    FpcHeaderWriter cwriter = new FpcHeaderWriter();
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch( FileNotFoundException e ) {
      e.printStackTrace();
    }
  }

  private static void printCHeader(String outdir, RizzlyProgram cprog) {
      String cfilename = outdir + cprog.getName() + ".h";
      CHeaderWriter cwriter = new CHeaderWriter();
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
    DefTraverser<Void,Set<evl.type.Type>> getter = new DefTraverser<Void, Set<evl.type.Type>>() {
      @Override
      protected Void visitTypeRef(TypeRef obj, Set<evl.type.Type> param) {
        param.add(obj.getRef());
        return null;
      }
    };
    Set<evl.type.Type> vs = new HashSet<evl.type.Type>();
    getter.traverse(u,vs);
    return vs;
  }

  private static RizzlyProgram makeHeader(RizzlyProgram prg) {
    RizzlyProgram ret = new RizzlyProgram(prg.getRootdir(), prg.getName());
    Set<Evl> anchor = new HashSet<Evl>();
    for( FunctionBase func : prg.getFunction() ) {
      if( func.getAttributes().contains(FuncAttr.Public) ) {
        boolean hasBody = func instanceof  FuncWithBody;
        assert( func.getAttributes().contains(FuncAttr.Extern) || hasBody );
        for( Variable arg : func.getParam() ){
          anchor.add(arg.getType());
        }
        if( func instanceof FuncWithReturn ){
          anchor.add( ((FuncWithReturn)func).getRet() );
        }
        if( hasBody ){
          if( func instanceof FuncWithReturn ){
            FuncProtoRet proto = new FuncProtoRet(func.getInfo(), func.getName(), func.getParam());
            proto.setRet( ((FuncWithReturn)func).getRet() );
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
        ret.getType().add((evl.type.Type)itr);
      } else if( itr instanceof NamedElement ){
        // element of record type
      } else {
        RError.err(ErrorType.Fatal, itr.getInfo(), "Object should not be used in header file: " + itr.getClass().getCanonicalName());
      }
    }
    
    ret = Copy.copy(ret);
    
    toposort(ret.getType().getList());

    return ret;
  }
}
