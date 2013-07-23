package evl.traverser;

import java.util.HashMap;
import java.util.Map;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.function.FunctionBase;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;

public class CompInstantiator extends NullTraverser<ImplElementary, Designator> {
  private KnowledgeBase kb;
  private Map<Named, Namespace> ifacemap = new HashMap<Named, Namespace>();

  public CompInstantiator(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static Map<Named, Namespace> process(ImplElementary rizzlyFile, KnowledgeBase kb) {
    CompInstantiator instantiator = new CompInstantiator(kb);
    instantiator.traverse(rizzlyFile, new Designator());
    return instantiator.ifacemap;
  }

  @Override
  protected ImplElementary visitDefault(Evl obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ImplElementary visitImplElementary(ImplElementary obj, Designator param) {
    Namespace ns = kb.getRoot().forceChildPath(param.toList());

    ImplElementary inst = Copy.copy(obj);

    ns.addAll(inst.getConstant());
    ns.addAll(inst.getVariable());
    ns.addAll(inst.getInternalFunction());

    ListOfNamed<NamedList<IfaceUse>> ifaceUsed = new ListOfNamed<NamedList<IfaceUse>>();
    for (CompUse compUse : inst.getComponent()) {
      Component comp = compUse.getLink();
      comp = visit(comp, new Designator(param, compUse.getName()));

      NamedList<IfaceUse> clist = new NamedList<IfaceUse>(new ElementInfo(), compUse.getName());
      ifaceUsed.add(clist);
      clist.addAll(comp.getIface(Direction.out).getList());

      Namespace compSpace = ns.findSpace(compUse.getName());
      assert (compSpace != null);
      assert (!ifacemap.containsKey(compUse));
      ifacemap.put(compUse, compSpace);
    }

    for (NamedList<FunctionBase> iface : inst.getInputFunc()) {
      Namespace space = new Namespace(iface.getInfo(), iface.getName());
      space.addAll(iface);
      ns.subMerge(space);
    }

    for (NamedList<NamedList<FunctionBase>> compItem : inst.getSubComCallback()) {
      Namespace compspace = ns.findSpace(compItem.getName());
      if (compspace == null) {
        RError.err(ErrorType.Fatal, "Namespace should exist since subcomponent was created");
        return null;
      }
      NamedList<IfaceUse> clist = ifaceUsed.find(compItem.getName());
      assert (clist != null);

      for (NamedList<FunctionBase> iface : compItem) {
        Namespace ifaceSpace = new Namespace(iface.getInfo(), iface.getName());
        ifaceSpace.addAll(iface);
        compspace.add(ifaceSpace);
      }
    }

    for (NamedList<IfaceUse> comp : ifaceUsed) {
      Namespace compSpace = ns.findSpace(comp.getName());
      assert (compSpace != null);
      for (IfaceUse iface : comp) {
        Namespace ifaceSpace = compSpace.findSpace(iface.getName());
        assert (ifaceSpace != null);
        assert (!ifacemap.containsKey(iface));
        ifacemap.put(iface, ifaceSpace);
      }
    }

    return inst;
  }

}
