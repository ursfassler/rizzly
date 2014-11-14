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

package main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import util.GraphHelper;
import util.Pair;
import util.SimpleGraph;
import util.StreamWriter;

import common.Direction;
import common.ElementInfo;
import common.Property;

import debug.DebugPrinter;
import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.composition.CompositionReduction;
import evl.composition.Connection;
import evl.composition.Endpoint;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.expression.reference.BaseRef;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.InterfaceFunction;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
import evl.function.header.FuncPrivateVoid;
import evl.function.header.FuncSubHandlerEvent;
import evl.function.header.FuncSubHandlerQuery;
import evl.hfsm.ImplHfsm;
import evl.hfsm.Transition;
import evl.hfsm.reduction.HfsmReduction;
import evl.hfsm.reduction.HfsmToFsm;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.other.SubCallbacks;
import evl.queue.QueueReduction;
import evl.statement.Block;
import evl.traverser.BitLogicCategorizer;
import evl.traverser.BitnotFixer;
import evl.traverser.CHeaderWriter;
import evl.traverser.CallgraphMaker;
import evl.traverser.ClassGetter;
import evl.traverser.CompInstantiator;
import evl.traverser.CompareReplacer;
import evl.traverser.ConstTyper;
import evl.traverser.ConstantPropagation;
import evl.traverser.DepCollector;
import evl.traverser.DepGraph;
import evl.traverser.EnumReduction;
import evl.traverser.FpcHeaderWriter;
import evl.traverser.IfCutter;
import evl.traverser.InitVarTyper;
import evl.traverser.IntroduceConvert;
import evl.traverser.LinkReduction;
import evl.traverser.NamespaceReduction;
import evl.traverser.OpenReplace;
import evl.traverser.OutsideReaderInfo;
import evl.traverser.OutsideWriterInfo;
import evl.traverser.RangeConverter;
import evl.traverser.ReduceUnion;
import evl.traverser.SystemIfaceAdder;
import evl.traverser.debug.CompCascadeDepth;
import evl.traverser.debug.DebugIfaceAdder;
import evl.traverser.debug.MsgNamesGetter;
import evl.traverser.iocheck.IoCheck;
import evl.traverser.iocheck.StateReaderInfo;
import evl.traverser.iocheck.StateWriterInfo;
import evl.traverser.modelcheck.HfsmTransScopeCheck;
import evl.traverser.modelcheck.ModelChecker;
import evl.traverser.typecheck.TypeChecker;
import evl.traverser.typecheck.specific.CompInterfaceTypeChecker;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.EnumElement;
import evl.type.base.RangeType;
import evl.type.composed.NamedElement;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

//TODO ensure that composition and hfsm use construct and destruct correctly

public class MainEvl {

  private static ElementInfo info = ElementInfo.NO;

  public static RizzlyProgram doEvl(ClaOption opt, String outdir, String debugdir, CompUse rootUse, Namespace aclasses, ArrayList<String> names) {
    KnowledgeBase kb = new KnowledgeBase(aclasses, debugdir);
    DebugPrinter dp = new DebugPrinter(aclasses, debugdir);

    dp.print("afterFun");
    dp.print("evldep", DepGraph.build(aclasses));

    ConstTyper.process(aclasses, kb);
    dp.print("consttype");

    Component root = rootUse.getLink();
    assert (aclasses.getChildren().contains(rootUse));
    aclasses.getChildren().remove(rootUse);

    if (!opt.doLazyModelCheck()) {
      modelCheck(debugdir, aclasses, root, kb);
    }

    // FuncHeaderReplacer.process(aclasses, kb); //TODO remove if not used

    IntroduceConvert.process(aclasses, kb);
    OpenReplace.process(aclasses, kb);

    dp.print("convert");

    root = compositionReduction(aclasses, root, kb);
    dp.print("compreduced");
    root = hfsmReduction(root, opt, aclasses, dp, kb);

    // FIXME: reimplement
    ReduceUnion.process(aclasses, kb);
    InitVarTyper.process(aclasses, kb);

    dp.print("reduced");

    // ExprCutter.process(aclasses, kb); // TODO reimplement
    BitLogicCategorizer.process(aclasses, kb);

    dp.print("normalized");

    typecheck(aclasses, root, debugdir); // TODO reimplement

    SystemIfaceAdder.process(aclasses, kb);
    dp.print("system");

    // ExprCutter.process(aclasses, kb); // TODO reimplement
    dp.print("memcaps");

    if (opt.doDebugEvent()) {
      names.addAll(addDebug(aclasses, root, dp, kb));
    }

    // only for debugging
    // typecheck(classes, debugdir);

    RangeConverter.process(aclasses, kb);
    CompareReplacer.process(aclasses, kb);

    BitnotFixer.process(aclasses, kb);

    kb.clear();

    RizzlyProgram prg = instantiate((ImplElementary) root, aclasses, dp, kb);

    dp.print("prog", prg);
    {
      evl.other.RizzlyProgram head = makeHeader(prg, debugdir);
      Set<String> blacklist = makeBlacklist();
      evl.traverser.Renamer.process(head, blacklist);
      printCHeader(outdir, head, names, kb);
      printFpcHeader(outdir, head, names, kb);
    }

    ConstantPropagation.process(prg);

    // Have to do it here since queue reduction creates enums
    dp.print("bevenum", prg);
    EnumReduction.process(prg);
    dp.print("avenum", prg);
    ConstantPropagation.process(prg);

    removeUnused(prg);
    kb.clear();

    IfCutter.process(prg);

    dp.print("instprog", prg);
    return prg;
  }

