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

import ast.copy.Copy;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Plus;
import ast.data.expression.value.NumberValue;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.function.header.Slot;
import ast.data.function.ret.FuncReturnNone;
import ast.data.reference.RefFactory;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptEntry;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.type.base.ArrayType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.variable.FunctionVariable;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;

class DispatchFunctionFactory {
  final private KnowType kt;

  public DispatchFunctionFactory(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  public Function create(String prefix, MetaList info, QueueVariables queueVariables, QueueTypes queueTypes) {
    Block body = createDispatchBody(queueVariables, queueTypes);
    Function dispatcher = new Slot(prefix + "dispatch", new AstList<FunctionVariable>(), new FuncReturnNone(info), body);
    dispatcher.metadata().add(info);
    dispatcher.property = FunctionProperty.Public;
    return dispatcher;
  }

  private Block createDispatchBody(QueueVariables queueVariables, QueueTypes queueTypes) {
    Block body = new Block();

    ArrayType dt = (ArrayType) kt.get(queueVariables.getQueue().type);
    UnionType ut = (UnionType) kt.get(dt.type);

    AstList<CaseOpt> opt = new AstList<CaseOpt>();
    RefIndex idx = new RefIndex(new ReferenceExpression(RefFactory.withOffset(queueVariables.getHead())));
    RefName tag = new RefName(ut.tag.getName());
    Reference ref = RefFactory.create(queueVariables.getQueue(), idx, tag);
    CaseStmt caseStmt = new CaseStmt(new ReferenceExpression(ref), opt, new Block());

    for (Function func : queueTypes.getFuncToMsgType().keySet()) {
      AstList<CaseOptEntry> value = new AstList<CaseOptEntry>();
      value.add(new CaseOptValue(new ReferenceExpression(RefFactory.withOffset(queueTypes.getFuncToMsgType().get(func)))));
      CaseOpt copt = new CaseOpt(value, new Block());

      NamedElement un = queueTypes.getFuncToElem().get(func);
      RecordType rec = queueTypes.getFuncToRecord().get(func);
      AstList<Expression> acarg = new AstList<Expression>();
      for (NamedElement elem : rec.element) {
        Reference vref = RefFactory.create(queueVariables.getQueue(), Copy.copy(idx), new RefName(un.getName()), new RefName(elem.getName()));

        acarg.add(new ReferenceExpression(vref));
      }

      Reference call = RefFactory.call(func, acarg);
      copt.code.statements.add(new CallStmt(call));

      caseStmt.option.add(copt);
    }

    AssignmentSingle add = new AssignmentSingle(RefFactory.withOffset(queueVariables.getHead()), new Plus(new ReferenceExpression(RefFactory.withOffset(queueVariables.getHead())), new NumberValue(BigInteger.ONE)));
    AssignmentSingle sub = new AssignmentSingle(RefFactory.withOffset(queueVariables.getCount()), new Minus(new ReferenceExpression(RefFactory.withOffset(queueVariables.getCount())), new NumberValue(BigInteger.ONE)));

    AstList<IfOption> option = new AstList<IfOption>();
    IfOption ifOption = new IfOption(new Greater(new ReferenceExpression(RefFactory.withOffset(queueVariables.getCount())), new NumberValue(BigInteger.ZERO)), new Block());
    ifOption.code.statements.add(caseStmt);
    ifOption.code.statements.add(sub);
    ifOption.code.statements.add(add);

    option.add(ifOption);
    IfStatement ifc = new IfStatement(option, new Block());

    body.statements.add(ifc);
    return body;
  }
}
