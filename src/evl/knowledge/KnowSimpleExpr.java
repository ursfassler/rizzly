package evl.knowledge;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.StringValue;
import evl.expression.binop.BinaryExp;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.RefPtrDeref;
import evl.expression.reference.Reference;
import evl.expression.unop.UnaryExp;
import evl.function.FunctionBase;
import evl.other.CompUse;
import evl.type.TypeRef;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.composed.RecordType;
import evl.type.special.NaturalType;
import evl.type.special.PointerType;
import evl.variable.ConstGlobal;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

/**
 * 
 * @author urs
 */
public class KnowSimpleExpr {

  static final SimpleGetter getter = new SimpleGetter();

  public static boolean isSimple(Expression expr) {
    return getter.traverse(expr, null);
  }
}

class SimpleGetter extends NullTraverser<Boolean, Void> {

  @Override
  protected Boolean visitDefault(Evl obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitItr(Iterable<? extends Evl> list, Void param) {
    for (Evl ast : list) {
      if (!visit(ast, param)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected Boolean visitReference(Reference obj, Void param) {
    boolean ret = visit(obj.getLink(), param) & visitItr(obj.getOffset(), param);
    return ret;
  }

  @Override
  protected Boolean visitRefCall(RefCall obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitRefIndex(RefIndex obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitRefName(RefName obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitRefPtrDeref(RefPtrDeref obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitTypeRef(TypeRef obj, Void param) {
    return visit(obj.getRef(), param);
  }

  @Override
  protected Boolean visitNaturalType(NaturalType obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  protected Boolean visitPointerType(PointerType obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  protected Boolean visitRangeType(RangeType obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitEnumType(EnumType obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitEnumElement(EnumElement obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitRecordType(RecordType obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitCompUse(CompUse obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitConstGlobal(ConstGlobal obj, Void param) {
    return visit(obj.getType(), param); // TODO ok?
  }

  @Override
  protected Boolean visitFuncVariable(FuncVariable obj, Void param) {
    return true;
    // return visit(obj.getType(),param);
  }

  @Override
  protected Boolean visitStateVariable(StateVariable obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitBinaryExp(BinaryExp obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitBoolValue(BoolValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitNumber(Number obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitStringValue(StringValue obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet");
  }

  @Override
  protected Boolean visitUnaryExp(UnaryExp obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitFunctionBase(FunctionBase obj, Void param) {
    return false;
  }

}
