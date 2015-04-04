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
import java.util.Map;

import pass.EvlPass;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.copy.Relinker;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.knowledge.KnowLeftIsContainerOfRight;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.Namespace;
import evl.type.Type;
import evl.type.base.TupleType;
import evl.variable.FuncVariable;

/**
 * Checks and moves the implementation of the slot and response to the interface
 *
 * @author urs
 *
 */
public class ElementaryReduction extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    Map<Named, Named> map = new HashMap<Named, Named>();

    for (ImplElementary impl : evl.getItems(ImplElementary.class, true)) {

      EvlList<FuncCtrlInDataIn> slotImpl = impl.function.getItems(FuncCtrlInDataIn.class);
      EvlList<FuncCtrlInDataOut> responseImpl = impl.function.getItems(FuncCtrlInDataOut.class);
      EvlList<FuncCtrlInDataIn> slotProto = impl.iface.getItems(FuncCtrlInDataIn.class);
      EvlList<FuncCtrlInDataOut> responseProto = impl.iface.getItems(FuncCtrlInDataOut.class);

      if (!checkForAll(slotImpl, slotProto, "interface declaration") | !checkForAll(responseImpl, responseProto, "interface declaration") | !checkForAll(responseProto, responseImpl, "implementation")) {
        return;
      }

      merge(slotImpl, slotProto, map, kb);
      merge(responseImpl, responseProto, map, kb);

      impl.function.removeAll(slotImpl);
      impl.function.removeAll(responseImpl);
    }

    Relinker.relink(evl, map);
  }

  private boolean checkForAll(EvlList<? extends Function> test, EvlList<? extends Function> set, String what) {
    boolean ret = true;
    for (Function func : test) {
      Function proto = set.find(func.getName());
      if (proto == null) {
        RError.err(ErrorType.Error, func.getInfo(), what + " not found for " + func.getName());
        ret = false;
      }
    }
    return ret;
  }

  private void merge(EvlList<? extends Function> test, EvlList<? extends Function> set, Map<Named, Named> map, KnowledgeBase kb) {
    for (Function func : test) {
      merge(func, set.find(func.getName()), map, kb);
    }
  }

  private void merge(Function func, Function proto, Map<Named, Named> map, KnowledgeBase kb) {
    assert (proto.body.statements.isEmpty());

    KnowLeftIsContainerOfRight kc = kb.getEntry(KnowLeftIsContainerOfRight.class);

    Type ft = getType(func.param);
    Type pt = getType(proto.param);
    if (!kc.get(ft, pt)) {
      RError.err(ErrorType.Hint, proto.getInfo(), "Prototype is here");
      RError.err(ErrorType.Error, func.getInfo(), "Implementation (argument) is not compatible with prototype");
    }

    KnowType kt = kb.getEntry(KnowType.class);
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

  private Type getType(EvlList<FuncVariable> param) {
    EvlList<SimpleRef<Type>> types = new EvlList<SimpleRef<Type>>();
    for (FuncVariable var : param) {
      types.add(var.type);
    }
    TupleType tt = new TupleType(ElementInfo.NO, "", types);
    return tt;
  }
}
