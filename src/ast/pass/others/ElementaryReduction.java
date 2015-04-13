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

package ast.pass.others;

import java.util.HashMap;
import java.util.Map;

import ast.ElementInfo;
import ast.copy.Relinker;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.elementary.ImplElementary;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.reference.TypeRef;
import ast.data.function.Function;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSlot;
import ast.data.type.Type;
import ast.data.type.base.TupleType;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowLeftIsContainerOfRight;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.other.ClassGetter;
import error.ErrorType;
import error.RError;

/**
 * Checks and moves the implementation of the slot and response to the interface
 *
 * @author urs
 *
 */
public class ElementaryReduction extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    Map<Named, Named> map = new HashMap<Named, Named>();

    for (ImplElementary impl : ClassGetter.getRecursive(ImplElementary.class, ast)) {

      AstList<FuncSlot> slotImpl = ClassGetter.filter(FuncSlot.class, impl.function);
      AstList<FuncResponse> responseImpl = ClassGetter.filter(FuncResponse.class, impl.function);
      AstList<FuncSlot> slotProto = ClassGetter.filter(FuncSlot.class, impl.iface);
      AstList<FuncResponse> responseProto = ClassGetter.filter(FuncResponse.class, impl.iface);

      if (!checkForAll(slotImpl, slotProto, "interface declaration") | !checkForAll(responseImpl, responseProto, "interface declaration") | !checkForAll(responseProto, responseImpl, "implementation")) {
        return;
      }

      merge(slotImpl, slotProto, map, kb);
      merge(responseImpl, responseProto, map, kb);

      impl.function.removeAll(slotImpl);
      impl.function.removeAll(responseImpl);
    }

    Relinker.relink(ast, map);
  }

  private boolean checkForAll(AstList<? extends Function> test, AstList<? extends Function> set, String what) {
    boolean ret = true;
    for (Function func : test) {
      Function proto = set.find(func.name);
      if (proto == null) {
        RError.err(ErrorType.Error, func.getInfo(), what + " not found for " + func.name);
        ret = false;
      }
    }
    return ret;
  }

  private void merge(AstList<? extends Function> test, AstList<? extends Function> set, Map<Named, Named> map, KnowledgeBase kb) {
    for (Function func : test) {
      merge(func, set.find(func.name), map, kb);
    }
  }

  private void merge(Function func, Function proto, Map<Named, Named> map, KnowledgeBase kb) {
    assert (proto.body.statements.isEmpty());

    KnowLeftIsContainerOfRight kc = kb.getEntry(KnowLeftIsContainerOfRight.class);
    KnowType kt = kb.getEntry(KnowType.class);

    Type ft = getType(func.param, kt);
    Type pt = getType(proto.param, kt);
    if (!kc.get(ft, pt)) {
      RError.err(ErrorType.Hint, proto.getInfo(), "Prototype is here");
      RError.err(ErrorType.Error, func.getInfo(), "Implementation (argument) is not compatible with prototype");
    }

    Type fr = kt.get(func.ret);
    Type pr = kt.get(proto.ret);
    if (!kc.get(pr, fr)) {
      RError.err(ErrorType.Hint, proto.ret.getInfo(), "Prototype is here");
      RError.err(ErrorType.Error, func.ret.getInfo(), "Implementation (return) is not compatible with prototype");
    }

    proto.body.statements.addAll(func.body.statements);
    func.body.statements.clear();

    for (int i = 0; i < func.param.size(); i++) {
      map.put(func.param.get(i), proto.param.get(i));
    }
    map.put(func, proto);
  }

  private Type getType(AstList<FuncVariable> param, KnowType kt) {
    AstList<TypeRef> types = new AstList<TypeRef>();
    for (FuncVariable var : param) {
      types.add(new SimpleRef<Type>(ElementInfo.NO, kt.get(var.type)));
    }
    TupleType tt = new TupleType(ElementInfo.NO, "", types);
    return tt;
  }
}
