package evl.traverser;

import java.util.HashMap;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.function.FunctionHeader;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;

public class CompInstantiator extends NullTraverser<ImplElementary, Designator> {
  private KnowledgeBase kb;
  private Map<Named, Named> ifacemap = new HashMap<Named, Named>();

  public CompInstantiator(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static Map<Named, Named> process(ImplElementary rizzlyFile, KnowledgeBase kb) {
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

    // XXX instantiated component should not have any function in Input nor Output

    // FIXME This code is probably full of errors
    ListOfNamed<NamedList<FunctionHeader>> ifaceUsed = new ListOfNamed<NamedList<FunctionHeader>>();
    for (CompUse compUse : inst.getComponent()) {
      Component comp = compUse.getLink();
      comp = visit(comp, new Designator(param, compUse.getName()));

      NamedList<FunctionHeader> clist = new NamedList<FunctionHeader>(new ElementInfo(), compUse.getName());
      ifaceUsed.add(clist);
      clist.addAll(comp.getOutput().getList());

      Namespace compSpace = ns.findSpace(compUse.getName());
      assert (compSpace != null);
      assert (!ifacemap.containsKey(compUse));
      ifacemap.put(compUse, compSpace);
    }

    for (NamedList<FunctionHeader> compItem : inst.getSubComCallback()) {
      Namespace compspace = ns.findSpace(compItem.getName());
      if (compspace == null) {
        RError.err(ErrorType.Fatal, "Namespace should exist since subcomponent was created");
        return null;
      }
      NamedList<FunctionHeader> clist = ifaceUsed.find(compItem.getName());
      assert (clist != null);

      for (FunctionHeader iface : compItem) {
        compspace.add(iface);
      }
    }

    for (NamedList<FunctionHeader> comp : ifaceUsed) {
      Namespace compSpace = ns.findSpace(comp.getName());
      assert (compSpace != null);
      for (FunctionHeader func : comp) {
        Named ifaceSpace = compSpace.findItem(func.getName());
        assert (ifaceSpace != null);
        assert (ifaceSpace instanceof FunctionHeader);
        assert (!ifacemap.containsKey(func));
        ifacemap.put(func, ifaceSpace);
      }
    }

    return inst;
  }

}
