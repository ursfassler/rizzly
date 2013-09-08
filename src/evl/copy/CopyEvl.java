package evl.copy;

import evl.other.RizzlyProgram;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Direction;

import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.CaseGoto;
import evl.cfg.CaseGotoOpt;
import evl.cfg.CaseOptEntry;
import evl.cfg.Goto;
import evl.cfg.IfGoto;
import evl.cfg.PhiStmt;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.expression.Expression;
import evl.expression.reference.RefItem;
import evl.function.FunctionBase;
import evl.hfsm.StateItem;
import evl.other.CompUse;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.NamedList;
import evl.statement.Statement;
import evl.type.Type;
import evl.type.TypeRef;
import evl.variable.Variable;

class CopyEvl extends NullTraverser<Evl, Void> {
  // / keeps the old -> new Named objects in order to relink references
  private Map<Named, Named> copied = new HashMap<Named, Named>();
  private CopyFunction func = new CopyFunction(this);
  private CopyVariable var = new CopyVariable(this);
  private CopyExpression expr = new CopyExpression(this);
  private CopyType type = new CopyType(this);
  private CopyStatement stmt = new CopyStatement(this);
  private CopyRef ref = new CopyRef(this);
  private CopyCaseOptEntry caoe = new CopyCaseOptEntry(this);
  private CopyStateItem cosi = new CopyStateItem(this);

  public Map<Named, Named> getCopied() {
    return copied;
  }

  @SuppressWarnings("unchecked")
  public <T extends Evl> T copy(T obj) {
    return (T) visit(obj, null);
  }

  public <T extends Evl> Collection<T> copy(Collection<T> obj) {
    ArrayList<T> ret = new ArrayList<T>();
    for (T itr : obj) {
      ret.add(copy(itr));
    }
    return ret;
  }

  @Override
  protected Evl visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Evl visit(Evl obj, Void param) {
    if (obj instanceof Named) {
      if (copied.containsKey(obj)) {
        Named ret = copied.get(obj);
        assert (ret != null);
        return ret;
      } else {
        Evl nobj = super.visit(obj, param);
        assert (nobj instanceof Named);
        copied.put((Named) obj, (Named) nobj);
        return nobj;
      }
    }
    return super.visit(obj, param);
  }

  @Override
  protected Evl visitRizzlyProgram(RizzlyProgram obj, Void param) {
    RizzlyProgram ret = new RizzlyProgram(obj.getRootdir(), obj.getName());
    ret.getConstant().addAll( copy(obj.getConstant().getList()) );
    ret.getFunction().addAll( copy(obj.getFunction().getList()) );
    ret.getType().addAll( copy(obj.getType().getList()) );
    ret.getVariable().addAll( copy(obj.getVariable().getList()) );
    return ret;
  }

  @Override
  protected Evl visitNamedList(NamedList<Named> obj, Void param) {
    NamedList<Named> ret = new NamedList<Named>(obj.getInfo(), obj.getName());
    ret.addAll(copy(obj.getList()));
    return ret;
  }

  @Override
  protected Evl visitFunctionBase(FunctionBase obj, Void param) {
    return func.traverse(obj, param);
  }

  @Override
  protected Evl visitVariable(Variable obj, Void param) {
    return var.traverse(obj, param);
  }

  @Override
  protected Evl visitExpression(Expression obj, Void param) {
    return expr.traverse(obj, param);
  }

  @Override
  protected Evl visitType(Type obj, Void param) {
    return type.traverse(obj, param);
  }

  @Override
  protected Evl visitStatement(Statement obj, Void param) {
    return stmt.traverse(obj, param);
  }

  @Override
  protected Evl visitRefItem(RefItem obj, Void param) {
    return ref.traverse(obj, param);
  }

  @Override
  protected Evl visitCaseOptEntry(CaseOptEntry obj, Void param) {
    return caoe.traverse(obj, param);
  }

  @Override
  protected Evl visitStateItem(StateItem obj, Void param) {
    return cosi.traverse(obj, param);
  }

