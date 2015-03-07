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

package evl.pass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pass.EvlPass;

import common.Direction;
import common.ElementInfo;
import common.Property;

import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.copy.CopyEvl;
import evl.copy.Relinker;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.InterfaceFunction;
import evl.function.header.FuncPrivateVoid;
import evl.function.ret.FuncReturnNone;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.Namespace;
import evl.other.SubCallbacks;
import evl.statement.Block;
import evl.variable.FuncVariable;

public class Instantiation extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    CompUse instComp = kb.getRootComp();
    evl.getChildren().remove(instComp);

    ImplElementary env = makeEnv(instComp.getLink(), kb);
    evl.getChildren().add(env);

    CompInstantiatorWorker instantiator = new CompInstantiatorWorker();
    ImplElementary inst = instantiator.traverse(env, evl);
    Relinker.relink(evl, instantiator.getLinkmap());

    evl.setName(instComp.getName());

    assert (inst.getIface().isEmpty());

    Set<Evl> pubfunc = new HashSet<Evl>();
    pubfunc.addAll(inst.getSubCallback());
    RError.ass(inst.getComponent().size() == 1, inst.getInfo(), "Only expected one instance");
    pubfunc.addAll(inst.getComponent().get(0).getLink().getIface());

    for (Evl nam : pubfunc) {
      nam.properties().put(Property.Public, true);
    }
  }

  private static ImplElementary makeEnv(Component top, KnowledgeBase kb) {
    ElementInfo info = ElementInfo.NO;
    FuncPrivateVoid entry = new FuncPrivateVoid(info, "entry", new EvlList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
    FuncPrivateVoid exit = new FuncPrivateVoid(info, "exit", new EvlList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
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

}

class CompInstantiatorWorker extends NullTraverser<ImplElementary, Namespace> {
  final private Map<Named, Named> linkmap = new HashMap<Named, Named>();

  @Override
  protected ImplElementary visitDefault(Evl obj, Namespace param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ImplElementary visitImplElementary(ImplElementary obj, Namespace ns) {
    CopyEvl copy = new CopyEvl();
    ImplElementary inst = copy.copy(obj);
    Relinker.relink(inst, copy.getCopied());

    ns.addAll(inst.getIface());
    ns.addAll(inst.getType());
    ns.addAll(inst.getConstant());
    ns.addAll(inst.getVariable());
    ns.addAll(inst.getFunction());
    ns.add(inst.getQueue());
    ns.add(inst.getEntryFunc());
    ns.add(inst.getExitFunc());

    // ns.getChildren().removeAll(ns.getChildren().getItems(FuncCtrlOutDataIn.class));
    // ns.getChildren().removeAll(ns.getChildren().getItems(FuncCtrlOutDataOut.class));

    for (CompUse compUse : inst.getComponent()) {
      Component comp = compUse.getLink();

      // copy / instantiate used component
      Namespace usens = new Namespace(compUse.getInfo(), compUse.getName());
      ImplElementary cpy = visit(comp, usens);
      compUse.setLink(cpy);
      ns.add(usens);
      linkmap.put(compUse, usens);

      // route output interface to sub-callback implementation
      for (Function impl : getSubCallback(inst.getSubCallback(), compUse).getFunc()) {
        // get output declaration of instantiated sub-component
        Function outdecl = (Function) cpy.getIface().find(impl.getName());

        assert (outdecl != null);
        assert (usens.getChildren().contains(outdecl));
        assert (!linkmap.containsKey(outdecl));

        // change links to output declaration to the sub-callback of this component
        linkmap.put(outdecl, impl);
        usens.getChildren().remove(outdecl);
        usens.add(impl);
      }
    }

    return inst;
  }

  private SubCallbacks getSubCallback(EvlList<SubCallbacks> subCallback, CompUse compUse) {
    for (SubCallbacks suc : subCallback) {
      if (suc.getCompUse().getLink() == compUse) {
        return suc;
      }
    }
    assert (false);
    return null;
  }

  public Map<Named, Named> getLinkmap() {
    return linkmap;
  }

}