package pir.passes;

import java.util.List;

import pir.other.PirValue;
import pir.other.Program;
import pir.statement.Statement;
import pir.statement.convert.SignExtendValue;
import pir.statement.convert.TruncValue;
import pir.statement.convert.TypeCast;
import pir.statement.convert.ZeroExtendValue;
import pir.traverser.StatementReplacer;
import pir.type.IntType;
import pir.type.SignedType;

/**
 * Replaces typecasts with sext, zext, trunc and assignments
 * 
 * @author urs
 * 
 */
public class TypecastReplacer extends StatementReplacer<Void> {

  public static void process(Program obj) {
    TypecastReplacer changer = new TypecastReplacer();
    changer.traverse(obj, null);
  }

  private IntType getType(PirValue val) {
    return (IntType) val.getType().getRef();
  }

  @Override
  protected List<Statement> visitTypeCast(TypeCast obj, Void param) {
    IntType srcType = getType(obj.getOriginal());
    IntType dstType = (IntType) obj.getVariable().getType().getRef();

    if (dstType.getBits() == srcType.getBits()) {
      return ret(new pir.statement.Assignment(obj.getVariable(), obj.getOriginal()));
    }

    if (dstType.getBits() < srcType.getBits()) {
      return ret(new TruncValue(obj.getVariable(), obj.getOriginal()));
    }

    boolean ss = srcType instanceof SignedType;
    boolean ds = dstType instanceof SignedType;

    if (ss && ds) {
      // value may be < 0
      return ret(new SignExtendValue(obj.getVariable(), obj.getOriginal()));
    } else {
      // value has to be >= 0
      return ret(new ZeroExtendValue(obj.getVariable(), obj.getOriginal()));
    }
  }

}
