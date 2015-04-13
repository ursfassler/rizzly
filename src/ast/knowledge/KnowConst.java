package ast.knowledge;

import ast.data.Ast;
import ast.data.expression.ArrayValue;
import ast.data.expression.BoolValue;
import ast.data.expression.Number;
import ast.data.expression.RecordValue;
import ast.data.expression.StringValue;
import ast.data.expression.TupleValue;
import ast.data.expression.TypeCast;
import ast.data.expression.UnionValue;
import ast.data.expression.UnsafeUnionValue;
import ast.data.expression.binop.ArithmeticOp;
import ast.data.expression.binop.BinaryExp;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.traverser.NullTraverser;
import error.ErrorType;
import error.RError;

public class KnowConst extends KnowledgeEntry {
  private final KnowConstTraverser kct = new KnowConstTraverser();

  @Override
  public void init(KnowledgeBase base) {
  }

  public boolean isConst(Ast ast) {
    Boolean ret = kct.traverse(ast, null);
    return ret;
  }

}

class KnowConstTraverser extends NullTraverser<Boolean, Void> {

  @Override
  protected Boolean visitDefault(Ast obj, Void param) {
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
    RError.err(ErrorType.Warning, obj.getInfo(), "fix me"); // TODO follow
                                                            // reference
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
