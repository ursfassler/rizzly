package main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import joGraph.HtmlGraphWriter;
import util.SimpleGraph;

import common.Designator;
import common.Direction;
import common.ElementInfo;
import common.FuncAttr;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.ReturnExpr;
import evl.composition.CompositionReduction;
import evl.composition.Connection;
import evl.composition.ImplComposition;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.doc.DepGraph;
import evl.doc.PrettyPrinter;
import evl.expression.ArrayValue;
import evl.expression.Expression;
import evl.expression.StringValue;
import evl.expression.reference.RefIndex;
import evl.expression.reference.Reference;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.impl.FuncGlobal;
import evl.function.impl.FuncPrivateVoid;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.Transition;
import evl.hfsm.doc.HfsmGraphviz;
import evl.hfsm.reduction.HfsmReduction;
import evl.hfsm.reduction.HfsmToFsm;
import evl.hfsm.reduction.SystemIfaceAdder;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowEvl;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.traverser.CallgraphMaker;
import evl.traverser.ClassGetter;
import evl.traverser.CompInstantiator;
import evl.traverser.DesCallgraphMaker;
import evl.traverser.ExprCutter;
import evl.traverser.LinkReduction;
import evl.traverser.NamespaceReduction;
import evl.traverser.OutsideReaderInfo;
import evl.traverser.OutsideWriterInfo;
import evl.traverser.SsaMaker;
import evl.traverser.debug.CompCascadeDepth;
import evl.traverser.debug.DebugIfaceAdder;
import evl.traverser.debug.MsgNamesGetter;
import evl.traverser.iocheck.IoCheck;
import evl.traverser.iocheck.StateReaderInfo;
import evl.traverser.iocheck.StateWriterInfo;
import evl.traverser.typecheck.TypeChecker;
import evl.traverser.typecheck.specific.CompInterfaceTypeChecker;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.Range;
import evl.variable.ConstGlobal;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import fun.hfsm.State;

public class MainEvl {
  private static ElementInfo info = new ElementInfo();

  public static RizzlyProgram doEvl(ClaOption opt, String debugdir, Namespace aclasses, Component root) {
    KnowledgeBase kb = new KnowledgeBase(aclasses, debugdir);

    PrettyPrinter.print(aclasses, debugdir + "ast.rzy");
    SsaMaker.process(aclasses, kb);
    PrettyPrinter.print(aclasses, debugdir + "ssa.rzy");

    typecheck(aclasses, root, debugdir);
    if (!opt.doLazyModelCheck()) {
      modelCheck(debugdir, aclasses, root, kb);
    }

    root = compositionReduction(aclasses, root);
    // TODO reimplement
    // root = hfsmReduction(root, opt, debugdir, aclasses, kb);

    PrettyPrinter.print(aclasses, debugdir + "reduced.rzy");

    // only for debugging
    // typecheck(classes, debugdir);

    // TODO or before type check?
    ExprCutter.process(aclasses, kb);
    PrettyPrinter.print(aclasses, debugdir + "expr.rzy");

    if (opt.doDebugEvent()) {
      addDebug(aclasses, (ImplElementary) root, debugdir);
    }

    RizzlyProgram prg = instantiate(root, debugdir, aclasses);
    return prg;
  }

  private static void modelCheck(String debugdir, Namespace aclasses, Component root, KnowledgeBase kb) {
    checkRoot(root, debugdir);
    checkUsefullness(aclasses);
    checkForRtcViolation(aclasses, kb);
    ioCheck(aclasses, kb);
  }

