package pir.passes;

import java.util.ArrayList;
import java.util.List;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.expression.reference.VarRef;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.Variable;
import pir.statement.Statement;
import pir.statement.StoreStmt;
import pir.statement.convert.SignExtendValue;
import pir.traverser.ExprTypeGetter;
import pir.type.SignedType;
import pir.type.Type;

import common.NameFactory;

/**
 * Extends types if needed for store statements
 * 
 * @author urs
 * 
 */
public class ValueExtender extends DefTraverser<List<Statement>, Void> {

  public static void process(Program obj) {
    ValueExtender changer = new ValueExtender();
    changer.traverse(obj, null);
  }

  @Override
  protected List<Statement> visitStoreStmt(StoreStmt obj, Void param) {
    // TODO add support for zext
    Type type = ExprTypeGetter.process(obj.getSrc(), ExprTypeGetter.NUMBER_AS_INT);
    assert (type instanceof SignedType);
    assert (obj.getDst().getType() instanceof SignedType);
    int srcBits = ((SignedType) type).getBits();
    int dstBits = ((SignedType) obj.getDst().getType()).getBits();

    if (dstBits > srcBits) {
      List<Statement> ret = new ArrayList<Statement>();

      Variable var = new SsaVariable(NameFactory.getNew(), obj.getDst().getType());
      SignExtendValue sext = new SignExtendValue(var, obj.getSrc());

      obj.setSrc(new VarRef(var));

      ret.add(sext);
      ret.add(obj);
      return ret;
    } else {
      assert (dstBits == srcBits);
      return null;
    }
  }

  // TODO share with ValueConverter
  @Override
  protected List<Statement> visitBasicBlock(BasicBlock obj, Void param) {
    ArrayList<Statement> stmts = new ArrayList<Statement>(obj.getCode());
    obj.getCode().clear();

    for (Statement stmt : stmts) {
      List<Statement> list = visit(stmt, null);
      if (list == null) {
        obj.getCode().add(stmt);
      } else {
        obj.getCode().addAll(list);
      }
    }

    List<Statement> list = visit(obj.getEnd(), null);
    if (list != null) {
      obj.getCode().addAll(list);
    }

    return null;
  }

}
