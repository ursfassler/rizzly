package evl.doc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.Designator;
import common.Direction;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.Connection;
import evl.composition.Endpoint;
import evl.composition.ImplComposition;
import evl.doc.compgraph.Component;
import evl.doc.compgraph.Interface;
import evl.doc.compgraph.SubComponent;
import evl.doc.compgraph.WorldComp;
import evl.knowledge.KnowPath;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.IfaceUse;
import evl.other.Namespace;

public class CompositionGraphMaker extends NullTraverser<Void, Void> {
  private KnowPath kp;
  private Set<WorldComp> compGraph = new HashSet<WorldComp>();

  public CompositionGraphMaker(KnowledgeBase kb) {
    kp = kb.getEntry(KnowPath.class);
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
  protected Void visitDefault(Evl obj, Void param) {
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
    Designator path = kp.get(impl);
    WorldComp comp = new WorldComp(path, impl.getName());

    Map<CompUse, SubComponent> compmap = new HashMap<CompUse, SubComponent>();
    Map<Designator, Interface> ifacemap = new HashMap<Designator, Interface>();

    for (IfaceUse iface : impl.getIface(Direction.in)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap);
      comp.getInput().add(niface);
    }
    for (IfaceUse iface : impl.getIface(Direction.out)) {
      Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap);
      comp.getOutput().add(niface);
    }

    for (CompUse use : impl.getComponent()) {
      evl.other.Component comptype = use.getLink();
      Designator subpath = kp.get(comptype);
      SubComponent sub = new SubComponent(use.getName(), subpath, comptype.getName());

      for (IfaceUse iface : comptype.getIface(Direction.in)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap);
        sub.getInput().add(niface);
      }
      for (IfaceUse iface : comptype.getIface(Direction.out)) {
        Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap);
        sub.getOutput().add(niface);
      }

      compmap.put(use, sub);
      comp.getComp().add(sub);
    }

    for (Connection con : impl.getConnection()) {
      Interface src = getIface(con.getEndpoint(Direction.in), ifacemap);
      Interface dst = getIface(con.getEndpoint(Direction.out), ifacemap);
      evl.doc.compgraph.Connection ncon = new evl.doc.compgraph.Connection(src, dst);
      src.getConnection().add(ncon);
      dst.getConnection().add(ncon);
      comp.getConn().add(ncon);
    }

    return comp;
  }

  private Interface getIface(Endpoint ep, Map<Designator, Interface> ifacemap) {
    Designator name = new Designator("Self", ep.getDes());

    Interface iface = ifacemap.get(name);
    if (iface == null) {
      RError.err(ErrorType.Error, ep.getInfo(), "Interface not found: " + name);
    }
    return iface;
  }

  private Interface makeIface(Designator name, Component sub, IfaceUse iface, Map<Designator, Interface> ifacemap) {
    evl.other.Interface ifacetype = iface.getLink();
    Designator path = kp.get(ifacetype);
    Interface niface = new Interface(sub, iface.getName(), path, ifacetype.getName());

    name = new Designator(name, iface.getName());
    assert (!ifacemap.containsKey(name));
    ifacemap.put(name, niface);
    return niface;
  }

}
