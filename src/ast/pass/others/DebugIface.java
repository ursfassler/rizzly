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

import main.Configuration;
import ast.Designator;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.StringValue;
import ast.data.function.FunctionProperty;
import ast.data.function.header.Response;
import ast.data.function.ret.FunctionReturnType;
import ast.data.reference.RefFactory;
import ast.data.statement.Block;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptEntry;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.ExpressionReturn;
import ast.data.type.TypeRefFactory;
import ast.data.type.base.ArrayType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.debug.MsgNamesGetter;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.debug.CompCascadeDepth;
import ast.pass.debug.DebugIfaceAdder;
import ast.repository.manipulator.TypeRepo;

public class DebugIface extends AstPass {
  public DebugIface(Configuration configuration) {
    super(configuration);
  }

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

    Response func = makeNameGetter("DebugName", symNameSizeType, names, stringType);
    func.property = FunctionProperty.Public;
    Component rootComp = (Component) kb.getRootComp().compRef.getTarget();
    rootComp.function.add(func);
  }

  private static Response makeNameGetter(String funcName, RangeType nameSizeType, ArrayList<String> names, StringType stringType) {
    FunctionVariable arg = new FunctionVariable("idx", TypeRefFactory.create(nameSizeType));
    AstList<FunctionVariable> args = new AstList<FunctionVariable>();
    args.add(arg);
    Block body = new Block();
    Response func = new Response(Designator.NAME_SEP + funcName, args, new FunctionReturnType(TypeRefFactory.create(stringType)), body);

    AstList<CaseOpt> option = new AstList<CaseOpt>();
    Block otherwise = new Block();
    CaseStmt cs = new CaseStmt(new ReferenceExpression(RefFactory.create(arg)), option, otherwise);
    body.statements.add(cs);

    for (int i = 0; i < names.size(); i++) {
      AstList<CaseOptEntry> values = new AstList<CaseOptEntry>();
      values.add(new CaseOptValue(new NumberValue(BigInteger.valueOf(i))));
      Block code = new Block();
      code.statements.add(new ExpressionReturn(new StringValue(names.get(i))));
      option.add(new CaseOpt(values, code));
    }

    otherwise.statements.add(new ExpressionReturn(new StringValue("")));

    return func;
  }
}
