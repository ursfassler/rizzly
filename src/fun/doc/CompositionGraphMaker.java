package fun.doc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.Designator;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.NullTraverser;
import fun.composition.ImplComposition;
import fun.doc.compgraph.Component;
import fun.doc.compgraph.Interface;
import fun.doc.compgraph.WorldComp;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.knowledge.KnowFunPath;
import fun.knowledge.KnowledgeBase;
import fun.other.Namespace;
import fun.type.Type;
import fun.variable.IfaceUse;

public class CompositionGraphMaker extends NullTraverser<Void, Void> {
  private KnowFunPath kp;
  private Set<WorldComp> compGraph = new HashSet<WorldComp>();

  public CompositionGraphMaker(KnowledgeBase kb) {
    kp = kb.getEntry(KnowFunPath.class);
  }

  public static Set<WorldComp> make(Namespace ast, KnowledgeBase kb) {
    CompositionGraphMaker pp = new CompositionGraphMaker(kb);
    pp.traverse(ast, null);
    return pp.compGraph;
  }

  public static WorldComp make(ImplComposition ast, KnowledgeBase kb) {
    CompositionGraphMaker pp = new CompositionGraphMaker(kb);
    pp.traverse(ast, null);
    assert (pp.compGraph.size() == 1);
    return pp.compGraph.iterator().next();
  }

  @Override
  protected Void visitDefault(Fun obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    WorldComp g = makeGraph(obj);
    compGraph.add(g);
    return null;
  }

  private WorldComp makeGraph(ImplComposition impl) {
    //FIXME reimplement
    throw new RuntimeException("Reimplement");
//    Designator path = kp.get(impl);
//    WorldComp comp = new WorldComp(path, impl.getName());
//
//    Map<CompUse, SubComponent> compmap = new HashMap<CompUse, SubComponent>();
//    Map<Designator, Interface> ifacemap = new HashMap<Designator, Interface>();
//
//    for (IfaceUse iface : impl.getIface(Direction.in)) {
//      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap);
//      comp.getInput().add(niface);
//    }
//    for (IfaceUse iface : impl.getIface(Direction.out)) {
//      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap);
//      comp.getOutput().add(niface);
//    }
//
//    for (CompUse use : impl.getComponent()) {
//      fun.other.Component comptype = (fun.other.Component) getType(use.getType());
//      Designator subpath = kp.get(comptype);
//      SubComponent sub = new SubComponent(use.getName(), subpath, comptype.getName());
//
//      for (IfaceUse iface : comptype.getIface(Direction.in)) {
//        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap);
//        sub.getInput().add(niface);
//      }
//      for (IfaceUse iface : comptype.getIface(Direction.out)) {
//        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap);
//        sub.getOutput().add(niface);
//      }
//
//      compmap.put(use, sub);
//      comp.getComp().add(sub);
//    }
//
//    for (fun.composition.Connection con : impl.getConnection()) {
//      Interface src = getIface(con.getEndpoint(Direction.in), ifacemap);
//      Interface dst = getIface(con.getEndpoint(Direction.out), ifacemap);
//      fun.doc.compgraph.Connection ncon = new fun.doc.compgraph.Connection(src, dst);
//      src.getConnection().add(ncon);
//      dst.getConnection().add(ncon);
//      comp.getConn().add(ncon);
//    }
//
//    return comp;
  }

  private Interface getIface(Reference ep, Map<Designator, Interface> ifacemap) {
    ReferenceLinked rl = (ReferenceLinked) ep;
    assert (rl.getOffset().size() <= 1);

    Designator name = new Designator("Self", rl.getLink().getName());
    if (!rl.getOffset().isEmpty()) {
      name = new Designator(name, ((RefName) rl.getOffset().get(0)).getName());
    }

    Interface iface = ifacemap.get(name);
    if (iface == null) {
      RError.err(ErrorType.Error, ep.getInfo(), "Interface not found: " + name);
    }
    return iface;
  }

  private Interface makeIface(Designator name, Component sub, IfaceUse iface, Map<Designator, Interface> ifacemap) {
    //FIXME reimplement
    throw new RuntimeException("Reimplement");
//    fun.other.Interface ifacetype = (fun.other.Interface) getType(iface.getType());
//    Designator path = kp.get(ifacetype);
//    Interface niface = new Interface(sub, iface.getName(), path, ifacetype.getName());
//
//    name = new Designator(name, iface.getName());
//    assert (!ifacemap.containsKey(name));
//    ifacemap.put(name, niface);
//    return niface;
  }

  private Type getType(Reference reference) {
    ReferenceLinked rl = (ReferenceLinked) reference;
    return (Type) rl.getLink();
  }

}
