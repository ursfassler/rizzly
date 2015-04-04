package evl.knowledge;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.expression.ArrayValue;
import evl.data.expression.BoolValue;
import evl.data.expression.Number;
import evl.data.expression.RecordValue;
import evl.data.expression.StringValue;
import evl.data.expression.TupleValue;
import evl.data.expression.TypeCast;
import evl.data.expression.UnionValue;
import evl.data.expression.UnsafeUnionValue;
import evl.data.expression.binop.ArithmeticOp;
import evl.data.expression.binop.BinaryExp;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.traverser.NullTraverser;

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
