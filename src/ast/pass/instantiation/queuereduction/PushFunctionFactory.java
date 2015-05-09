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

package ast.pass.instantiation.queuereduction;

import java.math.BigInteger;

import ast.Designator;
import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.Named;
import ast.data.expression.RefExp;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Mod;
import ast.data.expression.binop.Plus;
import ast.data.expression.value.NumberValue;
import ast.data.function.Function;
import ast.data.function.header.FuncProcedure;
import ast.data.function.ret.FuncReturnNone;
import ast.data.reference.RefIndex;
import ast.data.reference.RefItem;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.data.statement.VarDefStmt;
import ast.data.type.base.EnumElement;
import ast.data.type.composed.NamedElement;
import ast.data.variable.FuncVariable;

class PushFunctionFactory {
  static public Function create(ElementInfo info, QueueVariables queueVariables, QueueTypes queueTypes, Function func) {
    // Designator path = kp.get(func);
    // assert (path.size() > 0);
    // String name = new Designator(path,
    // func.getName()).toString(Designator.NAME_SEP);
    String name = func.name;

    AstList<FuncVariable> param = Copy.copy(func.param);
    Function impl = new FuncProcedure(info, Designator.NAME_SEP + "push" + Designator.NAME_SEP + name, param, new FuncReturnNone(info), createPushBody(param, queueVariables, queueTypes, queueTypes.getFuncToMsgType().get(func), queueTypes.getFuncToElem().get(func)));
    // impl.properties().put(Property.NAME, Designator.NAME_SEP + "push" +
    // Designator.NAME_SEP + name);
    return impl;
  }

  private static Block createPushBody(AstList<FuncVariable> param, QueueVariables queueVariables, QueueTypes queueTypes, EnumElement enumElement, NamedElement namedElement) {
    ElementInfo info = ElementInfo.NO;

    AstList<IfOption> option = new AstList<IfOption>();

    Block pushbody = new Block(info);

    FuncVariable idx = new FuncVariable(info, "wridx", Copy.copy(queueVariables.getHead().type));
    pushbody.statements.add(new VarDefStmt(info, idx));
    pushbody.statements.add(new AssignmentSingle(info, ref(idx), new Mod(info, new Plus(info, refexpr(queueVariables.getHead()), refexpr(queueVariables.getCount())), new NumberValue(info, BigInteger.valueOf(queueTypes.queueLength())))));

    Reference qir = ref(queueVariables.getQueue());
    qir.offset.add(new RefIndex(info, refexpr(idx)));
    qir.offset.add(new RefName(info, queueTypes.getMessage().tag.name));
    pushbody.statements.add(new AssignmentSingle(info, qir, refexpr(enumElement)));

    for (FuncVariable arg : param) {
      Reference elem = ref(queueVariables.getQueue());
      elem.offset.add(new RefIndex(info, refexpr(idx)));
      elem.offset.add(new RefName(info, namedElement.name));
      elem.offset.add(new RefName(info, arg.name));

      pushbody.statements.add(new AssignmentSingle(info, elem, refexpr(arg)));
    }

    pushbody.statements.add(new AssignmentSingle(info, ref(queueVariables.getCount()), new Plus(info, refexpr(queueVariables.getCount()), new NumberValue(info, BigInteger.ONE))));

    IfOption ifok = new IfOption(info, new Less(info, refexpr(queueVariables.getCount()), new NumberValue(info, BigInteger.valueOf(queueTypes.queueLength()))), pushbody);
    option.add(ifok);

    Block body = new Block(info);
    body.statements.add(new IfStmt(info, option, new Block(info))); // TODO add
    // error
    // code
    return body;
  }

  private static RefExp refexpr(Named idx) {
    return new RefExp(ElementInfo.NO, ref(idx));
  }

  private static Reference ref(Named idx) {
    return new Reference(ElementInfo.NO, idx, new AstList<RefItem>());
  }
}
