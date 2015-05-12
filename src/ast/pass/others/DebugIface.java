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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import ast.Designator;
import ast.ElementInfo;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.expression.RefExp;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.StringValue;
import ast.data.function.FunctionProperty;
import ast.data.function.header.FuncResponse;
import ast.data.function.ret.FuncReturnType;
import ast.data.reference.RefFactory;
import ast.data.statement.Block;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptEntry;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.ReturnExpr;
import ast.data.type.TypeRefFactory;
import ast.data.type.base.ArrayType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.variable.FuncVariable;
import ast.dispatcher.debug.MsgNamesGetter;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.debug.CompCascadeDepth;
import ast.pass.debug.DebugIfaceAdder;
import ast.repository.manipulator.TypeRepo;

public class DebugIface extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ArrayList<String> names = new ArrayList<String>(MsgNamesGetter.get(ast));
    if (names.isEmpty()) {
      return; // this means that there is no input nor output interface
    }
    TypeRepo kbi = new TypeRepo(kb);

    int depth = (new CompCascadeDepth()).traverse(kb.getRoot(), null);
    depth += 2;
    Collections.sort(names);

    RangeType symNameSizeType = kbi.getRangeType(names.size());
    ArrayType arrayType = kbi.getArray(BigInteger.valueOf(depth), symNameSizeType);
    RangeType sizeType = kbi.getRangeType(depth);
    StringType stringType = kbi.getStringType();

    DebugIfaceAdder reduction = new DebugIfaceAdder(arrayType, sizeType, symNameSizeType, names);
    reduction.traverse(ast, null);

    FuncResponse func = makeNameGetter("DebugName", symNameSizeType, names, stringType);
    func.property = FunctionProperty.Public;
    Component rootComp = kb.getRootComp().compRef.getTarget();
    rootComp.function.add(func);
  }

  private static FuncResponse makeNameGetter(String funcName, RangeType nameSizeType, ArrayList<String> names, StringType stringType) {
    ElementInfo info = ElementInfo.NO;
    FuncVariable arg = new FuncVariable(info, "idx", TypeRefFactory.create(info, nameSizeType));
    AstList<FuncVariable> args = new AstList<FuncVariable>();
    args.add(arg);
    Block body = new Block(info);
    FuncResponse func = new FuncResponse(info, Designator.NAME_SEP + funcName, args, new FuncReturnType(info, TypeRefFactory.create(info, stringType)), body);

    AstList<CaseOpt> option = new AstList<CaseOpt>();
    Block otherwise = new Block(info);
    CaseStmt cs = new CaseStmt(info, new RefExp(info, RefFactory.create(info, arg)), option, otherwise);
    body.statements.add(cs);

    for (int i = 0; i < names.size(); i++) {
      AstList<CaseOptEntry> values = new AstList<CaseOptEntry>();
      values.add(new CaseOptValue(info, new NumberValue(info, BigInteger.valueOf(i))));
      Block code = new Block(info);
      code.statements.add(new ReturnExpr(info, new StringValue(info, names.get(i))));
      option.add(new CaseOpt(info, values, code));
    }

    otherwise.statements.add(new ReturnExpr(info, new StringValue(info, "")));

    return func;
  }
}
