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
import ast.ElementInfo;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.RefExp;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.LessEqual;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.Relation;
import ast.data.expression.value.NumberValue;
import ast.data.function.Function;
import ast.data.function.header.FuncFunction;
import ast.data.function.ret.FuncReturnType;
import ast.data.reference.RefCall;
import ast.data.reference.RefFactory;
import ast.data.reference.Reference;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.data.statement.ReturnExpr;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.base.RangeType;
import ast.data.variable.FuncVariable;
import ast.dispatcher.DfsTraverser;
import ast.knowledge.KnowLlvmLibrary;
import ast.knowledge.KnowledgeBase;
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
  protected Void visitReference(Reference obj, Void param) {
    if ((obj.link instanceof Type) && !obj.offset.isEmpty() && (obj.offset.get(0) instanceof RefCall)) {
      Type resType = (Type) obj.link;
      Function convertFunc = getConvertFunc(resType);
      obj.link = convertFunc;
    }
    return super.visitReference(obj, param);
  }

  private Function getConvertFunc(Type resType) {
    String name = CONVERT_PREFIX + resType.name;

    Function ret = (Function) ra.find(name);
    if (ret == null) {
      if (resType instanceof RangeType) {
        ret = makeConvertRange((RangeType) resType);
      } else {
        RError.err(ErrorType.Fatal, "Unknown convert target: " + resType.name);
        return null;
      }
      assert (ret != null);
      assert (name.equals(ret.name));
      ra.add(ret);
    }

    assert (ret.param.size() == 1);
    // assert (ret.getRet().getLink() == resType);

    return ret;
  }

  private FuncFunction makeConvertRange(RangeType resType) {
    String name = CONVERT_PREFIX + resType.name;
    ElementInfo info = new ElementInfo(name, 0, 0);

    Block body = new Block(info);
    FuncVariable value = new FuncVariable(info, "value", TypeRefFactory.create(info, kbi.getIntegerType()));

    Block ok = new Block(info);
    Block error = new Block(info);
    AstList<IfOption> option = new AstList<IfOption>();

    { // test
      Relation aboveLower = new LessEqual(info, new NumberValue(info, resType.range.low), new RefExp(info, RefFactory.full(info, value)));
      Relation belowHigher = new LessEqual(info, new RefExp(info, RefFactory.full(info, value)), new NumberValue(info, resType.range.high));
      Expression cond = new LogicAnd(info, aboveLower, belowHigher);
      IfOption opt = new IfOption(info, cond, ok);
      option.add(opt);
    }

    { // ok, cast
      TypeCast cast = new TypeCast(info, TypeRefFactory.create(info, resType), new RefExp(info, RefFactory.full(info, value)));
      ReturnExpr ret = new ReturnExpr(info, cast);
      ok.statements.add(ret);
    }

    { // error, do something
      // TODO how to trap or exception throwing?
      // TODO insert call to debug output with error message
      // TODO throw exception
      Reference call = RefFactory.call(info, kll.getTrap());
      ReturnExpr trap = new ReturnExpr(info, new NumberValue(info, resType.range.low));

      error.statements.add(new CallStmt(info, call));
      error.statements.add(trap);
    }

    IfStmt ifstmt = new IfStmt(info, option, error);
    body.statements.add(ifstmt);

    AstList<FuncVariable> param = new AstList<FuncVariable>();
    param.add(value);
    FuncFunction ret = new FuncFunction(info, name, param, new FuncReturnType(info, TypeRefFactory.create(info, resType)), body);

    return ret;
  }
}
