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

package ast.pass.instantiation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pass.AstPass;
import ast.copy.CopyAst;
import ast.copy.Relinker;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.SubCallbacks;
import ast.data.component.elementary.ImplElementary;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Block;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowledgeBase;
import ast.pass.CompositionReduction;
import ast.traverser.NullTraverser;
import ast.traverser.other.ClassGetter;

import common.Direction;
import common.ElementInfo;

import error.RError;

public class ElementaryInstantiation extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    CompUse instComp = kb.getRootComp();
    ast.children.remove(instComp);

    ImplElementary env = makeEnv((Component) instComp.compRef.getTarget(), kb);
    ast.children.add(env);

    CompInstantiatorWorker instantiator = new CompInstantiatorWorker();
    ImplElementary inst = instantiator.traverse(env, ast);
    Relinker.relink(ast, instantiator.getLinkmap());

    ast.name = instComp.name;

    assert (inst.iface.isEmpty());

    Set<Function> pubfunc = new HashSet<Function>();
    pubfunc.addAll(ClassGetter.getRecursive(Function.class, inst.subCallback));
    RError.ass(inst.component.size() == 1, inst.getInfo(), "Only expected one instance");
    Component targetComp = (Component) inst.component.get(0).compRef.getTarget();
    pubfunc.addAll(targetComp.iface);

    for (Function nam : pubfunc) {
      if (nam.property == FunctionProperty.Private) {
        nam.property = FunctionProperty.Public;
      }
    }
  }

  private static ImplElementary makeEnv(Component top, KnowledgeBase kb) {
    ElementInfo info = ElementInfo.NO;
    FuncProcedure entry = new FuncProcedure(info, "entry", new AstList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
    FuncProcedure exit = new FuncProcedure(info, "exit", new AstList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
    ImplElementary env = new ImplElementary(ElementInfo.NO, "", new SimpleRef<FuncProcedure>(info, entry), new SimpleRef<FuncProcedure>(info, exit));
    env.function.add(entry);
    env.function.add(exit);

    CompUse item = new CompUse(ElementInfo.NO, "!inst", new SimpleRef<Component>(ElementInfo.NO, top));
    env.component.add(item);

    for (CompUse compu : env.component) {
      SubCallbacks suc = new SubCallbacks(compu.getInfo(), new SimpleRef<CompUse>(ElementInfo.NO, compu));
      env.subCallback.add(suc);
      Component refComp = (Component) compu.compRef.getTarget();
      for (InterfaceFunction out : refComp.getIface(Direction.out)) {
        Function suha = CompositionReduction.makeHandler(out);
        suha.property = FunctionProperty.External;
        suc.func.add(suha);
      }
    }

    return env;
  }

}

class CompInstantiatorWorker extends NullTraverser<ImplElementary, Namespace> {
  final private Map<Named, Named> linkmap = new HashMap<Named, Named>();

  @Override
  protected ImplElementary visitDefault(Ast obj, Namespace param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected ImplElementary visitImplElementary(ImplElementary obj, Namespace ns) {
    CopyAst copy = new CopyAst();
    ImplElementary inst = copy.copy(obj);
    Relinker.relink(inst, copy.getCopied());

    ns.children.addAll(inst.iface);
    ns.children.addAll(inst.type);
    ns.children.addAll(inst.constant);
    ns.children.addAll(inst.variable);
    ns.children.addAll(inst.function);
    ns.children.add(inst.queue);
    ns.children.add(inst.entryFunc);
    ns.children.add(inst.exitFunc);

    // ns.getChildren().removeAll(ns.getChildren().getItems(FuncCtrlOutDataIn.class));
    // ns.getChildren().removeAll(ns.getChildren().getItems(FuncCtrlOutDataOut.class));

    for (CompUse compUse : inst.component) {
      Component comp = (Component) compUse.compRef.getTarget();

      // copy / instantiate used component
      Namespace usens = new Namespace(compUse.getInfo(), compUse.name);
      ImplElementary cpy = visit(comp, usens);
      compUse.compRef = new SimpleRef<Component>(compUse.getInfo(), cpy);
      ns.children.add(usens);
      linkmap.put(compUse, usens);

      // route output interface to sub-callback implementation
      for (Function impl : getSubCallback(inst.subCallback, compUse).func) {
        // get output declaration of instantiated sub-component
        Function outdecl = cpy.iface.find(impl.name);

        assert (outdecl != null);
        assert (usens.children.contains(outdecl));
        assert (!linkmap.containsKey(outdecl));

        // change links to output declaration to the sub-callback of this
        // component
        linkmap.put(outdecl, impl);
        usens.children.remove(outdecl);
        usens.children.add(impl);
      }
    }

    return inst;
  }

  private SubCallbacks getSubCallback(AstList<SubCallbacks> subCallback, CompUse compUse) {
    for (SubCallbacks suc : subCallback) {
      if (suc.compUse.link == compUse) {
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