  /**
   * Checks that only allowed functions change state or write output
   */
  private static void ioCheck(Namespace aclasses, KnowledgeBase kb) {
    SimpleGraph<Evl> cg = CallgraphMaker.make(aclasses, kb);
    // printGraph(kb.getRootdir() + "callgraph.gv", cg);

    Map<Evl, Boolean> writes = new HashMap<Evl, Boolean>();
    Map<Evl, Boolean> reads = new HashMap<Evl, Boolean>();
    Map<Evl, Boolean> outputs = new HashMap<Evl, Boolean>();
    Map<Evl, Boolean> inputs = new HashMap<Evl, Boolean>();
    for (Evl header : cg.vertexSet()) {
      writes.put(header, StateWriterInfo.get(header));
      reads.put(header, StateReaderInfo.get(header));
      if (header instanceof FunctionBase) {
        inputs.put(header, OutsideReaderInfo.get((FunctionBase) header));
        outputs.put(header, OutsideWriterInfo.get((FunctionBase) header));
      } else {
        inputs.put(header, false);
        outputs.put(header, false);
      }
    }
    // print(writes, reads, outputs, inputs);

    doTransitiveClosure(cg);

    writes = doTransStuff(cg, writes);
    reads = doTransStuff(cg, reads);
    outputs = doTransStuff(cg, outputs);
    inputs = doTransStuff(cg, inputs);

    // System.out.println("-------");
    // print(writes, reads, outputs, inputs);

    IoCheck ioCheck = new IoCheck(writes, reads, outputs, inputs);
    ioCheck.check(ClassGetter.get(FunctionBase.class, aclasses));
    ioCheck.check(ClassGetter.get(Transition.class, aclasses));
  }

  private static <T extends Evl> Map<T, Boolean> doTransStuff(SimpleGraph<T> cg, Map<? extends Evl, Boolean> does) {
    Map<T, Boolean> ret = new HashMap<T, Boolean>();
    for (T u : cg.vertexSet()) {
      boolean doThings = does.get(u);
      for (Evl v : cg.getOutVertices(u)) {
        doThings |= does.get(v);
      }
      ret.put(u, doThings);
    }
    return ret;
  }

  @SuppressWarnings("unused")
  private static void print(Map<Evl, Boolean> writes, Map<Evl, Boolean> reads, Map<Evl, Boolean> outputs, Map<Evl, Boolean> inputs) {
    for (Evl header : writes.keySet()) {
      String rwio = "";
      rwio += reads.get(header) ? "r" : " ";
      rwio += writes.get(header) ? "w" : " ";
      rwio += inputs.get(header) ? "i" : " ";
      rwio += outputs.get(header) ? "o" : " ";
      System.out.print(rwio);
      System.out.print("\t");
      System.out.print(header);
      System.out.println();
    }
  }

  // TODO do it more elegant (i.e. use provided algorithm)
  private static <T extends Object> void doTransitiveClosure(SimpleGraph<T> cg) {
    for (T a : cg.vertexSet()) {
      int outdeg = 0;
      while (outdeg != cg.outDegreeOf(a)) {
        outdeg = cg.outDegreeOf(a);
        for (T b : cg.getOutVertices(a)) {
          for (T c : cg.getOutVertices(b)) {
            cg.addEdge(a, c);
          }
        }
      }
    }
  }

  private static void typecheck(Namespace aclasses, Component root, String rootdir) {
    KnowledgeBase kb = new KnowledgeBase(aclasses, rootdir);
    TypeChecker.processList(aclasses, kb); // check statements
    CompInterfaceTypeChecker.process(aclasses, kb); // check interfaces against implementation

    // TODO VarInit should be checked when translated into SSA
    // VarInitCheck.process(aclasses);
  }

  // TODO provide a call/connection graph in the error message
  /**
   * Checks that Run To Completion semantic is not violated, i.e. that calls on component is a DAG
   * 
   * @param kb
   */
  private static void checkForRtcViolation(Namespace aclasses, KnowledgeBase kb) {
    {
      List<ImplElementary> elemset = ClassGetter.get(ImplElementary.class, aclasses);
      for (ImplElementary elem : elemset) {
        SimpleGraph<Designator> cg = DesCallgraphMaker.make(elem);
        checkRtcViolation(cg, 3, elem.getInfo());
      }
    }
    {
      List<ImplComposition> elemset = ClassGetter.get(ImplComposition.class, aclasses);
      for (ImplComposition elem : elemset) {
        SimpleGraph<Designator> cg = makeCallgraph(elem.getConnection());
        checkRtcViolation(cg, 2, elem.getInfo());
      }
    }
    // no need to check for hfsm since they can not have sub-components
  }

