package pir.passes;

import java.util.ArrayList;
import java.util.List;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefItem;
import pir.expression.reference.VarRef;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.statement.ComplexWriter;
import pir.statement.GetElementPtr;
import pir.statement.LoadStmt;
import pir.statement.Statement;
import pir.type.ArrayType;
import pir.type.Type;

import common.NameFactory;

/**
 * Replaces read accesses to structs and arrays with getelementptr and load instructions
 * 
 * USE AFTER ComplexWriterReduction
 * 
 * @author urs
 * 
 */
public class ReferenceReadReduction extends DefTraverser<Void, List<Statement>> {

  public static void process(Program obj) {
    ReferenceReadReduction changer = new ReferenceReadReduction();
    changer.traverse(obj, null);
  }

  @Override
  protected Void visitVarRef(VarRef obj, List<Statement> param) {
    if (obj.getOffset().isEmpty()) {
      return null;
    }

    List<PExpression> offset = new ArrayList<PExpression>();

    Type type = obj.getRef().getType();

    offset.add(new Number(0));

    // TODO share with ComplexWriterReduction
    for (RefItem itm : obj.getOffset()) {
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
    GetElementPtr gep = new GetElementPtr(dstAddr, new VarRef(obj.getRef()), offset);

    SsaVariable value = new SsaVariable(NameFactory.getNew(), type);// FIXME use correct type
    LoadStmt load = new LoadStmt(value, new VarRef(dstAddr));

    obj.setRef(value);
    obj.getOffset().clear();

    param.add(gep);
    param.add(load);
    return null;
  }

  @Override
  protected Void visitComplexWriter(ComplexWriter obj, List<Statement> param) {
    throw new RuntimeException("not yet implemented"); // should not occur since it is removed by ComplexWriterReduction
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, List<Statement> param) {
    assert (param == null);
    ArrayList<Statement> stmts = new ArrayList<Statement>(obj.getCode());
    obj.getCode().clear();

    List<Statement> list = new ArrayList<Statement>();
    for (Statement stmt : stmts) {
      visit(stmt, list);
      list.add(stmt);
    }

    visit(obj.getEnd(), list);
    obj.getCode().addAll(list);

    // FIXME visit following phis?

    return null;
  }

}
