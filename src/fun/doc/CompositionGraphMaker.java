package fun.doc;

import java.util.Map;

import common.Designator;

import error.ErrorType;
import error.RError;
import fun.composition.ImplComposition;
import fun.doc.compgraph.Interface;
import fun.doc.compgraph.WorldComp;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.knowledge.KnowledgeBase;

public class CompositionGraphMaker {
  public static final String METADATA_KEY = "geometry";

  public static WorldComp make(Designator path, String name, ImplComposition impl, KnowledgeBase kb) {
    throw new RuntimeException("reimplement"); // TODO reimplement
    // KnowFunPath kp = kb.getEntry(KnowFunPath.class);
    // WorldComp comp = new WorldComp(path, name, impl.getInfo().getMetadata(METADATA_KEY));
    //
    // Map<CompUse, SubComponent> compmap = new HashMap<CompUse, SubComponent>();
    // Map<Designator, Interface> ifacemap = new HashMap<Designator, Interface>();
    //
    // for (IfaceUse iface : impl.getIface(Direction.in)) {
    // Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
    // comp.getInput().add(niface);
    // }
    // for (IfaceUse iface : impl.getIface(Direction.out)) {
    // Interface niface = makeIface(new Designator("Self"), comp, iface, ifacemap, kb);
    // comp.getOutput().add(niface);
    // }
    //
    // for (CompUse use : impl.getComponent()) {
    // fun.other.Component comptype = getComp(use.getType());
    // Designator subpath = kp.get(comptype);
    // SubComponent sub = new SubComponent(use.getName(), subpath, comptype.getName(),
    // use.getInfo().getMetadata(METADATA_KEY));
    //
    // for (IfaceUse iface : comptype.getIface(Direction.in)) {
    // Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
    // sub.getInput().add(niface);
    // }
    // for (IfaceUse iface : comptype.getIface(Direction.out)) {
    // Interface niface = makeIface(new Designator("Self", use.getName()), sub, iface, ifacemap, kb);
    // sub.getOutput().add(niface);
    // }
    //
    // compmap.put(use, sub);
    // comp.getComp().add(sub);
    // }
    //
    // for (fun.composition.Connection con : impl.getConnection()) {
    // Interface src = getIface(con.getEndpoint(Direction.in), ifacemap, kb);
    // Interface dst = getIface(con.getEndpoint(Direction.out), ifacemap, kb);
    // fun.doc.compgraph.Connection ncon = new fun.doc.compgraph.Connection(src, dst,
    // con.getInfo().getMetadata(METADATA_KEY));
    // src.getConnection().add(ncon);
    // dst.getConnection().add(ncon);
    // comp.getConn().add(ncon);
    // }
    //
    // return comp;
  }

  private static Interface getIface(Reference ep, Map<Designator, Interface> ifacemap, KnowledgeBase kb) {
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

  // private static Interface makeIface(Designator name, Component sub, IfaceUse iface, Map<Designator, Interface>
  // ifacemap, KnowledgeBase kb) {
  // KnowFunPath kp = kb.getEntry(KnowFunPath.class);
  // fun.other.Interface ifacetype = getIface(iface.getType());
  // Designator path = kp.get(ifacetype);
  // Interface niface = new Interface(sub, iface.getName(), path, ifacetype.getName());
  //
  // name = new Designator(name, iface.getName());
  // assert (!ifacemap.containsKey(name));
  // ifacemap.put(name, niface);
  // return niface;
  // }

  // private static fun.other.Interface getIface(Expression expr) {
  // throw new RuntimeException( "reimplement" ); //TODO reimplement
  // ReferenceLinked reference = (ReferenceLinked) expr;
  // ReferenceLinked rl = reference;
  // return (fun.other.Interface) ((Generator) rl.getLink()).getTemplate();
  // }
  //
  // private static fun.other.Component getComp(Expression expr) {
  // ReferenceLinked reference = (ReferenceLinked) expr;
  // ReferenceLinked rl = reference;
  // return (fun.other.Component) ((Generator) rl.getLink()).getTemplate();
  // }

}
