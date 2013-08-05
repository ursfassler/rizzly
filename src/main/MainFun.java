package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import joGraph.HtmlGraphWriter;

import org.jgrapht.alg.CycleDetector;

import parser.FileParser;
import util.Pair;
import util.SimpleGraph;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.doc.DepGraph;
import fun.doc.DocWriter;
import fun.doc.PrettyPrinter;
import fun.expression.Expression;
import fun.expression.reference.ReferenceLinked;
import fun.function.impl.FuncGlobal;
import fun.generator.ComponentGenerator;
import fun.generator.TypeGenerator;
import fun.knowledge.KnowledgeBase;
import fun.other.Named;
import fun.other.NamedComponent;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.symbol.SymbolTable;
import fun.traverser.ClassNameExtender;
import fun.traverser.GenfuncParamExtender;
import fun.traverser.Linker;
import fun.traverser.MakeBasicBlocks;
import fun.traverser.Memory;
import fun.traverser.NamespaceLinkReduction;
import fun.traverser.SsaMaker;
import fun.traverser.StateLinkReduction;
import fun.traverser.TypeEvalReplacer;
import fun.traverser.spezializer.ExprEvaluator;
import fun.traverser.spezializer.Specializer;
import fun.type.NamedType;
import fun.type.base.AnyType;
import fun.type.base.BaseType;
import fun.type.base.BooleanType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.VoidType;
import fun.type.genfunc.GenericArray;
import fun.type.genfunc.GenericRange;
import fun.type.genfunc.GenericTypeType;
import fun.type.genfunc.GenericUnsigned;
import fun.variable.ConstGlobal;
import fun.variable.StateVariable;

public class MainFun {
  private static ElementInfo info = new ElementInfo();

  public static Pair<String, Namespace> doFun(ClaOption opt, Designator rootfile, String debugdir, String docdir) {
    Collection<RizzlyFile> fileList = loadFiles(rootfile, opt.getRootPath());

    System.out.println("loaded files:");
    for (RizzlyFile f : fileList) {
      System.out.print("  ");
      System.out.print(f.getName());
      System.out.println();
    }

    List<NamedType> primtyp = genPrimitiveTypes();
    List<TypeGenerator> gentyp = genPrimitiveGenericTypes();

    SymbolTable<Designator, String> sym = new SymbolTable<Designator, String>();
    for (NamedType typ : primtyp) {
      sym.add(typ.getName(), new Designator(typ.getName()));
    }
    for (TypeGenerator typ : gentyp) {
      sym.add(typ.getName(), new Designator(typ.getName()));
    }
    ClassNameExtender.process(fileList, sym);

    Namespace classes = new Namespace(info, "!");
    classes.addAll(primtyp);
    classes.addAll(gentyp);

    for (RizzlyFile f : fileList) {
      Namespace parent = classes.forceChildPath(f.getName().toList());
      parent.addAll(f.getConstant());
      parent.addAll(f.getFunction());
      parent.addAll(f.getCompfunc());
    }

    PrettyPrinter.print(classes, debugdir + "pretty.rzy");

    KnowledgeBase knowledgeBase = new KnowledgeBase(classes, fileList, debugdir);
    Linker.process(classes, knowledgeBase);
    NamespaceLinkReduction.process(classes);
    StateLinkReduction.process(classes, knowledgeBase);
    GenfuncParamExtender.process(classes);

    PrettyPrinter.print(classes, debugdir + "linkreduced.rzy");

    Named root = classes.getChildItem(opt.getRootComp().toList());
    printDepGraph(debugdir + "rdep.gv", classes, root, fileList);
    DocWriter.print(fileList, new KnowledgeBase(classes, fileList, docdir));

    NamedComponent nroot = evaluate(root, classes, debugdir, fileList);

    PrettyPrinter.print(classes, debugdir + "evaluated.rzy");
    removeUnused(debugdir, classes, nroot, fileList);
    PrettyPrinter.print(classes, debugdir + "stripped.rzy");
    translateToSsa(classes, debugdir, new KnowledgeBase(classes, fileList, debugdir));
    return new Pair<String, Namespace>(nroot.getName(), classes);
  }

  private static void translateToSsa(Namespace classes, String rootdir, KnowledgeBase kb) {
    MakeBasicBlocks.process(classes, kb);
    SsaMaker.process(classes,kb);
  }

