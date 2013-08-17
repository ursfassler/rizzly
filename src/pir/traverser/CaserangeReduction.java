package pir.traverser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pir.NullTraverser;
import pir.PirObject;
import pir.expression.PExpression;
import pir.expression.RelOp;
import pir.expression.Relation;
import pir.expression.reference.RefHead;
import pir.expression.reference.Reference;
import pir.other.FuncVariable;
import pir.statement.ArOp;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.Statement;
import pir.statement.VarDefStmt;
import pir.type.Type;

/**
 * Replaces a case range with equivalent code
 *
 * @author urs
 *
 */
public class CaserangeReduction extends StmtReplacer<Void> {
  static private int varNr = 0;
  static private CRangeDetector detector = new CRangeDetector();

  public static void process(PirObject cprog) {
    CaserangeReduction cVarDeclToTop = new CaserangeReduction();
    cVarDeclToTop.traverse(cprog, null);
  }

  private FuncVariable makeVar(PExpression expr) {
    Type type = ExprTypeGetter.process(expr);
    FuncVariable var = new FuncVariable("switchVar" + varNr, type);
    varNr++;
    return var;
  }

  @Override
  protected Statement visitCaseStmt(CaseStmt obj, Void param) {
    obj = (CaseStmt) super.visitCaseStmt(obj, param);
    boolean hasRange = detector.traverse(obj, null);
    if (hasRange) {
      return makeNewCase(obj);
    } else {
      return obj;
    }
  }

  public Statement makeNewCase(CaseStmt obj) {
    Block block = new Block();
    FuncVariable var = makeVar(obj.getCondition());
    block.getStatement().add(new VarDefStmt(var));
    block.getStatement().add(new Assignment(new Reference(new RefHead(var)), obj.getCondition()));

    IfStmt ifstmt = new IfStmt(obj.getOtherwise());

    List<CaseEntry> ncase = new ArrayList<CaseEntry>();
    for (CaseEntry itr : obj.getEntries()) {
      if (detector.traverse(itr, null)) {
        PExpression expr = convert(itr.getValues(), var);
        IfStmtEntry entry = new IfStmtEntry(expr, itr.getCode());
        ifstmt.getOption().add(entry);
      } else {
        ncase.add(itr);
      }
    }

    Block other = new Block();
    other.getStatement().add(ifstmt);
    CaseStmt nobj = new CaseStmt(new Reference(new RefHead(var)), other);
    nobj.getEntries().addAll(ncase);
    block.getStatement().add(nobj);

    return block;
  }

  private PExpression convert(List<CaseOptEntry> values, FuncVariable var) {
    PExpression expr;

    Iterator<CaseOptEntry> itr = values.iterator();
    assert (itr.hasNext());

    expr = convert(itr.next(), var);

    while (itr.hasNext()) {
      PExpression next = convert(itr.next(), var);
      expr = new ArithmeticOp(expr, next, ArOp.OR);
    }

    return expr;
  }

  private PExpression convert(CaseOptEntry next, FuncVariable var) {
    if (next instanceof CaseOptValue) {
      CaseOptValue value = (CaseOptValue) next;
      return new Relation(new Reference(new RefHead(var)), value.getValue(), RelOp.EQUAL);
    } else if (next instanceof CaseOptRange) {
      CaseOptRange range = (CaseOptRange) next;
      Relation low = new Relation(range.getStart(), new Reference(new RefHead(var)), RelOp.LESS_EQUAL);
      Relation high = new Relation(new Reference(new RefHead(var)), range.getEnd(), RelOp.LESS_EQUAL);
      return new ArithmeticOp(low, high, ArOp.AND);
    } else {
      throw new RuntimeException("not yet implemented: " + next.getClass().getCanonicalName());
    }
  }

}

class CRangeDetector extends NullTraverser<Boolean, Void> {

  @Override
  protected Boolean doDefault(PirObject obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitCaseEntry(CaseEntry obj, Void param) {
    for (CaseOptEntry opt : obj.getValues()) {
      if (visit(opt, param)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected Boolean visitCaseOptValue(CaseOptValue obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitCaseOptRange(CaseOptRange obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitCaseStmt(CaseStmt obj, Void param) {
    for (CaseEntry opt : obj.getEntries()) {
      if (visit(opt, param)) {
        return true;
      }
    }
    return false;
  }

}
