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

import main.Configuration;
import ast.Designator;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.LessEqual;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.Relation;
import ast.data.expression.value.NumberValue;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.function.ret.FunctionReturnType;
import ast.data.reference.RefCall;
import ast.data.reference.RefFactory;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.base.RangeType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowLlvmLibrary;
import ast.knowledge.KnowledgeBase;
import ast.meta.SourcePosition;
import ast.pass.AstPass;
import ast.repository.manipulator.RepoAdder;
import ast.repository.manipulator.TypeRepo;
import error.ErrorType;
import error.RError;

/**
 * Replaces calls to types with corresponding convert function calls
 *
 * x := R{0,10}( y ); => x := _convert_R( 0, 10, y );
 *
 * with function _convert_R( low, high, value: Integer );
 *
 */
public class IntroduceConvert extends AstPass {
  public IntroduceConvert(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    IntroduceConvertWorker convert = new IntroduceConvertWorker(kb);
    convert.traverse(ast, null);
  }

}

class IntroduceConvertWorker extends DfsTraverser<Void, Void> {
  private final TypeRepo kbi;
  private final RepoAdder ra;
  private final KnowLlvmLibrary kll;
  static final private String CONVERT_PREFIX = Designator.NAME_SEP + "convert" + Designator.NAME_SEP;

  public IntroduceConvertWorker(KnowledgeBase kb) {
    super();
    kbi = new TypeRepo(kb);
    kll = kb.getEntry(KnowLlvmLibrary.class);
    ra = new RepoAdder(kb);
  }

  @Override
  protected Void visitReference(LinkedReferenceWithOffset_Implementation obj, Void param) {
    if ((obj.getLink() instanceof Type) && !obj.getOffset().isEmpty() && (obj.getOffset().get(0) instanceof RefCall)) {
      Type resType = (Type) obj.getLink();
      Function convertFunc = getConvertFunc(resType);
      obj.setLink(convertFunc);
    }
    return super.visitReference(obj, param);
  }

  private Function getConvertFunc(Type resType) {
    String name = CONVERT_PREFIX + resType.getName();

    Function ret = (Function) ra.find(name);
    if (ret == null) {
      if (resType instanceof RangeType) {
        ret = makeConvertRange((RangeType) resType);
      } else {
        RError.err(ErrorType.Fatal, "Unknown convert target: " + resType.getName());
        return null;
      }
      assert (ret != null);
      assert (name.equals(ret.getName()));
      ra.add(ret);
    }

    assert (ret.param.size() == 1);
    // assert (ret.getRet().getLink() == resType);

    return ret;
  }

  private FuncFunction makeConvertRange(RangeType resType) {
    String name = CONVERT_PREFIX + resType.getName();
    SourcePosition info = new SourcePosition(name, 0, 0); // TODO use for everything

    Block body = new Block();
    FunctionVariable value = new FunctionVariable("value", TypeRefFactory.create(kbi.getIntegerType()));

    Block ok = new Block();
    Block error = new Block();
    AstList<IfOption> option = new AstList<IfOption>();

    { // test
      Relation aboveLower = new LessEqual(new NumberValue(resType.range.low), new ReferenceExpression(RefFactory.oldFull(value)));
      Relation belowHigher = new LessEqual(new ReferenceExpression(RefFactory.oldFull(value)), new NumberValue(resType.range.high));
      Expression cond = new LogicAnd(aboveLower, belowHigher);
      IfOption opt = new IfOption(cond, ok);
      option.add(opt);
    }

    { // ok, cast
      TypeCast cast = new TypeCast(TypeRefFactory.create(resType), new ReferenceExpression(RefFactory.oldFull(value)));
      ExpressionReturn ret = new ExpressionReturn(cast);
      ok.statements.add(ret);
    }

    { // error, do something
      // TODO how to trap or exception throwing?
      // TODO insert call to debug output with error message
      // TODO throw exception
      LinkedReferenceWithOffset_Implementation call = RefFactory.oldCall(kll.getTrap());
      ExpressionReturn trap = new ExpressionReturn(new NumberValue(resType.range.low));

      error.statements.add(new CallStmt(call));
      error.statements.add(trap);
    }

    IfStatement ifstmt = new IfStatement(option, error);
    body.statements.add(ifstmt);

    AstList<FunctionVariable> param = new AstList<FunctionVariable>();
    param.add(value);
    FuncFunction ret = new FuncFunction(name, param, new FunctionReturnType(TypeRefFactory.create(resType)), body);

    return ret;
  }
}
