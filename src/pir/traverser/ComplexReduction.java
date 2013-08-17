package pir.traverser;

import java.util.ArrayList;
import java.util.List;

import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefItem;
import pir.other.Program;
import pir.statement.ComplexWriter;
import pir.statement.Insertvalue;
import pir.statement.Statement;
import pir.type.Array;
import pir.type.Type;

/**
 * Replaces write accesses to structs and arrays with insertvalue instructions
 * 
 * @author urs
 * 
 */
public class ComplexReduction extends StmtReplacer<Void> {

  public static void process(Program obj) {
    ComplexReduction changer = new ComplexReduction();
    changer.traverse(obj, null);
  }

  @Override
  protected List<Statement> visitComplexWriter(ComplexWriter obj, Void param) {
    List<Statement> ret = new ArrayList<Statement>();
    List<Integer> offset = new ArrayList<Integer>();

    Type type = obj.getDst().getRef().getType();

    for (RefItem itm : obj.getDst().getOffset()) {
      if (itm instanceof RefIndex) {
        assert( type instanceof Array );
        PExpression idx = ((RefIndex) itm).getIndex();
        Number ofn = (Number) idx;    //FIXME only in a special case correct
        offset.add( ofn.getValue() );
      } else {
        throw new RuntimeException("not yet implemented: " + itm.getClass().getCanonicalName());
      }
    }
    
    return add( new Insertvalue(dst, obj.g, src, index) )

    assert (false);

    return ret;
  }

}
