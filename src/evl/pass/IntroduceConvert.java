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

import pass.EvlPass;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.TypeCast;
import evl.expression.binop.Lessequal;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.Relation;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.header.FuncGlobal;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowLlvmLibrary;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.type.Type;
import evl.type.base.RangeType;
import evl.variable.FuncVariable;

/**
 * Replaces calls to types with corresponding convert function calls
 *
 * x := R{0,10}( y ); => x := _convert_R( 0, 10, y );
 *
 * with function _convert_R( low, high, value: Integer );
 *
 */
public class IntroduceConvert extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    IntroduceConvertWorker convert = new IntroduceConvertWorker(kb);
    convert.traverse(evl, null);
  }

}

class IntroduceConvertWorker extends DefTraverser<Void, Void> {
  private final KnowBaseItem kbi;
  private final KnowLlvmLibrary kll;
  static final private String CONVERT_PREFIX = Designator.NAME_SEP + "convert" + Designator.NAME_SEP;
  private static final ElementInfo info = ElementInfo.NO;

  public IntroduceConvertWorker(KnowledgeBase kb) {
    super();
    kbi = kb.getEntry(KnowBaseItem.class);
    kll = kb.getEntry(KnowLlvmLibrary.class);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if ((obj.getLink() instanceof Type) && !obj.getOffset().isEmpty() && (obj.getOffset().get(0) instanceof RefCall)) {
      Type resType = (Type) obj.getLink();
      Function convertFunc = getConvertFunc(resType);
      obj.setLink(convertFunc);
    }
    return super.visitReference(obj, param);
  }

  private Function getConvertFunc(Type resType) {
    String name = CONVERT_PREFIX + resType.getName();

    Function ret = (Function) kbi.findItem(name);
    if (ret == null) {
      if (resType instanceof RangeType) {
        ret = makeConvertRange((RangeType) resType);
      } else {
        RError.err(ErrorType.Fatal, "Unknown convert target: " + resType.getName());
        return null;
      }
      assert (ret != null);
      assert (name.equals(ret.getName()));
      kbi.addItem(ret);
    }

    assert (ret.getParam().size() == 1);
    assert (ret.getRet().getLink() == resType);

    return ret;
  }

  private FuncGlobal makeConvertRange(RangeType resType) {
    Block body = new Block(info);
    FuncVariable value = new FuncVariable(info, "value", new SimpleRef<Type>(info, kbi.getIntegerType()));

    Block ok = new Block(info);
    Block error = new Block(info);
    EvlList<IfOption> option = new EvlList<IfOption>();

    { // test
      Relation aboveLower = new Lessequal(info, new Number(info, resType.getNumbers().getLow()), new Reference(info, value));
      Relation belowHigher = new Lessequal(info, new Reference(info, value), new Number(info, resType.getNumbers().getHigh()));
      Expression cond = new LogicAnd(info, aboveLower, belowHigher);
      IfOption opt = new IfOption(info, cond, ok);
      option.add(opt);
    }

    { // ok, cast
      TypeCast cast = new TypeCast(info, new SimpleRef<Type>(info, resType), new Reference(info, value));
      ReturnExpr ret = new ReturnExpr(info, cast);
      ok.getStatements().add(ret);
    }

    { // error, do something
      // TODO how to trap or exception throwing?
      // TODO insert call to debug output with error message
      // TODO throw exception
      Reference call = new Reference(info, kll.getTrap());
      call.getOffset().add(new RefCall(info, new EvlList<Expression>()));
      ReturnExpr trap = new ReturnExpr(info, new Number(info, resType.getNumbers().getLow()));

      error.getStatements().add(new CallStmt(info, call));
      error.getStatements().add(trap);
    }

    IfStmt ifstmt = new IfStmt(info, option, error);
    body.getStatements().add(ifstmt);

    EvlList<FuncVariable> param = new EvlList<FuncVariable>();
    param.add(value);
    FuncGlobal ret = new FuncGlobal(info, CONVERT_PREFIX + resType.getName(), param, new SimpleRef<Type>(info, resType), body);

    return ret;
  }
}
