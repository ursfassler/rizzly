package pir.passes;

import pir.DefTraverser;
import pir.other.PirValue;
import pir.other.Program;
import pir.statement.normal.StmtSignes;
import pir.statement.normal.binop.Arithmetic;
import pir.statement.normal.binop.Relation;
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
    IntType lt = (IntType) left.getType().getRef();
    IntType rt = (IntType) right.getType().getRef();
    assert (lt == rt);
    return lt instanceof SignedType;
  }

  @Override
  protected Void visitArithmetic(Arithmetic obj, Void param) {
    if (isSigned(obj.getLeft(), obj.getRight())) {
      obj.setSignes(StmtSignes.signed);
    } else {
      obj.setSignes(StmtSignes.unsigned);
    }
    return super.visitArithmetic(obj, param);
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
