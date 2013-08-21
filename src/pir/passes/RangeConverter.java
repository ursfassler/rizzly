package pir.passes;

import java.util.ArrayList;
import java.util.List;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.cfg.PhiStmt;
import pir.expression.Number;
import pir.expression.reference.VarRefSimple;
import pir.know.KnowBaseItem;
import pir.know.KnowledgeBase;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.Statement;
import pir.statement.convert.TypeCast;
import pir.traverser.ExprTypeGetter;
import pir.type.RangeType;
import pir.type.Type;
import pir.type.TypeRef;

import common.NameFactory;

/**
 * Extends and truncates types of values used in an arithmetic operation
 * 
 * @author urs
 * 
 */
public class RangeConverter extends DefTraverser<List<Statement>, Void> {
  private KnowBaseItem kbi;

  public RangeConverter(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static void process(Program obj, KnowledgeBase kb) {
    RangeConverter changer = new RangeConverter(kb);
    changer.traverse(obj, null);
  }

  @Override
  protected List<Statement> visitArithmeticOp(ArithmeticOp obj, Void param) {
    Type lt = ExprTypeGetter.process(obj.getLeft(), true);
    Type rt = ExprTypeGetter.process(obj.getRight(), true);
    Type dt = obj.getVariable().getType().getRef();

    assert (lt instanceof RangeType);
    assert (rt instanceof RangeType);
    assert (dt instanceof RangeType);

    RangeType it = RangeType.makeContainer((RangeType) lt, (RangeType) rt);
    RangeType bt = RangeType.makeContainer(it, (RangeType) dt);
    bt = kbi.getRangeType(bt.getLow(), bt.getHigh()); // add bt to program

    List<Statement> ret = new ArrayList<Statement>();

    if (RangeType.isBigger(bt, (RangeType) lt)) {
      SsaVariable lev = new SsaVariable(NameFactory.getNew(), new TypeRef(bt));
      TypeCast lex = new TypeCast(lev, obj.getLeft());
      obj.setLeft(new VarRefSimple(lev));
      ret.add(lex);
    }

    if (RangeType.isBigger(bt, (RangeType) rt)) {
      SsaVariable rev = new SsaVariable(NameFactory.getNew(), new TypeRef(bt));
      TypeCast rex = new TypeCast(rev, obj.getRight());
      obj.setRight(new VarRefSimple(rev));
      ret.add(rex);
    }

    ret.add(obj);

    if (RangeType.isBigger(bt, (RangeType) dt)) {
      SsaVariable irv = new SsaVariable(NameFactory.getNew(), new TypeRef(bt));
      TypeCast rex = new TypeCast(obj.getVariable(), new VarRefSimple(irv));
      obj.setVariable(irv);
      ret.add(rex);
    }

    return ret;
  }

  @Override
  protected List<Statement> visitPhiStmt(PhiStmt obj, Void param) {
    return super.visitPhiStmt(obj, param); // TODO implement it
  }

  @Override
  protected List<Statement> visitAssignment(Assignment obj, Void param) {
    if (obj.getSrc() instanceof Number) {
      return null;
    }

    Type lt = ExprTypeGetter.process(obj.getSrc(), true);
    Type dt = obj.getVariable().getType().getRef();

    assert (lt instanceof RangeType);
    assert (dt instanceof RangeType);

    RangeType bt = RangeType.makeContainer((RangeType) lt, (RangeType) dt);
    bt = kbi.getRangeType(bt.getLow(), bt.getHigh()); // add bt to program

    List<Statement> ret = new ArrayList<Statement>();

    if (RangeType.isBigger(bt, (RangeType) lt)) {
      SsaVariable lev = new SsaVariable(NameFactory.getNew(), new TypeRef(bt));
      TypeCast lex = new TypeCast(lev, obj.getSrc());
      obj.setSrc(new VarRefSimple(lev));
      ret.add(lex);
    }

    ret.add(obj);

    if (RangeType.isBigger(bt, (RangeType) dt)) {
      SsaVariable irv = new SsaVariable(NameFactory.getNew(), new TypeRef(bt));
      TypeCast rex = new TypeCast(obj.getVariable(), new VarRefSimple(irv));
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