  private static void printDepGraph(String debugdir, Namespace classes, Named root, Collection<RizzlyFile> fileList) {
    SimpleGraph<Named> g = DepGraph.build(classes, new KnowledgeBase(classes, fileList, debugdir));
    g.addEdge(new StateVariable(new ElementInfo(), "!inst", null), root);
    try {
      @SuppressWarnings("resource")
      HtmlGraphWriter<Named> writer = new HtmlGraphWriter<Named>(new joGraph.Writer(new PrintStream(debugdir))) {
        @Override
        protected void wrVertex(Named v) {
          wrVertexStart(v);
          wrRow(v.getName());
          wrVertexEnd();
        }
      };
      writer.print(g);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void removeUnused(String debugdir, Namespace classes, Named root, Collection<RizzlyFile> fileList) {
    SimpleGraph<Named> g = DepGraph.build(root, new KnowledgeBase(classes, fileList, debugdir));
    removeUnused(classes, g.vertexSet());
  }

  private static void removeUnused(Namespace ns, Set<Named> keep) {
    Set<Named> remove = new HashSet<Named>();
    for (Named itr : ns) {
      if (itr instanceof Namespace) {
        removeUnused((Namespace) itr, keep);
      } else {
        if (!keep.contains(itr)) {
          remove.add(itr);
        }
      }
    }
    ns.removeAll(remove);
  }

  private static NamedComponent evaluate(Named root, Namespace classes, String debugdir, Collection<RizzlyFile> fileList) {
    KnowledgeBase kb = new KnowledgeBase(classes, fileList, debugdir);
    SimpleGraph<Named> g = DepGraph.build(classes, kb);

    { // Cycle detection
      CycleDetector<Named, Pair<Named, Named>> cd = new CycleDetector<Named, Pair<Named, Named>>(g);
      Set<Named> cycle = cd.findCycles();
      if (!cycle.isEmpty()) {
        for (Named v : cycle) {
          RError.err(ErrorType.Hint, v.getInfo(), "Dependency cycle found, invovling type: " + v.getName());
        }
        RError.err(ErrorType.Warning, "Maybe a dependency cycle found in types");
      }
    }

    {
      {
        List<ConstGlobal> gconst = classes.getItems(ConstGlobal.class, true);
        for (ConstGlobal itr : gconst) {
          NamedType type = (NamedType) ExprEvaluator.evaluate(itr.getType(), new Memory(), kb);
          itr.setType(new ReferenceLinked(itr.getInfo(), type));
        }
      }

      {
        Memory mem = new Memory();
        TypeEvalReplacer replacer = new TypeEvalReplacer(kb);
        List<FuncGlobal> gfunc = classes.getItems(FuncGlobal.class, true);
        for (FuncGlobal itr : gfunc) {
          replacer.traverse(itr, mem);
        }
      }

      NamedComponent nroot = Specializer.processComp((ComponentGenerator) root, new ArrayList<Expression>(), kb);

      return nroot;
    }
  }

  private static List<NamedType> genPrimitiveTypes() {
    List<NamedType> ret = new ArrayList<NamedType>();
    ret.add(makeNamedBaseType(new BooleanType()));
    ret.add(makeNamedBaseType(new VoidType()));
    ret.add(makeNamedBaseType(new IntegerType()));
    ret.add(makeNamedBaseType(new NaturalType()));
    ret.add(makeNamedBaseType(new AnyType()));
    ret.add(makeNamedBaseType(new StringType()));
    return ret;
  }

  private static List<TypeGenerator> genPrimitiveGenericTypes() {
    List<TypeGenerator> ret = new ArrayList<TypeGenerator>();
    ret.add(makeGenericBaseType(new GenericRange()));
    ret.add(makeGenericBaseType(new GenericUnsigned()));
    ret.add(makeGenericBaseType(new GenericArray()));
    ret.add(makeGenericBaseType(new GenericTypeType()));
    return ret;
  }

  private static NamedType makeNamedBaseType(BaseType inst) {
    assert (inst.getParamList().isEmpty());
    return new NamedType(new ElementInfo(), inst.getName(), inst);
  }

  private static TypeGenerator makeGenericBaseType(BaseType inst) {
    assert (!inst.getParamList().isEmpty());
    return new TypeGenerator(info, inst.getName(), inst.getParamList().getList(), inst);
  }

  private static Collection<RizzlyFile> loadFiles(Designator rootname, String rootdir) {
    Map<Designator, RizzlyFile> loaded = new HashMap<Designator, RizzlyFile>();

    Queue<Designator> toload = new LinkedList<Designator>();
    toload.add(rootname);

    while (!toload.isEmpty()) {
      Designator lname = toload.poll();
      if (loaded.containsKey(lname)) {
        continue;
      }
      String filename = rootdir + lname.toString(File.separator) + ClaOption.extension;
      RizzlyFile lfile = FileParser.parse(filename);
      lfile.setName(lname);

      loaded.put(lname, lfile);
      for (Designator name : lfile.getImports()) {
        toload.add(name);
      }
    }
    return loaded.values();
  }

}
