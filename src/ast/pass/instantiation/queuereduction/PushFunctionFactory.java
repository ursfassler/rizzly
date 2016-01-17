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
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.Named;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Modulo;
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
import ast.data.statement.IfStatement;
import ast.data.statement.VarDefStmt;
import ast.data.type.base.EnumElement;
import ast.data.type.composed.NamedElement;
import ast.data.variable.FunctionVariable;
import ast.meta.MetaList;

class PushFunctionFactory {
  static public Function create(MetaList info, QueueVariables queueVariables, QueueTypes queueTypes, Function func) {
    // Designator path = kp.get(func);
    // assert (path.size() > 0);
    // String name = new Designator(path,
    // func.getName()).toString(Designator.NAME_SEP);
    String name = func.getName();

    AstList<FunctionVariable> param = Copy.copy(func.param);
    Function impl = new FuncProcedure(Designator.NAME_SEP + "push" + Designator.NAME_SEP + name, param, new FuncReturnNone(info), createPushBody(param, queueVariables, queueTypes, queueTypes.getFuncToMsgType().get(func), queueTypes.getFuncToElem().get(func)));
    impl.metadata().add(info);
    // impl.properties().put(Property.NAME, Designator.NAME_SEP + "push" +
    // Designator.NAME_SEP + name);
    return impl;
  }

  private static Block createPushBody(AstList<FunctionVariable> param, QueueVariables queueVariables, QueueTypes queueTypes, EnumElement enumElement, NamedElement namedElement) {
    AstList<IfOption> option = new AstList<IfOption>();

    Block pushbody = new Block();

    FunctionVariable idx = new FunctionVariable("wridx", Copy.copy(queueVariables.getHead().type));
    pushbody.statements.add(new VarDefStmt(idx));
    pushbody.statements.add(new AssignmentSingle(ref(idx), new Modulo(new Plus(refexpr(queueVariables.getHead()), refexpr(queueVariables.getCount())), new NumberValue(BigInteger.valueOf(queueTypes.queueLength())))));

    Reference qir = ref(queueVariables.getQueue());
    qir.offset.add(new RefIndex(refexpr(idx)));
    qir.offset.add(new RefName(queueTypes.getMessage().tag.getName()));
    pushbody.statements.add(new AssignmentSingle(qir, refexpr(enumElement)));

    for (FunctionVariable arg : param) {
      Reference elem = ref(queueVariables.getQueue());
      elem.offset.add(new RefIndex(refexpr(idx)));
      elem.offset.add(new RefName(namedElement.getName()));
      elem.offset.add(new RefName(arg.getName()));

      pushbody.statements.add(new AssignmentSingle(elem, refexpr(arg)));
    }

    pushbody.statements.add(new AssignmentSingle(ref(queueVariables.getCount()), new Plus(refexpr(queueVariables.getCount()), new NumberValue(BigInteger.ONE))));

    IfOption ifok = new IfOption(new Less(refexpr(queueVariables.getCount()), new NumberValue(BigInteger.valueOf(queueTypes.queueLength()))), pushbody);
    option.add(ifok);

    Block body = new Block();
    body.statements.add(new IfStatement(option, new Block())); // TODO add
    // error
    // code
    return body;
  }

  private static ReferenceExpression refexpr(Named idx) {
    return new ReferenceExpression(ref(idx));
  }

  private static Reference ref(Named idx) {
    return new Reference(idx, new AstList<RefItem>());
  }
}
