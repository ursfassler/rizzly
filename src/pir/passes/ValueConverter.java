package pir.passes;

import java.util.ArrayList;
import java.util.List;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.expression.reference.VarRef;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.Variable;
import pir.statement.ArithmeticOp;
import pir.statement.Statement;
import pir.statement.convert.SignExtendValue;
import pir.statement.convert.TruncValue;
import pir.traverser.ExprTypeGetter;
import pir.type.RangeType;
import pir.type.Type;

import common.NameFactory;

/**
 * Extends and truncates types of values used in an arithmetic operation
 * 
 * @author urs
 * 
 */
@Deprecated // do it already in EVL
public class ValueConverter extends DefTraverser<List<Statement>, Void> {

  public static void process(Program obj) {
    ValueConverter changer = new ValueConverter();
    changer.traverse(obj, null);
  }

  @Override
  protected List<Statement> visitArithmeticOp(ArithmeticOp obj, Void param) {
    Type lt = ExprTypeGetter.process(obj.getLeft(), true);
    Type rt = ExprTypeGetter.process(obj.getRight(), true);
    Type dt = obj.getVariable().getType();

    assert (lt instanceof RangeType);
    assert (rt instanceof RangeType);
    assert (dt instanceof RangeType);

    RangeType it = RangeType.makeContainer((RangeType) lt, (RangeType) rt);
    RangeType bt = RangeType.makeContainer(it, (RangeType) dt);
    // TODO add bt to program types

    List<Statement> ret = new ArrayList<Statement>();

    if (RangeType.isBigger(bt, (RangeType) lt)) {
      Variable lev = new SsaVariable(NameFactory.getNew(), bt);
      SignExtendValue lex = new SignExtendValue(lev, obj.getLeft());
      obj.setLeft(new VarRef(lev));
      ret.add(lex);
    }

    if (RangeType.isBigger(bt, (RangeType) rt)) {
      Variable rev = new SsaVariable(NameFactory.getNew(), bt);
      SignExtendValue rex = new SignExtendValue(rev, obj.getRight());
      obj.setRight(new VarRef(rev));
      ret.add(rex);
    }

    ret.add(obj);

    if (RangeType.isBigger(it, (RangeType) dt)) {
      Variable irv = new SsaVariable(NameFactory.getNew(), it);
      TruncValue rex = new TruncValue(obj.getVariable(), new VarRef(irv));
      obj.setVariable(irv);
      ret.add(rex);
    }

    return ret;
  }

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