  private static Set<String> makeBlacklist() {
    Set<String> blacklist = new HashSet<String>();
    blacklist.add("if");
    blacklist.add("goto");
    blacklist.add("while");
    blacklist.add("do");
    blacklist.add("byte");
    blacklist.add("word");
    blacklist.add("integer");
    blacklist.add("string");
    return blacklist;
  }

  private static void modelCheck(String debugdir, Namespace aclasses, Component root, KnowledgeBase kb) {
    checkRoot(root, debugdir);
    checkUsefullness(aclasses, kb);
    checkForRtcViolation(aclasses, kb);
    ioCheck(aclasses, kb);
    HfsmTransScopeCheck.process(aclasses, kb);

    CompInterfaceTypeChecker.process(aclasses, kb); // check interfaces against
    // implementation
    ModelChecker.process(aclasses, kb);
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
      if (header instanceof Function) {
        inputs.put(header, OutsideReaderInfo.get((Function) header));
        outputs.put(header, OutsideWriterInfo.get((Function) header));
      } else {
        inputs.put(header, false);
        outputs.put(header, false);
      }
    }
    // print(writes, reads, outputs, inputs);

    GraphHelper.doTransitiveClosure(cg);

    writes = doTransStuff(cg, writes);
    reads = doTransStuff(cg, reads);
    outputs = doTransStuff(cg, outputs);
    inputs = doTransStuff(cg, inputs);

    // System.out.println("-------");
    // print(writes, reads, outputs, inputs);

    IoCheck ioCheck = new IoCheck(writes, reads, outputs, inputs);
    ioCheck.check(ClassGetter.get(Function.class, aclasses));
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

