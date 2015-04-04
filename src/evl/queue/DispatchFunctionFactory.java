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

package evl.queue;

import java.math.BigInteger;

import common.ElementInfo;
import common.Property;

import evl.expression.Number;
import evl.expression.TupleValue;
import evl.expression.binop.Greater;
import evl.expression.binop.Minus;
import evl.expression.binop.Plus;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.Function;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.ret.FuncReturnNone;
import evl.other.EvlList;
import evl.statement.AssignmentSingle;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptEntry;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.type.base.ArrayType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.variable.FuncVariable;

class DispatchFunctionFactory {
  static public Function create(String prefix, ElementInfo info, QueueVariables queueVariables, QueueTypes queueTypes) {
    Block body = createDispatchBody(queueVariables, queueTypes);
    Function dispatcher = new FuncCtrlInDataIn(info, prefix + "dispatch", new EvlList<FuncVariable>(), new FuncReturnNone(info), body);
    dispatcher.properties().put(Property.Public, true);
    return dispatcher;
  }

  static private Block createDispatchBody(QueueVariables queueVariables, QueueTypes queueTypes) {
    ElementInfo info = ElementInfo.NO;

    Block body = new Block(info);

    ArrayType dt = (ArrayType) queueVariables.getQueue().type.link;
    UnionType ut = (UnionType) dt.type.link;

    EvlList<CaseOpt> opt = new EvlList<CaseOpt>();
    Reference ref = new Reference(info, queueVariables.getQueue());
    ref.offset.add(new RefIndex(info, new Reference(info, queueVariables.getHead())));
    ref.offset.add(new RefName(info, ut.tag.getName()));
    CaseStmt caseStmt = new CaseStmt(info, ref, opt, new Block(info));

    for (Function func : queueTypes.getFuncToMsgType().keySet()) {
      EvlList<CaseOptEntry> value = new EvlList<CaseOptEntry>();
      value.add(new CaseOptValue(info, new Reference(info, queueTypes.getFuncToMsgType().get(func))));
      CaseOpt copt = new CaseOpt(info, value, new Block(info));

      NamedElement un = queueTypes.getFuncToElem().get(func);
      RecordType rec = queueTypes.getFuncToRecord().get(func);
      TupleValue acarg = new TupleValue(info);
      for (NamedElement elem : rec.element) {
        Reference vref = new Reference(info, queueVariables.getQueue());
        vref.offset.add(new RefIndex(info, new Reference(info, queueVariables.getHead())));
        vref.offset.add(new RefName(info, un.getName()));
        vref.offset.add(new RefName(info, elem.getName()));
        acarg.value.add(vref);
      }

      Reference call = new Reference(info, func);
      call.offset.add(new RefCall(info, acarg));
      copt.code.statements.add(new CallStmt(info, call));

      caseStmt.option.add(copt);
    }

    AssignmentSingle add = new AssignmentSingle(info, new Reference(info, queueVariables.getHead()), new Plus(info, new Reference(info, queueVariables.getHead()), new Number(info, BigInteger.ONE)));
    AssignmentSingle sub = new AssignmentSingle(info, new Reference(info, queueVariables.getCount()), new Minus(info, new Reference(info, queueVariables.getCount()), new Number(info, BigInteger.ONE)));

    EvlList<IfOption> option = new EvlList<IfOption>();
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
