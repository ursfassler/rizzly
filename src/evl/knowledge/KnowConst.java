package evl.knowledge;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Number;
import evl.expression.RecordValue;
import evl.expression.StringValue;
import evl.expression.TupleValue;
import evl.expression.TypeCast;
import evl.expression.UnionValue;
import evl.expression.UnsafeUnionValue;
import evl.expression.binop.ArithmeticOp;
import evl.expression.binop.BinaryExp;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;

public class KnowConst extends KnowledgeEntry {
  private final KnowConstTraverser kct = new KnowConstTraverser();

  @Override
  public void init(KnowledgeBase base) {
  }

  public boolean isConst(Evl evl) {
    Boolean ret = kct.traverse(evl, null);
    return ret;
  }

}

class KnowConstTraverser extends NullTraverser<Boolean, Void> {

  @Override
  protected Boolean visitDefault(Evl obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "KnowConst not implemented for " + obj.getClass().getCanonicalName());
    return null;
  }

  @Override
  protected Boolean visitBinaryExp(BinaryExp obj, Void param) {
    return visit(obj.left, param) && visit(obj.right, param);
  }

  @Override
  protected Boolean visitArithmeticOp(ArithmeticOp obj, Void param) {
    return visit(obj.left, param) && visit(obj.right, param);
  }

  @Override
  protected Boolean visitSimpleRef(SimpleRef obj, Void param) {
    // TODO Auto-generated method stub
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Boolean visitTypeCast(TypeCast obj, Void param) {
    return visit(obj.value, param);
  }

  @Override
  protected Boolean visitReference(Reference obj, Void param) {
    RError.err(ErrorType.Warning, obj.getInfo(), "fix me"); // TODO follow reference
    return false;
  }

  @Override
  protected Boolean visitBoolValue(BoolValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitUnsafeUnionValue(UnsafeUnionValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitRecordValue(RecordValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitTupleValue(TupleValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitUnionValue(UnionValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitArrayValue(ArrayValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitNumber(Number obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitStringValue(StringValue obj, Void param) {
    return true;
  }

}