  private static void typecheck(Namespace aclasses, Component root, String rootdir) {
    KnowledgeBase kb = new KnowledgeBase(aclasses, rootdir);
    TypeChecker.process(aclasses, kb); // check statements
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
        SimpleGraph<Evl> cg = CallgraphMaker.make(elem, kb);
        assert (elem.getComponent().isEmpty());
        // TODO do we need to check here?
        // TODO check somewhere that slots and responses don't call slot and responses
        // checkRtcViolation(cg, 3, elem.getInfo());
      }
    }
    {
      List<ImplComposition> elemset = ClassGetter.get(ImplComposition.class, aclasses);
      for (ImplComposition elem : elemset) {
        SimpleGraph<CompUse> cg = makeCallgraph(elem.getConnection());
        checkRtcViolation(cg, 2, elem.getInfo());
      }
    }
    // no need to check for hfsm since they can not have sub-components
  }

  private static SimpleGraph<CompUse> makeCallgraph(List<Connection> connection) {
    SimpleGraph<CompUse> ret = new SimpleGraph<CompUse>();
    for (Connection con : connection) {
      Endpoint src = con.getEndpoint(Direction.in);
      Endpoint dst = con.getEndpoint(Direction.out);
      if ((src instanceof EndpointSub) && (dst instanceof EndpointSub)) {
        CompUse srcComp = ((EndpointSub) src).getLink();
        CompUse dstComp = ((EndpointSub) dst).getLink();
        ret.addVertex(srcComp);
        ret.addVertex(dstComp);
        ret.addEdge(srcComp, dstComp);
      }
    }
    return ret;
  }

  private static void checkRtcViolation(SimpleGraph<CompUse> cg, int n, ElementInfo info) {
    GraphHelper.doTransitiveClosure(cg);
    ArrayList<CompUse> vs = new ArrayList<CompUse>(cg.vertexSet());
    Collections.sort(vs);
    EvlList<CompUse> erritems = new EvlList<CompUse>();
    for (CompUse v : cg.vertexSet()) {
      if (cg.containsEdge(v, v)) {
        erritems.add(v);
      }
    }
    if (!erritems.isEmpty()) {
      Collections.sort(erritems);
      for (CompUse v : erritems) {
        RError.err(ErrorType.Hint, v.getInfo(), "Involved component: " + v.getName());
      }
      RError.err(ErrorType.Error, info, "Violation of run to completion detected");
    }
  }

  // TODO do test in FUN part, issues warnings only once for parameterized
  // components.
  /**
   * Checks if there is a input and output data flow. Gives a warning otherwise.
   *
   * A component with only input or output data flow can not do a lot (or not more than a global function can do).
   *
   * @param aclasses
   * @param kb
   */
  private static void checkUsefullness(Namespace aclasses, KnowledgeBase kb) {
    for (Component comp : ClassGetter.get(Component.class, aclasses)) {
      boolean inEmpty = comp.getIface().getItems(FuncCtrlInDataIn.class).isEmpty() && comp.getIface().getItems(FuncCtrlOutDataIn.class).isEmpty();
      boolean outEmpty = comp.getIface().getItems(FuncCtrlOutDataOut.class).isEmpty() && comp.getIface().getItems(FuncCtrlInDataOut.class).isEmpty();
      String name = comp.getName();
      if (inEmpty && outEmpty) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + name + " has no input and no output data flow");
      } else if (outEmpty) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + name + " has no output data flow");
      } else if (inEmpty) {
        RError.err(ErrorType.Warning, comp.getInfo(), "Component " + name + " has no input data flow");
      }
    }
  }

  /**
   * Throws an error if an interface in the top component contains a query. Because we have to be sure that queries are
   * implement correctly.
   *
   * @param root
   * @param rootdir
   */
  private static void checkRoot(Component root, String rootdir) {
    EvlList<FuncCtrlOutDataIn> queries = new EvlList<FuncCtrlOutDataIn>();
    for (InterfaceFunction itr : root.getIface()) {
      if (itr instanceof FuncCtrlOutDataIn) {
        queries.add((FuncCtrlOutDataIn) itr);
      }
    }
    for (FuncCtrlOutDataIn func : queries) {
      RError.err(ErrorType.Hint, func.getInfo(), func.getName());
    }
    if (!queries.isEmpty()) {
      RError.err(ErrorType.Error, root.getInfo(), "Top component is not allowed to have queries in output");
    }
  }

  private static Component compositionReduction(Namespace aclasses, Component root, KnowledgeBase kb) {
    Map<ImplComposition, ImplElementary> map = CompositionReduction.process(aclasses, kb);
    Relinker.relink(aclasses, map);
    if (map.containsKey(root)) {
      root = map.get(root);
    }
    return root;
  }

  private static Component hfsmReduction(Component root, ClaOption opt, Namespace classes, DebugPrinter dp, KnowledgeBase kb) {
    // HfsmGraphviz.print(classes, debugdir + "hfsm.gv");//TODO reimplement
    HfsmToFsm.process(classes, kb);
    dp.print("fsm");

    Map<ImplHfsm, ImplElementary> map = HfsmReduction.process(classes, new KnowledgeBase(classes, opt.getRootPath()));
    Relinker.relink(classes, map);
    // Linker.process(classes, knowledgeBase);

    if (map.containsKey(root)) {
      root = map.get(root);
    }
    return root;
  }

  private static ArrayList<String> addDebug(Namespace classes, Component root, DebugPrinter dp, KnowledgeBase kb) {
    ArrayList<String> names = new ArrayList<String>(MsgNamesGetter.get(classes));
    if (names.isEmpty()) {
      return names; // this means that there is no input nor output interface
    }

    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    int depth = CompCascadeDepth.get(root);
    depth += 2;
    Collections.sort(names);

    RangeType symNameSizeType = kbi.getRangeType(names.size());
    ArrayType arrayType = kbi.getArray(BigInteger.valueOf(depth), symNameSizeType);
    RangeType sizeType = kbi.getRangeType(depth);
    VoidType voidType = kbi.getVoidType();

    DebugIfaceAdder.process(classes, arrayType, sizeType, symNameSizeType, voidType, names);

    dp.print("debug");

    return names;
  }

  private static RizzlyProgram instantiate(ImplElementary top, Namespace classes, DebugPrinter dp, KnowledgeBase kb) {
    ImplElementary env = makeEnv(top, kb);
    classes.getChildren().add(env);

    dp.print("env");
    CompInstantiator.process(env, classes);
    dp.print("insta");

    // FIXME hacky
    Namespace inst = classes.findSpace("!inst");

    LinkReduction.process(inst);
    dp.print("instance");

    // List<Namespace> instes = inst.getChildren().getItems(Namespace.class);
    // assert (instes.size() == 1);
    // inst = instes.get(0);

    Set<Evl> pubfunc = new HashSet<Evl>();
    pubfunc.addAll(inst.getChildren().getItems(FuncCtrlInDataIn.class));
    pubfunc.addAll(inst.getChildren().getItems(FuncCtrlInDataOut.class));
    pubfunc.addAll(inst.getChildren().getItems(FuncCtrlOutDataIn.class));
    pubfunc.addAll(inst.getChildren().getItems(FuncCtrlOutDataOut.class));
    pubfunc.addAll(inst.getChildren().getItems(FuncSubHandlerEvent.class));
    pubfunc.addAll(inst.getChildren().getItems(FuncSubHandlerQuery.class));

    for (Evl nam : pubfunc) {
      // TODO shouldn't be FuncCtrlOutDataOut instead FuncSubHandlerEvent?
      nam.properties().put(Property.Public, true);
    }

    dp.print("bflatAll", DepGraph.build(classes));

    // Use only stuff which is referenced from public input functions
    removeUnused(classes, pubfunc, dp);
    kb.clear();
    QueueReduction.process(inst, kb);

    dp.print("bflat", DepGraph.build(classes));
    dp.print("bflat");
    EvlList<Evl> flat = NamespaceReduction.process(classes, kb);
    dp.print("aflat");

    RizzlyProgram prg = new RizzlyProgram("inst");
    prg.getFunction().addAll(flat.getItems(Function.class));
    prg.getVariable().addAll(flat.getItems(StateVariable.class));
    prg.getConstant().addAll(flat.getItems(Constant.class));
    prg.getType().addAll(flat.getItems(Type.class));

    return prg;
  }

  private static void removeUnused(RizzlyProgram prg) {
    Set<Function> roots = new HashSet<Function>();

    for (Function func : prg.getFunction()) {
      if (func.properties().get(Property.Public) == Boolean.TRUE) {
        roots.add(func);
      }
    }

    SimpleGraph<Evl> g = DepGraph.build(roots);

    Set<Evl> keep = g.vertexSet();

    prg.getConstant().retainAll(keep);
    prg.getVariable().retainAll(keep);
    prg.getFunction().retainAll(keep);
    prg.getType().retainAll(keep);
  }

  private static ImplElementary makeEnv(Component top, KnowledgeBase kb) {
    VoidType vt = kb.getEntry(KnowBaseItem.class).getVoidType();
    FuncPrivateVoid entry = new FuncPrivateVoid(info, "entry", new EvlList<FuncVariable>(), new SimpleRef<Type>(info, vt), new Block(info));
    FuncPrivateVoid exit = new FuncPrivateVoid(info, "exit", new EvlList<FuncVariable>(), new SimpleRef<Type>(info, vt), new Block(info));
    ImplElementary env = new ImplElementary(ElementInfo.NO, "", new SimpleRef<FuncPrivateVoid>(info, entry), new SimpleRef<FuncPrivateVoid>(info, exit));
    env.getFunction().add(entry);
    env.getFunction().add(exit);

    CompUse item = new CompUse(ElementInfo.NO, top, "!inst");
    env.getComponent().add(item);

    for (CompUse compu : env.getComponent()) {
      SubCallbacks suc = new SubCallbacks(compu.getInfo(), new SimpleRef<CompUse>(ElementInfo.NO, compu));
      env.getSubCallback().add(suc);
      for (InterfaceFunction out : compu.getLink().getIface(Direction.out)) {
        Function suha = CompositionReduction.makeHandler((Function) out);
        suha.properties().put(Property.Extern, true);
        suha.properties().put(Property.Public, true);
        suc.getFunc().add(suha);
      }
    }

    return env;
  }

  private static void removeUnused(Namespace classes, Set<? extends Evl> pubfunc, DebugPrinter dp) {
    SimpleGraph<Evl> g = DepGraph.build(pubfunc);
    dp.print("instused", g);
    removeUnused(classes, g.vertexSet());
  }

  private static void removeUnused(Namespace ns, Set<? extends Evl> keep) {
    Set<Evl> remove = new HashSet<Evl>();
    for (Evl itr : ns.getChildren()) {
      if (itr instanceof Namespace) {
        removeUnused((Namespace) itr, keep);
      } else {
        if (!keep.contains(itr)) {
          remove.add(itr);
        }
      }
    }
    ns.getChildren().removeAll(remove);
  }

  private static RizzlyProgram makeHeader(RizzlyProgram prg, String debugdir) {
    RizzlyProgram ret = new RizzlyProgram(prg.getName());
    Set<Evl> anchor = new HashSet<Evl>();
    for (Function func : prg.getFunction()) {
      if (Boolean.TRUE.equals(func.properties().get(Property.Public))) {
        for (FuncVariable arg : func.getParam()) {
          anchor.add(arg.getType().getLink());
        }
        anchor.add(func.getRet().getLink());

        ret.getFunction().add(func);
      }
    }

    Set<Evl> dep = DepCollector.process(anchor);

    for (Evl itr : dep) {
      if (itr instanceof evl.type.Type) {
        ret.getType().add((evl.type.Type) itr);
      } else if (itr instanceof SimpleRef) {
        // element of record type
      } else if (itr instanceof NamedElement) {
        // element of record type
      } else if (itr instanceof EnumElement) {
        // element of enumerator type
      } else {
        RError.err(ErrorType.Fatal, itr.getInfo(), "Object should not be used in header file: " + itr.getClass().getCanonicalName());
      }
    }

    RizzlyProgram cpy = Copy.copy(ret);
    for (Function func : cpy.getFunction()) {
      func.getBody().getStatements().clear();
    }

    toposort(cpy.getType());

    return cpy;
  }

  private static void toposort(List<evl.type.Type> list) {
    SimpleGraph<evl.type.Type> g = new SimpleGraph<evl.type.Type>(list);
    for (evl.type.Type u : list) {
      Set<evl.type.Type> vs = getDirectUsedTypes(u);
      for (evl.type.Type v : vs) {
        g.addEdge(u, v);
      }
    }

    ArrayList<evl.type.Type> old = new ArrayList<evl.type.Type>(list);
    int size = list.size();
    list.clear();
    LinkedList<evl.type.Type> nlist = new LinkedList<evl.type.Type>();
    TopologicalOrderIterator<evl.type.Type, Pair<evl.type.Type, evl.type.Type>> itr = new TopologicalOrderIterator<evl.type.Type, Pair<evl.type.Type, evl.type.Type>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<evl.type.Type> diff = new ArrayList<evl.type.Type>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static Set<evl.type.Type> getDirectUsedTypes(evl.type.Type u) {
    DefTraverser<Void, Set<evl.type.Type>> getter = new DefTraverser<Void, Set<evl.type.Type>>() {

      @Override
      protected Void visitBaseRef(BaseRef obj, Set<Type> param) {
        if (obj.getLink() instanceof Type) {
          param.add((Type) obj.getLink());
        }
        return super.visitBaseRef(obj, param);
      }
    };
    Set<evl.type.Type> vs = new HashSet<evl.type.Type>();
    getter.traverse(u, vs);
    return vs;
  }

  private static void printCHeader(String outdir, RizzlyProgram cprog, List<String> debugNames, KnowledgeBase kb) {
    String cfilename = outdir + cprog.getName() + ".h";
    CHeaderWriter cwriter = new CHeaderWriter(debugNames, kb);
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void printFpcHeader(String outdir, RizzlyProgram cprog, List<String> debugNames, KnowledgeBase kb) {
    String cfilename = outdir + cprog.getName() + ".pas";
    FpcHeaderWriter cwriter = new FpcHeaderWriter(debugNames, kb);
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}
