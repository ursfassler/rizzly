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

import java.util.HashSet;
import java.util.Set;

import pass.EvlPass;
import util.SimpleGraph;

import common.Direction;
import common.ElementInfo;
import common.Property;

import debug.DebugPrinter;
import evl.Evl;
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
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.other.SubCallbacks;
import evl.queue.QueueReduction;
import evl.statement.Block;
import evl.traverser.CompInstantiator;
import evl.traverser.DepGraph;
import evl.traverser.LinkReduction;
import evl.traverser.NamespaceReduction;
import evl.type.Type;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

public class Instantiation extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    kb.clear();
    String name = kb.getRootComp().getName();
    instantiate((ImplElementary) kb.getRootComp().getLink(), evl, null, kb);
    evl.setName(name);
  }

  private static void instantiate(ImplElementary top, Namespace classes, DebugPrinter dp, KnowledgeBase kb) {
    ImplElementary env = makeEnv(top, kb);
    classes.getChildren().add(env);

    // dp.print("env");
    CompInstantiator.process(env, classes);
    // dp.print("insta");

    // FIXME hacky
    Namespace inst = classes.findSpace("!inst");

    LinkReduction.process(inst);
    // dp.print("instance");

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

    // dp.print("bflatAll", DepGraph.build(classes));

    // Use only stuff which is referenced from public input functions
    removeUnused(classes, pubfunc, dp);
    kb.clear();
    QueueReduction.process(inst, kb);

    // dp.print("bflat", DepGraph.build(classes));
    // dp.print("bflat");
    EvlList<Evl> flat = NamespaceReduction.process(classes, kb);
    // dp.print("aflat");

    classes.clear();

    classes.addAll(flat.getItems(Function.class));
    classes.addAll(flat.getItems(StateVariable.class));
    classes.addAll(flat.getItems(Constant.class));
    classes.addAll(flat.getItems(Type.class));
  }

  private static ImplElementary makeEnv(Component top, KnowledgeBase kb) {
    VoidType vt = kb.getEntry(KnowBaseItem.class).getVoidType();
    ElementInfo info = ElementInfo.NO;
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
    // dp.print("instused", g);
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

}