  @Override
  protected Evl visitCompUse(CompUse obj, Void param) {
    return new CompUse(obj.getInfo(), obj.getName(), obj.getLink()); // we keep link to old Component
  }

  @Override
  protected Evl visitIfaceUse(IfaceUse obj, Void param) {
    return new IfaceUse(obj.getInfo(), obj.getName(), obj.getLink()); // we keep link to old Interface
  }

  @Override
  protected Evl visitImplElementary(ImplElementary obj, Void param) {
    ImplElementary ret = new ImplElementary(obj.getInfo(), obj.getName());

    ret.getIface(Direction.in).addAll(copy(obj.getIface(Direction.in).getList()));
    ret.getIface(Direction.out).addAll(copy(obj.getIface(Direction.out).getList()));
    ret.getVariable().addAll(copy(obj.getVariable().getList()));
    ret.getConstant().addAll(copy(obj.getConstant().getList()));
    ret.getComponent().addAll(copy(obj.getComponent().getList()));
    ret.getInternalFunction().addAll(copy(obj.getInternalFunction().getList()));
    ret.getInputFunc().addAll(copy(obj.getInputFunc().getList()));
    ret.getSubComCallback().addAll(copy(obj.getSubComCallback().getList()));
    ret.setEntryFunc(copy(obj.getEntryFunc()));
    ret.setExitFunc(copy(obj.getExitFunc()));

    return ret;
  }

  @Override
  protected Evl visitBasicBlockList(BasicBlockList obj, Void param) {
    BasicBlockList bbl = new BasicBlockList(obj.getInfo(),copy(obj.getEntry()),copy(obj.getExit()));
    bbl.getBasicBlocks().addAll(copy(obj.getBasicBlocks()));
    return bbl;
  }

  @Override
  protected Evl visitBasicBlock(BasicBlock obj, Void param) {
    BasicBlock bb = new BasicBlock(obj.getInfo(), obj.getName());
    copied.put(obj, bb);
    bb.getPhi().addAll(copy(obj.getPhi()));
    bb.getCode().addAll(copy(obj.getCode()));
    bb.setEnd(copy(obj.getEnd()));
    return bb;
  }

  @Override
  protected Evl visitCaseGoto(CaseGoto obj, Void param) {
    CaseGoto ret = new CaseGoto(obj.getInfo());
    ret.setCondition(copy(obj.getCondition()));
    ret.getOption().addAll(copy(obj.getOption()));
    ret.setOtherwise(copy(obj.getOtherwise()));
    return ret;
  }

  @Override
  protected Evl visitCaseGotoOpt(CaseGotoOpt obj, Void param) {
    return new CaseGotoOpt(obj.getInfo(), (List<CaseOptEntry>) copy(obj.getValue()), copy(obj.getDst()));
  }

  @Override
  protected Evl visitIfGoto(IfGoto obj, Void param) {
    IfGoto ret = new IfGoto(obj.getInfo());
    ret.setCondition(copy(obj.getCondition()));
    ret.setThenBlock(copy(obj.getThenBlock()));
    ret.setElseBlock(copy(obj.getElseBlock()));
    return ret;
  }

  @Override
  protected Evl visitGoto(Goto obj, Void param) {
    return new Goto(obj.getInfo(), copy(obj.getTarget()));
  }

  @Override
  protected Evl visitReturnExpr(ReturnExpr obj, Void param) {
    return new ReturnExpr(obj.getInfo(), copy(obj.getExpr()));
  }

  @Override
  protected Evl visitReturnVoid(ReturnVoid obj, Void param) {
    return new ReturnVoid(obj.getInfo());
  }

  @Override
  protected Evl visitPhiStmt(PhiStmt obj, Void param) {
    PhiStmt ret = new PhiStmt(obj.getInfo(), copy(obj.getVariable()));
    for (BasicBlock in : obj.getInBB()) {
      ret.addArg(copy(in), copy(obj.getArg(in)));
    }
    return ret;
  }

  @Override
  protected Evl visitTypeRef(TypeRef obj, Void param) {
    return new TypeRef(obj.getInfo(), obj.getRef());
  }

}
