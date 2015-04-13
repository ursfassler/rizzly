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

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.expression.Number;
import ast.data.expression.TupleValue;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Plus;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.function.header.FuncSlot;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptEntry;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.data.type.base.ArrayType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;

class DispatchFunctionFactory {
  final private KnowType kt;

  public DispatchFunctionFactory(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  public Function create(String prefix, ElementInfo info, QueueVariables queueVariables, QueueTypes queueTypes) {
    Block body = createDispatchBody(queueVariables, queueTypes);
    Function dispatcher = new FuncSlot(info, prefix + "dispatch", new AstList<FuncVariable>(), new FuncReturnNone(info), body);
    dispatcher.property = FunctionProperty.Public;
    return dispatcher;
  }

  private Block createDispatchBody(QueueVariables queueVariables, QueueTypes queueTypes) {
    ElementInfo info = ElementInfo.NO;

    Block body = new Block(info);

    ArrayType dt = (ArrayType) kt.get(queueVariables.getQueue().type);
    UnionType ut = (UnionType) kt.get(dt.type);

    AstList<CaseOpt> opt = new AstList<CaseOpt>();
    Reference ref = new Reference(info, queueVariables.getQueue());
    ref.offset.add(new RefIndex(info, new Reference(info, queueVariables.getHead())));
    ref.offset.add(new RefName(info, ut.tag.name));
    CaseStmt caseStmt = new CaseStmt(info, ref, opt, new Block(info));

    for (Function func : queueTypes.getFuncToMsgType().keySet()) {
      AstList<CaseOptEntry> value = new AstList<CaseOptEntry>();
      value.add(new CaseOptValue(info, new Reference(info, queueTypes.getFuncToMsgType().get(func))));
      CaseOpt copt = new CaseOpt(info, value, new Block(info));

      NamedElement un = queueTypes.getFuncToElem().get(func);
      RecordType rec = queueTypes.getFuncToRecord().get(func);
      TupleValue acarg = new TupleValue(info);
      for (NamedElement elem : rec.element) {
        Reference vref = new Reference(info, queueVariables.getQueue());
        vref.offset.add(new RefIndex(info, new Reference(info, queueVariables.getHead())));
        vref.offset.add(new RefName(info, un.name));
        vref.offset.add(new RefName(info, elem.name));
        acarg.value.add(vref);
      }

      Reference call = new Reference(info, func);
      call.offset.add(new RefCall(info, acarg));
      copt.code.statements.add(new CallStmt(info, call));

      caseStmt.option.add(copt);
    }

    AssignmentSingle add = new AssignmentSingle(info, new Reference(info, queueVariables.getHead()), new Plus(info, new Reference(info, queueVariables.getHead()), new Number(info, BigInteger.ONE)));
    AssignmentSingle sub = new AssignmentSingle(info, new Reference(info, queueVariables.getCount()), new Minus(info, new Reference(info, queueVariables.getCount()), new Number(info, BigInteger.ONE)));

    AstList<IfOption> option = new AstList<IfOption>();
    IfOption ifOption = new IfOption(info, new Greater(info, new Reference(info, queueVariables.getCount()), new Number(info, BigInteger.ZERO)), new Block(info));
    ifOption.code.statements.add(caseStmt);
    ifOption.code.statements.add(sub);
    ifOption.code.statements.add(add);

    option.add(ifOption);
    IfStmt ifc = new IfStmt(info, option, new Block(info));

    body.statements.add(ifc);
    return body;
  }
}
