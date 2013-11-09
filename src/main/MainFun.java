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
import parser.FileParser;
import util.Pair;
import util.SimpleGraph;

import common.Designator;
import common.ElementInfo;

import fun.doc.DepGraph;
import fun.doc.DocWriter;
import fun.doc.PrettyPrinter;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.knowledge.KnowledgeBase;
import fun.other.Component;
import fun.other.Generator;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.other.SymbolTable;
import fun.traverser.DeAlias;
import fun.traverser.EnumLinkReduction;
import fun.traverser.Linker;
import fun.traverser.Memory;
import fun.traverser.NamespaceLinkReduction;
import fun.traverser.StateLinkReduction;
import fun.traverser.TypeEvalReplacer;
import fun.traverser.spezializer.EvalTo;
import fun.traverser.spezializer.Specializer;
import fun.type.Type;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.VoidType;
import fun.type.template.ArrayTemplate;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeTypeTemplate;
import fun.variable.ConstGlobal;
import fun.variable.StateVariable;

public class MainFun {
  private static ElementInfo info = new ElementInfo();

  public static Pair<String, Namespace> doFun(ClaOption opt, Designator rootfile, String debugdir, String docdir) {
    Collection<RizzlyFile> fileList = loadFiles(rootfile, opt.getRootPath());

    System.out.println("loaded files:");
    for (RizzlyFile f : fileList) {
      System.out.print("  ");
      System.out.print(f.getFullName());
      System.out.println();
    }

    List<Type> types = new ArrayList<Type>();
    types.addAll(genPrimitiveTypes());
    types.addAll(genPrimitiveGenericTypes());

    SymbolTable sym = new SymbolTable();
    for (Type typ : types) {
      sym.add(typ);
    }
    Linker.process(types, fileList, sym);
    Linker.process(fileList, sym);
    PrettyPrinter.print(fileList, debugdir + "linked.rzy");

    Namespace classes = new Namespace(info, "!");
    classes.addAll(types);

    for (RizzlyFile f : fileList) {
      Namespace parent = classes.forceChildPath(f.getFullName().toList());
      parent.addAll(f.getType());
      parent.addAll(f.getComp());
      parent.addAll(f.getConstant());
      parent.addAll(f.getFunction());
    }

    PrettyPrinter.print(classes, debugdir + "pretty.rzy");

    KnowledgeBase knowledgeBase = new KnowledgeBase(classes, fileList, debugdir);

    NamespaceLinkReduction.process(classes);
    StateLinkReduction.process(classes, knowledgeBase);
    EnumLinkReduction.process(classes, knowledgeBase);

    PrettyPrinter.print(classes, debugdir + "linkreduced.rzy");

    Named root = classes.getChildItem(opt.getRootComp().toList());
    DocWriter.print(fileList, new KnowledgeBase(classes, fileList, docdir)); // TODO reimplement

    Component nroot = evaluate(root, classes, debugdir, fileList);
    DeAlias.process(classes);

    PrettyPrinter.print(classes, debugdir + "evaluated.rzy");
    removeUnused(classes, nroot);
    PrettyPrinter.print(classes, debugdir + "stripped.rzy");
    printDepGraph(debugdir + "rdep.gv", classes, nroot);
    return new Pair<String, Namespace>(nroot.getName(), classes);
  }

  private static void printDepGraph(String debugdir, Namespace classes, Named root) {
    SimpleGraph<Named> g = DepGraph.build(classes);
    StateVariable instVar = new StateVariable(new ElementInfo(), "!inst", null);
    g.addVertex(instVar);
    g.addEdge(instVar, root);
    try {
      @SuppressWarnings("resource")
      HtmlGraphWriter<Named, Pair<Named, Named>> writer = new HtmlGraphWriter<Named, Pair<Named, Named>>(new joGraph.Writer(new PrintStream(debugdir))) {
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

  private static void removeUnused(Namespace classes, Named root) {
    SimpleGraph<Named> g = DepGraph.build(root);
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

  private static Component evaluate(Named root, Namespace classes, String debugdir, Collection<RizzlyFile> fileList) {
    KnowledgeBase kb = new KnowledgeBase(classes, fileList, debugdir);

    // { // Cycle detection
    // SimpleGraph<Named> g = DepGraph.build(classes);
    // CycleDetector<Named, Pair<Named, Named>> cd = new CycleDetector<Named, Pair<Named, Named>>(g);
    // Set<Named> cycle = cd.findCycles();
    // if (!cycle.isEmpty()) {
    // for (Named v : cycle) {
    // RError.err(ErrorType.Hint, v.getInfo(), "Dependency cycle found, invovling type: " + v.getName());
    // }
    // RError.err(ErrorType.Error, "Dependency cycle found in types");
    // }
    // }

    {
      {
        List<ConstGlobal> gconst = classes.getItems(ConstGlobal.class, true);
        for (ConstGlobal itr : gconst) {
          Type type = (Type) EvalTo.any(itr.getType(), kb);
          itr.setType(new Reference(itr.getType().getInfo(), type));
        }
      }

      {
        TypeEvalReplacer replacer = new TypeEvalReplacer(kb);

        for (RizzlyFile f : fileList) {
          List<Generator> itms = new ArrayList<Generator>();
          itms.addAll(f.getType().getList());
          itms.addAll(f.getFunction().getList());
          itms.addAll(f.getComp().getList());
          for (Generator itr : itms) {
            if (itr.getTemplateParam().isEmpty()) {
              replacer.traverse(itr, new Memory());
            }
          }
        }
      }

      Named nroot = Specializer.process((Generator) root, new ArrayList<Expression>(), root.getInfo(), kb);
      return (Component) nroot;
    }
  }

  private static List<Type> genPrimitiveTypes() {
    List<Type> ret = new ArrayList<Type>();
    ret.add((Type) new BooleanType());
    ret.add((Type) new VoidType());
    ret.add((Type) new IntegerType());
    ret.add((Type) new NaturalType());
    ret.add((Type) new AnyType());
    ret.add((Type) new StringType());
    return ret;
  }

  private static List<Type> genPrimitiveGenericTypes() {
    List<Type> ret = new ArrayList<Type>();
    ret.add(new RangeTemplate());
    ret.add(new ArrayTemplate());
    ret.add(new TypeTypeTemplate());
    return ret;
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
      lfile.setFullName(lname);

      loaded.put(lname, lfile);
      for (Designator name : lfile.getImports()) {
        toload.add(name);
      }
    }
    return loaded.values();
  }

}
