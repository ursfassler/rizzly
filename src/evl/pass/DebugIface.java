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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import pass.EvlPass;

import common.Designator;
import common.ElementInfo;

import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.expression.Number;
import evl.data.expression.StringValue;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.FunctionProperty;
import evl.data.function.header.FuncCtrlInDataOut;
import evl.data.function.ret.FuncReturnType;
import evl.data.statement.Block;
import evl.data.statement.CaseOpt;
import evl.data.statement.CaseOptEntry;
import evl.data.statement.CaseOptValue;
import evl.data.statement.CaseStmt;
import evl.data.statement.ReturnExpr;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.special.VoidType;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.pass.debug.CompCascadeDepth;
import evl.pass.debug.DebugIfaceAdder;
import evl.traverser.debug.MsgNamesGetter;

public class DebugIface extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ArrayList<String> names = new ArrayList<String>(MsgNamesGetter.get(evl));
    if (names.isEmpty()) {
      return; // this means that there is no input nor output interface
    }

    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    int depth = (new CompCascadeDepth()).traverse(kb.getRoot(), null);
    depth += 2;
    Collections.sort(names);

    RangeType symNameSizeType = kbi.getRangeType(names.size());
    ArrayType arrayType = kbi.getArray(BigInteger.valueOf(depth), symNameSizeType);
    RangeType sizeType = kbi.getRangeType(depth);
    VoidType voidType = kbi.getVoidType();
    StringType stringType = kbi.getStringType();

    DebugIfaceAdder reduction = new DebugIfaceAdder(arrayType, sizeType, symNameSizeType, voidType, names);
    reduction.traverse(evl, null);

    FuncCtrlInDataOut func = makeNameGetter("DebugName", symNameSizeType, names, stringType);
    func.property = FunctionProperty.Public;
    kb.getRootComp().instref.link.function.add(func);
  }

  private static FuncCtrlInDataOut makeNameGetter(String funcName, RangeType nameSizeType, ArrayList<String> names, StringType stringType) {
    ElementInfo info = ElementInfo.NO;
    FuncVariable arg = new FuncVariable(info, "idx", new SimpleRef<Type>(info, nameSizeType));
    EvlList<FuncVariable> args = new EvlList<FuncVariable>();
    args.add(arg);
    Block body = new Block(info);
    FuncCtrlInDataOut func = new FuncCtrlInDataOut(info, Designator.NAME_SEP + funcName, args, new FuncReturnType(info, new SimpleRef<Type>(info, stringType)), body);

    EvlList<CaseOpt> option = new EvlList<CaseOpt>();
    Block otherwise = new Block(info);
    CaseStmt cs = new CaseStmt(info, new SimpleRef<FuncVariable>(info, arg), option, otherwise);
    body.statements.add(cs);

    for (int i = 0; i < names.size(); i++) {
      EvlList<CaseOptEntry> values = new EvlList<CaseOptEntry>();
      values.add(new CaseOptValue(info, new Number(info, BigInteger.valueOf(i))));
      Block code = new Block(info);
      code.statements.add(new ReturnExpr(info, new StringValue(info, names.get(i))));
      option.add(new CaseOpt(info, values, code));
    }

    otherwise.statements.add(new ReturnExpr(info, new StringValue(info, "")));

    return func;
  }
}
