package pir.passes;

import java.util.ArrayList;
import java.util.List;

import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefItem;
import pir.expression.reference.VarRef;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.statement.ComplexWriter;
import pir.statement.GetElementPtr;
import pir.statement.Statement;
import pir.statement.StoreStmt;
import pir.traverser.StmtReplacer;
import pir.type.ArrayType;
import pir.type.Type;

import common.NameFactory;

/**
 * Replaces write accesses to structs and arrays with getelementptr and store instructions
 * 
 * @author urs
 * 
 */
public class ComplexWriterReduction extends StmtReplacer<Void> {

  public static void process(Program obj) {
    ComplexWriterReduction changer = new ComplexWriterReduction();
    changer.traverse(obj, null);
  }

  @Override
  protected List<Statement> visitComplexWriter(ComplexWriter obj, Void param) {
    List<PExpression> offset = new ArrayList<PExpression>();

    Type type = obj.getDst().getRef().getType();

    offset.add(new Number(0));

    for (RefItem itm : obj.getDst().getOffset()) {
      if (itm instanceof RefIndex) {
        assert (type instanceof ArrayType);
        PExpression idx = ((RefIndex) itm).getIndex();
        offset.add(idx);
        type = ((ArrayType) type).getType();
      } else {
        throw new RuntimeException("not yet implemented: " + itm.getClass().getCanonicalName());
      }
    }

    SsaVariable dstAddr = new SsaVariable(NameFactory.getNew(), type); // FIXME use correct type
    GetElementPtr gep = new GetElementPtr(dstAddr, new VarRef(obj.getDst().getRef()), offset);
    StoreStmt store = new StoreStmt(new VarRef(dstAddr), obj.getSrc());

    return add(gep, store);
  }

}