  private static SimpleGraph<Designator> makeCallgraph(List<Connection> connection) {
    SimpleGraph<Designator> ret = new SimpleGraph<Designator>();
    for (Connection con : connection) {
      Designator src = con.getEndpoint(Direction.in).getDes();
      Designator dst = con.getEndpoint(Direction.out).getDes();
      ret.addEdge(src, dst);
    }
    return ret;
  }

  private static void checkRtcViolation(SimpleGraph<Designator> cg, int n, ElementInfo info) {
    doTransitiveClosure(cg);
    for (Designator v : cg.vertexSet()) {
      cg.removeEdge(v, v); // this is not what we are looking for but recursive calls
    }
    SimpleGraph<String> compcall = new SimpleGraph<String>();
    for (Designator v : cg.vertexSet()) {
      if (v.size() == n) {
        for (Designator u : cg.getOutVertices(v)) {
          if (u.size() == n) {
            String vcomp = v.toList().get(0);
            String ucomp = u.toList().get(0);
            compcall.addEdge(vcomp, ucomp);
          }
        }
      }
    }
    doTransitiveClosure(compcall);
    ArrayList<String> vs = new ArrayList<String>(compcall.vertexSet());
    Collections.sort(vs);
    for (String v : vs) {
      if (compcall.containsEdge(v, v)) {
        RError.err(ErrorType.Error, info, "Violation of run to completion detected for component: " + v);
      }
    }
  }

