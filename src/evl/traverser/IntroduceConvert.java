package evl.traverser;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.TypeCast;
import evl.expression.binop.Lessequal;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.Relation;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.FuncWithReturn;
import evl.function.FunctionHeader;
import evl.function.impl.FuncGlobal;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowLlvmLibrary;
import evl.knowledge.KnowledgeBase;
import evl.other.ListOfNamed;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.type.Type;
import evl.type.TypeRef;
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
public class IntroduceConvert extends DefTraverser<Void, Void> {
  private final KnowBaseItem kbi;
  private final KnowLlvmLibrary kll;
  static final private String CONVERT_PREFIX = Designator.NAME_SEP + "convert" + Designator.NAME_SEP;
  private static final ElementInfo info = new ElementInfo();

  public IntroduceConvert(KnowledgeBase kb) {
    super();
    kbi = kb.getEntry(KnowBaseItem.class);
    kll = kb.getEntry(KnowLlvmLibrary.class);
  }

  public static void process(Evl evl, KnowledgeBase kb) {
    IntroduceConvert convert = new IntroduceConvert(kb);
    convert.traverse(evl, null);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if ((obj.getLink() instanceof Type) && !obj.getOffset().isEmpty() && (obj.getOffset().get(0) instanceof RefCall)) {
      Type resType = (Type) obj.getLink();
      FunctionHeader convertFunc = getConvertFunc(resType);
      obj.setLink(convertFunc);
    }
    return super.visitReference(obj, param);
  }

  private FunctionHeader getConvertFunc(Type resType) {
    String name = CONVERT_PREFIX + resType.getName();

    FuncWithReturn ret = (FuncWithReturn) kbi.findItem(name);
    if (ret == null) {
      if (resType instanceof RangeType) {
        ret = makeConvertRange(name, (RangeType) resType);
      } else {
        RError.err(ErrorType.Fatal, "Unknown convert target: " + resType.getName());
        return null;
      }
      assert (ret != null);
      assert (ret.getName().equals(name));
      kbi.addItem(ret);
    }

    assert (ret.getParam().size() == 1);
    assert (ret.getRet().getRef() == resType);

    return ret;
  }

  private FuncGlobal makeConvertRange(String name, RangeType resType) {
    Block body = new Block(info);
    FuncVariable value = new FuncVariable(info, "value", new TypeRef(info, kbi.getIntegerType()));

    Block ok = new Block(info);
    Block error = new Block(info);
    List<IfOption> option = new ArrayList<IfOption>();

    { // test
      Relation aboveLower = new Lessequal(info, new Number(info, resType.getNumbers().getLow()), new Reference(info, value));
      Relation belowHigher = new Lessequal(info, new Reference(info, value), new Number(info, resType.getNumbers().getHigh()));
      Expression cond = new LogicAnd(info, aboveLower, belowHigher);
      IfOption opt = new IfOption(info, cond, ok);
      option.add(opt);
    }

    { // ok, cast
      TypeCast cast = new TypeCast(info, new TypeRef(info, resType), new Reference(info, value));
      ReturnExpr ret = new ReturnExpr(info, cast);
      ok.getStatements().add(ret);
    }

    { // error, do something
      // TODO how to trap or exception throwing?
      // TODO insert call to debug output with error message
      // TODO throw exception
      Reference call = new Reference(info, kll.getTrap());
      call.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
      ReturnExpr trap = new ReturnExpr(info, new Number(info, resType.getNumbers().getLow()));

      error.getStatements().add(new CallStmt(info, call));
      error.getStatements().add(trap);
    }

    IfStmt ifstmt = new IfStmt(info, option, error);
    body.getStatements().add(ifstmt);

    List<FuncVariable> param = new ArrayList<FuncVariable>();
    param.add(value);
    FuncGlobal ret = new FuncGlobal(info, name, new ListOfNamed<FuncVariable>(param));
    ret.setRet(new TypeRef(info, resType));
    ret.setBody(body);

    return ret;
  }
}
