package evl.traverser;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.binop.And;
import evl.expression.binop.Lessequal;
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
import evl.statement.bbend.IfGoto;
import evl.statement.bbend.ReturnExpr;
import evl.statement.bbend.Unreachable;
import evl.statement.normal.CallStmt;
import evl.statement.normal.TypeCast;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.RangeType;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import evl.variable.Variable;

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
    if (obj.getLink() instanceof Type) {
      assert (!obj.getOffset().isEmpty());
      assert (obj.getOffset().get(0) instanceof RefCall);
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
    BasicBlockList bbl = new BasicBlockList(info);

    BasicBlock branch = bbl.getEntry();
    BasicBlock ok = bbl.getExit();
    BasicBlock error = new BasicBlock(info, "BB_error");
    bbl.getBasicBlocks().add(error);

    FuncVariable value = new FuncVariable(info, "value", new TypeRef(info, kbi.getIntegerType()));

    { // test
      Relation aboveLower = new Lessequal(info, new Number(info, resType.getNumbers().getLow()), new Reference(info, value));
      Relation belowHigher = new Lessequal(info, new Reference(info, value), new Number(info, resType.getNumbers().getHigh()));
      Expression cond = new And(info, aboveLower, belowHigher);
      branch.setEnd(new IfGoto(info, cond, ok, error));
    }

    { // ok, cast
      SsaVariable retvar = new SsaVariable(info, "ret", new TypeRef(info, resType));
      TypeCast cast = new TypeCast(info, retvar, new TypeRef(info, resType), new Reference(info, value));
      ok.getCode().add(cast);
      ok.setEnd(new ReturnExpr(info, new Reference(info, retvar)));
    }

    { // error, do something
      // TODO insert call to debug output with error message
      // TODO throw exception
      Reference call = new Reference(info, kll.getTrap());
      call.getOffset().add(new RefCall(info, new ArrayList<Expression>()));
      error.getCode().add(new CallStmt(info, call));
      error.setEnd(new Unreachable(info));
    }

    List<Variable> param = new ArrayList<Variable>();
    param.add(value);
    FuncGlobal ret = new FuncGlobal(info, name, new ListOfNamed<Variable>(param));
    ret.setRet(new TypeRef(info, resType));
    ret.setBody(bbl);

    return ret;
  }
}