  // TODO do test in FUN part, issues warnings only once for parameterized components.
  /**
   * Checks if there is a input and output data flow. Gives a warning otherwise.
   * 
   * A component with only input or output data flow can not do a lot (or not more than a global function can do).
   * 
   * @param aclasses
   */
  private static void checkUsefullness(Namespace aclasses) {
    for (Component comp : ClassGetter.get(Component.class, aclasses)) {
      boolean in = false;
      boolean out = false;
      List<FunctionBase> outFunc = ClassGetter.getAll(FunctionBase.class, getInterfaces(comp.getIface(Direction.out).getList()));
      List<FunctionBase> inFunc = ClassGetter.getAll(FunctionBase.class, getInterfaces(comp.getIface(Direction.in).getList()));
      for (FunctionBase itr : inFunc) {
        if (itr instanceof FuncWithReturn) {
          out = true;
        } else {
          in = true;
        }
      }
      for (FunctionBase itr : outFunc) {
        if (itr instanceof FuncWithReturn) {
          in = true;
        } else {
          out = true;
        }
      }
      if (!in && !out) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + comp.getName() + " has no input and no output data flow");
      }
      if (in && !out) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + comp.getName() + " has no output data flow");
      }
      if (!in && out) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + comp.getName() + " has no input data flow");
      }
    }
  }

  private static Set<Interface> getInterfaces(Collection<IfaceUse> list) {
    Set<Interface> ret = new HashSet<Interface>();
    for (IfaceUse var : list) {
      ret.add(var.getLink());
    }
    return ret;
  }

  // TODO add a compiler flag to change the error into a warning. Like --lazyDeveloper or so
  /**
   * Throws an error if an interface in the top component contains a query. Because we have to be sure that queries are
   * implement correctly.
   * 
   * @param root
   * @param rootdir
   */
  private static void checkRoot(Component root, String rootdir) {
    for (IfaceUse itr : root.getIface(Direction.out)) {
      Interface iface = itr.getLink();
      for (FunctionBase func : iface.getPrototype()) {
        if (func instanceof FuncWithReturn) {
          RError.err(ErrorType.Error, itr.getInfo(), "Top component is not allowed to have queries in output (" + itr.getName() + "." + func.getName() + ")");
        }
      }
    }
  }

  private static Component compositionReduction(Namespace aclasses, Component root) {
    Map<ImplComposition, ImplElementary> map = CompositionReduction.process(aclasses);
    Relinker.relink(aclasses, map);
    if (map.containsKey(root)) {
      root = map.get(root);
    }
    return root;
  }

  private static Component hfsmReduction(Component root, ClaOption opt, String debugdir, Namespace classes, KnowledgeBase kb) {
    addConDestructor(classes, debugdir, kb);

    HfsmGraphviz.print(classes, debugdir + "hfsm.gv");
    HfsmToFsm.process(classes, kb);
    PrettyPrinter.print(classes, debugdir + "fsm.rzy");

    Map<ImplHfsm, ImplElementary> map = HfsmReduction.process(classes, new KnowledgeBase(classes, opt.getRootPath()));
    Relinker.relink(classes, map);
    // Linker.process(classes, knowledgeBase);

    if (map.containsKey(root)) {
      root = map.get(root);
    }
    return root;
  }

  private static void addConDestructor(Namespace classes, String debugdir, KnowledgeBase kb) {
    Interface debugIface = new Interface(info, SystemIfaceAdder.IFACE_TYPE_NAME);

    FuncProtoVoid sendFunc = new FuncProtoVoid(info, SystemIfaceAdder.CONSTRUCT, new ListOfNamed<FuncVariable>());
    debugIface.getPrototype().add(sendFunc);

    FuncProtoVoid recvFunc = new FuncProtoVoid(info, SystemIfaceAdder.DESTRUCT, new ListOfNamed<FuncVariable>());
    debugIface.getPrototype().add(recvFunc);

    classes.add(debugIface);

    SystemIfaceAdder.process(classes, kb);
    PrettyPrinter.print(classes, debugdir + "system.rzy");
  }

  private static void addDebug(Namespace classes, ImplElementary root, String debugdir) {
    ArrayList<String> names = new ArrayList<String>(MsgNamesGetter.get(classes));
    if (names.isEmpty()) {
      return; // this means that there is no input nor output interface
    }

    KnowledgeBase kb = new KnowledgeBase(classes, debugdir);
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    int depth = CompCascadeDepth.get(root);
    depth += 2;
    Collections.sort(names);

    Range symNameSizeType = kbi.getRangeType(names.size());
    ArrayType arrayType = kbi.getArray(depth, symNameSizeType);
    Range sizeType = kbi.getRangeType(depth);

    Interface debugIface;
    FuncProtoVoid recvFunc;
    {
      debugIface = new Interface(info, "_Debug");

      {
        ArrayList<FuncVariable> param = new ArrayList<FuncVariable>();
        FuncVariable sender = new FuncVariable(info, "sender", arrayType);
        param.add(sender);
        FuncVariable size = new FuncVariable(info, "size", sizeType);
        param.add(size);

        FuncProtoVoid sendFunc = new FuncProtoVoid(info, "msgSend", new ListOfNamed<FuncVariable>(param));

        debugIface.getPrototype().add(sendFunc);
      }

      {
        ArrayList<FuncVariable> param = new ArrayList<FuncVariable>();
        FuncVariable sender = new FuncVariable(info, "receiver", arrayType);
        param.add(sender);
        FuncVariable size = new FuncVariable(info, "size", sizeType);
        param.add(size);

        recvFunc = new FuncProtoVoid(info, "msgRecv", new ListOfNamed<FuncVariable>(param));

        debugIface.getPrototype().add(recvFunc);
      }

      classes.add(debugIface);
    }

    DebugIfaceAdder.process(classes, arrayType, sizeType, debugIface, names);

    Interface debugQueryIface;
    {
      debugQueryIface = new Interface(info, "_DebugQuery");

      {
        ArrayList<FuncVariable> param = new ArrayList<FuncVariable>();

        FuncVariable sender = new FuncVariable(info, "nr", symNameSizeType);
        param.add(sender);

        FuncProtoRet sendFunc = new FuncProtoRet(info, "getSym", new ListOfNamed<FuncVariable>(param));
        sendFunc.setRet(new Reference(info, kbi.getStringType()));
        debugQueryIface.getPrototype().add(sendFunc);
      }

      classes.add(debugQueryIface);
    }

    ConstGlobal symtable;
    {
      List<Expression> values = new ArrayList<Expression>();
      for (String itr : names) {
        values.add(new StringValue(info, itr));
      }
      ArrayValue val = new ArrayValue(info, values);

      ArrayType symType = kbi.getArray(names.size(), kbi.getStringType());

      symtable = new ConstGlobal(info, "_debugsym", symType, val);

      classes.add(symtable);
    }

    root.getIface(Direction.in).add(new IfaceUse(info, "_debugQuery", debugQueryIface));

    {
      ArrayList<FuncVariable> param = new ArrayList<FuncVariable>();
      FuncVariable sender = new FuncVariable(info, "nr", symNameSizeType);
      param.add(sender);

      BasicBlockList body = new BasicBlockList(info);
      {
        Reference index = new Reference(info, sender);

        Reference array = new Reference(info, symtable);
        array.getOffset().add(new RefIndex(info, index));

        BasicBlock bb = new BasicBlock(info,"bodyBB");
        bb.setEnd(new ReturnExpr(info, array));
        body.getBasicBlocks().add(bb);
      }

      FuncGlobal func = new FuncGlobal(info, "getSym", new ListOfNamed<FuncVariable>(param));
      func.setRet(new Reference(info, kbi.getStringType()));
      func.setBody(body);
      func.setAttribute(FuncAttr.Public);

      Designator ns = new Designator("_debugQuery");
      root.addFunction(ns.toList(), func);
    }

    // Linker.process(classes, new KnowledgeBase(classes, debugdir));
    // evaluate(classes, debugdir);

    PrettyPrinter.print(classes, debugdir + "debug.rzy");
  }

  private static RizzlyProgram instantiate(Component top, String rootdir, Namespace classes) {
    KnowledgeBase kb = new KnowledgeBase(classes, rootdir);
    ImplElementary env = makeEnv("inst", top, kb);
    classes.add(env);

    {
      PrettyPrinter.print(classes, rootdir + "env.rzy");
      Map<? extends Named, ? extends Named> map = CompInstantiator.process(env, kb);
      PrettyPrinter.print(classes, rootdir + "insta.rzy");

      KnowEvl kf = kb.getEntry(KnowEvl.class);
      Evl inst = kf.get(new Designator("inst"), info);

      Relinker.relink(inst, map);

      LinkReduction.process(inst);
    }

    kb = new KnowledgeBase(classes, rootdir);

    PrettyPrinter.print(classes, rootdir + "instance.rzy");
    {
      Namespace root = classes.forcePath(new Designator("!env", "inst"));
      Set<FunctionBase> pubfunc = makeInputPublic(root, top.getIface(Direction.in));

      // Use only stuff which is referenced from public input functions
      removeUnused(rootdir, classes, pubfunc);
    }

    kb = null;

    PrettyPrinter.print(classes, rootdir + "bflat.rzy");
    ListOfNamed<Named> flat = NamespaceReduction.process(classes);
    PrettyPrinter.print(classes, rootdir + "aflat.rzy");

    RizzlyProgram prg = new RizzlyProgram(rootdir, "inst");
    prg.getFunction().addAll(flat.getItems(FunctionBase.class));
    prg.getVariable().addAll(flat.getItems(StateVariable.class));
    prg.getConstant().addAll(flat.getItems(Constant.class));
    prg.getType().addAll(flat.getItems(Type.class));

    return prg;
  }

  private static ImplElementary makeEnv(String instname, Component top, KnowledgeBase kb) {
    String envname = "!Env";
    ImplElementary env = new ImplElementary(new ElementInfo(envname, -1, -1), "!Env");
    {
      FunctionBase entryFunc = makeEntryExitFunc(State.ENTRY_FUNC_NAME);
      FunctionBase exitFunc = makeEntryExitFunc(State.EXIT_FUNC_NAME);
      env.getInternalFunction().add(entryFunc);
      env.getInternalFunction().add(exitFunc);
      env.setEntryFunc(new Reference(info, entryFunc));
      env.setExitFunc(new Reference(info, exitFunc));
    }

    env.getComponent().add(new CompUse(info, instname, top));

    ListOfNamed<NamedList<FunctionBase>> outprot = addOutIfaceFunc(top.getIface(Direction.out), kb);

    for (NamedList<FunctionBase> list : outprot) {
      ArrayList<String> ns = new ArrayList<String>();
      ns.add(instname);
      ns.add(list.getName());
      for (FunctionBase func : list) {
        env.addFunction(ns, func);
      }
    }

    return env;
  }

  private static FuncPrivateVoid makeEntryExitFunc(String name) {
    FuncPrivateVoid func = new FuncPrivateVoid(info, name, new ListOfNamed<FuncVariable>());
    func.setBody(new BasicBlockList(info));
    return func;
  }

  private static <T extends Evl> void printGraph(String filename, SimpleGraph<T> cg) {
    try {
      @SuppressWarnings("resource")
      HtmlGraphWriter<T> writer = new HtmlGraphWriter<T>(new joGraph.Writer(new PrintStream(filename))) {
        @Override
        protected void wrVertex(T v) {
          wrVertexStart(v);
          wrRow(v.toString());
          wrVertexEnd();
        }
      };
      writer.print(cg);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void removeUnused(String debugdir, Namespace classes, Set<? extends Named> roots) {
    SimpleGraph<Named> g = DepGraph.build(roots);
    printGraph(debugdir + "instused.gv", g);
    removeUnused(classes, g.vertexSet());
  }

  @SuppressWarnings("unused")
  private static List<pir.type.Type> getDirectUsedTypes(pir.type.Type u) {
    pir.traverser.Getter<pir.type.Type, Void> getter = new pir.traverser.Getter<pir.type.Type, Void>() {
      @Override
      protected Void visitRefHead(RefHead obj, Void param) {
        if (obj.getRef() instanceof pir.type.Type) {
          add((pir.type.Type) obj.getRef());
        }
        return null;
      }
    };
    List<pir.type.Type> vs = getter.get(u, null);
    return vs;
  }

  private static ListOfNamed<NamedList<FunctionBase>> addOutIfaceFunc(ListOfNamed<IfaceUse> ifaces, KnowledgeBase kb) {
    ListOfNamed<NamedList<FunctionBase>> ret = new ListOfNamed<NamedList<FunctionBase>>();

    for (IfaceUse ifaceRef : ifaces) {
      NamedList<FunctionBase> list = new NamedList<FunctionBase>(info, ifaceRef.getName());
      Interface iface = ifaceRef.getLink();
      assert (iface != null);
      for (FunctionBase func : iface.getPrototype()) {
        FunctionBase prot = Copy.copy(func);
        prot.setAttribute(FuncAttr.Extern);
        prot.setAttribute(FuncAttr.Public);
        list.add(prot);
      }
      ret.add(list);
    }
    return ret;
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

  private static Set<FunctionBase> makeInputPublic(Namespace root, ListOfNamed<IfaceUse> listOfNamed) {
    Set<FunctionBase> ret = new HashSet<FunctionBase>();

    for (IfaceUse iface : listOfNamed) {
      Namespace ifspace = root.findSpace(iface.getName());
      assert (ifspace != null);
      for (Named item : ifspace) {
        assert (item instanceof FunctionBase);
        FunctionBase func = (FunctionBase) item;
        func.setAttribute(FuncAttr.Public);
        ret.add(func);
      }
    }
    return ret;
  }

}
