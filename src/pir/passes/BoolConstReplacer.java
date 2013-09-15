package pir.passes;

import java.math.BigInteger;

import pir.expression.BoolValue;
import pir.know.KnowBaseItem;
import pir.know.KnowledgeBase;
import pir.other.PirValue;
import pir.other.Program;
import pir.traverser.PirValueReplacer;
import pir.type.NoSignType;
import pir.type.TypeRef;

/**
 * Replaces boolean constants with i1 constants
 * 
 * @author urs
 * 
 */
public class BoolConstReplacer extends PirValueReplacer<Void, Void> {

  final private NoSignType btype;

  public BoolConstReplacer(NoSignType btype) {
    this.btype = btype;
  }

  public static void process(Program obj, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);
    NoSignType i1type = kbi.getNoSignType(1);

    BoolConstReplacer replacer = new BoolConstReplacer(i1type);
    replacer.traverse(obj, null);
  }

  @Override
  protected PirValue replace(PirValue val, Void param) {
    if( val instanceof BoolValue ) {
      boolean value = ( (BoolValue) val ).isValue();
      BigInteger ival = value ? BigInteger.ONE : BigInteger.ZERO;
      return new pir.expression.Number(ival, new TypeRef(btype));
    } else {
      return val;
    }
  }
}
