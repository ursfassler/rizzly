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

package evl.pass.instantiation.queuereduction;

import java.math.BigInteger;

import common.Designator;
import common.ElementInfo;

import evl.copy.Copy;
import evl.data.EvlList;
import evl.data.expression.Number;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Plus;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.function.ret.FuncReturnNone;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Block;
import evl.data.statement.IfOption;
import evl.data.statement.IfStmt;
import evl.data.statement.VarDefStmt;
import evl.data.type.Type;
import evl.data.type.base.EnumElement;
import evl.data.type.composed.NamedElement;
import evl.data.variable.FuncVariable;

class PushFunctionFactory {
  static public Function create(ElementInfo info, QueueVariables queueVariables, QueueTypes queueTypes, Function func) {
    // Designator path = kp.get(func);
    // assert (path.size() > 0);
    // String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);
    String name = func.name;

    EvlList<FuncVariable> param = Copy.copy(func.param);
    Function impl = new FuncPrivateVoid(info, Designator.NAME_SEP + "push" + Designator.NAME_SEP + name, param, new FuncReturnNone(info), createPushBody(param, queueVariables, queueTypes, queueTypes.getFuncToMsgType().get(func), queueTypes.getFuncToElem().get(func)));
    // impl.properties().put(Property.NAME, Designator.NAME_SEP + "push" + Designator.NAME_SEP + name);
    return impl;
  }

  private static Block createPushBody(EvlList<FuncVariable> param, QueueVariables queueVariables, QueueTypes queueTypes, EnumElement enumElement, NamedElement namedElement) {
    ElementInfo info = ElementInfo.NO;

    EvlList<IfOption> option = new EvlList<IfOption>();

    Block pushbody = new Block(info);

    FuncVariable idx = new FuncVariable(info, "wridx", new SimpleRef<Type>(info, queueVariables.getHead().type.link));
    pushbody.statements.add(new VarDefStmt(info, idx));
    pushbody.statements.add(new AssignmentSingle(info, new Reference(info, idx), new Mod(info, new Plus(info, new Reference(info, queueVariables.getHead()), new Reference(info, queueVariables.getCount())), new Number(info, BigInteger.valueOf(queueTypes.queueLength())))));

    Reference qir = new Reference(info, queueVariables.getQueue());
    qir.offset.add(new RefIndex(info, new Reference(info, idx)));
    qir.offset.add(new RefName(info, queueTypes.getMessage().tag.name));
    pushbody.statements.add(new AssignmentSingle(info, qir, new Reference(info, enumElement)));

    for (FuncVariable arg : param) {
      Reference elem = new Reference(info, queueVariables.getQueue());
      elem.offset.add(new RefIndex(info, new Reference(info, idx)));
      elem.offset.add(new RefName(info, namedElement.name));
      elem.offset.add(new RefName(info, arg.name));

      pushbody.statements.add(new AssignmentSingle(info, elem, new Reference(info, arg)));
    }

    pushbody.statements.add(new AssignmentSingle(info, new Reference(info, queueVariables.getCount()), new Plus(info, new Reference(info, queueVariables.getCount()), new Number(info, BigInteger.ONE))));

    IfOption ifok = new IfOption(info, new Less(info, new Reference(info, queueVariables.getCount()), new Number(info, BigInteger.valueOf(queueTypes.queueLength()))), pushbody);
    option.add(ifok);

    Block body = new Block(info);
    body.statements.add(new IfStmt(info, option, new Block(info))); // TODO add error code
    return body;
  }

}
