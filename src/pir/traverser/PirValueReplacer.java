package pir.traverser;

import java.util.ArrayList;
import java.util.Collection;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.cfg.CaseGoto;
import pir.cfg.IfGoto;
import pir.cfg.PhiStmt;
import pir.cfg.ReturnExpr;
import pir.expression.reference.RefIndex;
import pir.expression.reference.VarRef;
import pir.other.PirValue;
import pir.statement.ArithmeticOp;
import pir.statement.Assignment;
import pir.statement.CallAssignment;
import pir.statement.CallStmt;
import pir.statement.GetElementPtr;
import pir.statement.LoadStmt;
import pir.statement.Relation;
import pir.statement.StoreStmt;
import pir.statement.convert.ConvertValue;

abstract public class PirValueReplacer<R, P> extends DefTraverser<R, P> {

  abstract protected PirValue replace(PirValue val, P param);

  protected void repList(Collection<PirValue> list, P param) {
    ArrayList<PirValue> clist = new ArrayList<PirValue>(list);
    list.clear();
    for (PirValue val : clist) {
      list.add(replace(val, param));
    }
  }

  @Override
  protected R visitCallAssignment(CallAssignment obj, P param) {
    repList(obj.getParameter(), param);
    return super.visitCallAssignment(obj, param);
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    repList(obj.getParameter(), param);
    return super.visitCallStmt(obj, param);
  }

  @Override
  protected R visitStoreStmt(StoreStmt obj, P param) {
    obj.setSrc(replace(obj.getSrc(), param));
    obj.setDst(replace(obj.getDst(), param));
    return super.visitStoreStmt(obj, param);
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    obj.setLeft(replace(obj.getLeft(), param));
    obj.setRight(replace(obj.getRight(), param));
    return super.visitArithmeticOp(obj, param);
  }

  @Override
  protected R visitRelation(Relation obj, P param) {
    obj.setLeft(replace(obj.getLeft(), param));
    obj.setRight(replace(obj.getRight(), param));
    return super.visitRelation(obj, param);
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    obj.setValue(replace(obj.getValue(), param));
    return super.visitReturnExpr(obj, param);
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitPhiStmt(PhiStmt obj, P param) {
    for (BasicBlock in : new ArrayList<BasicBlock>(obj.getInBB())) {
      obj.addArg(in, replace(obj.getArg(in), param));
    }
    return super.visitPhiStmt(obj, param);
  }

  @Override
  protected R visitIfGoto(IfGoto obj, P param) {
    obj.setCondition(replace(obj.getCondition(), param));
    return super.visitIfGoto(obj, param);
  }

  @Override
  protected R visitCaseGoto(CaseGoto obj, P param) {
    obj.setCondition(replace(obj.getCondition(), param));
    return super.visitCaseGoto(obj, param);
  }

  @Override
  protected R visitVarRef(VarRef obj, P param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitLoadStmt(LoadStmt obj, P param) {
    obj.setSrc(replace(obj.getSrc(),param));
    return super.visitLoadStmt(obj, param);
  }

  @Override
  protected R visitGetElementPtr(GetElementPtr obj, P param) {
    obj.setBase(replace(obj.getBase(),param));
    repList(obj.getOffset(),param);
    return super.visitGetElementPtr(obj, param);
  }

  @Override
  protected R visitConvertValue(ConvertValue obj, P param) {
    obj.setOriginal(replace(obj.getOriginal(), param));
    return super.visitConvertValue(obj, param);
  }

}
