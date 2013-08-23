package pir.passes;

import pir.DefTraverser;
import pir.other.PirValue;
import pir.other.Program;
import pir.statement.ArithmeticOp;
import pir.statement.Relation;
import pir.statement.StmtSignes;
import pir.traverser.ExprTypeGetter;
import pir.type.IntType;
import pir.type.SignedType;

/**
 * Set signes of statements (mainly compare)
 * 
 * @author urs
 * 
 */
public class StmtSignSetter extends DefTraverser<Void, Void> {

  public static void process(Program prog) {
    StmtSignSetter changer = new StmtSignSetter();
    changer.traverse(prog, null);
  }

  private boolean isSigned(PirValue left, PirValue right) {
    IntType lt = (IntType) ExprTypeGetter.process(left, ExprTypeGetter.NUMBER_AS_INT);
    IntType rt = (IntType) ExprTypeGetter.process(right, ExprTypeGetter.NUMBER_AS_INT);
    assert (lt == rt);
    return lt instanceof SignedType;
  }

  @Override
  protected Void visitArithmeticOp(ArithmeticOp obj, Void param) {
    if (isSigned(obj.getLeft(), obj.getRight())) {
      obj.setSignes(StmtSignes.signed);
    } else {
      obj.setSignes(StmtSignes.unsigned);
    }
    return super.visitArithmeticOp(obj, param);
  }

  @Override
  protected Void visitRelation(Relation obj, Void param) {
    if (isSigned(obj.getLeft(), obj.getRight())) {
      obj.setSignes(StmtSignes.signed);
    } else {
      obj.setSignes(StmtSignes.unsigned);
    }
    return super.visitRelation(obj, param);
  }

}
