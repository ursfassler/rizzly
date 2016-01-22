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

package ast.pass.reduction;

import java.util.HashMap;
import java.util.Map;

import main.Configuration;
import ast.copy.Relinker;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.elementary.ImplElementary;
import ast.data.function.Function;
import ast.data.function.header.Response;
import ast.data.function.header.Slot;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.TypeReference;
import ast.data.type.base.TupleType;
import ast.data.variable.FunctionVariable;
import ast.knowledge.KnowLeftIsContainerOfRight;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.Collector;
import ast.repository.query.NameFilter;
import ast.repository.query.TypeFilter;
import ast.specification.IsClass;
import error.ErrorType;
import error.RError;

/**
 * Checks and moves the implementation of the slot and response to the interface
 *
 * @author urs
 *
 */
public class ElementaryReduction extends AstPass {
  public ElementaryReduction(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    Map<Named, Named> map = new HashMap<Named, Named>();

    for (ImplElementary impl : Collector.select(ast, new IsClass(ImplElementary.class)).castTo(ImplElementary.class)) {

      AstList<Slot> slotImpl = TypeFilter.select(impl.function, Slot.class);
      AstList<Response> responseImpl = TypeFilter.select(impl.function, Response.class);
      AstList<Slot> slotProto = TypeFilter.select(impl.iface, Slot.class);
      AstList<Response> responseProto = TypeFilter.select(impl.iface, Response.class);

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
      Function proto = NameFilter.select(set, func.getName());
      if (proto == null) {
        RError.err(ErrorType.Error, what + " not found for " + func.getName(), func.metadata());
        ret = false;
      }
    }
    return ret;
  }

  private void merge(AstList<? extends Function> test, AstList<? extends Function> set, Map<Named, Named> map, KnowledgeBase kb) {
    for (Function func : test) {
      merge(func, NameFilter.select(set, func.getName()), map, kb);
    }
  }

  private void merge(Function func, Function proto, Map<Named, Named> map, KnowledgeBase kb) {
    assert (proto.body.statements.isEmpty());

    KnowLeftIsContainerOfRight kc = kb.getEntry(KnowLeftIsContainerOfRight.class);
    KnowType kt = kb.getEntry(KnowType.class);

    Type ft = getType(func.param, kt);
    Type pt = getType(proto.param, kt);
    if (!kc.get(ft, pt)) {
      RError.err(ErrorType.Hint, "Prototype is here", proto.metadata());
      RError.err(ErrorType.Error, "Implementation (argument) is not compatible with prototype", func.metadata());
    }

    Type fr = kt.get(func.ret);
    Type pr = kt.get(proto.ret);
    if (!kc.get(pr, fr)) {
      RError.err(ErrorType.Hint, "Prototype is here", proto.ret.metadata());
      RError.err(ErrorType.Error, "Implementation (return) is not compatible with prototype", func.ret.metadata());
    }

    proto.body.statements.addAll(func.body.statements);
    func.body.statements.clear();

    for (int i = 0; i < func.param.size(); i++) {
      map.put(func.param.get(i), proto.param.get(i));
    }
    map.put(func, proto);
  }

  private Type getType(AstList<FunctionVariable> param, KnowType kt) {
    AstList<TypeReference> types = new AstList<TypeReference>();
    for (FunctionVariable var : param) {
      types.add(TypeRefFactory.create(kt.get(var.type)));
    }
    TupleType tt = new TupleType("", types);
    return tt;
  }
}
