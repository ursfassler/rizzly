package fun.toevl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import common.ElementInfo;

import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockEnd;
import evl.cfg.BasicBlockList;
import evl.cfg.CaseGoto;
import evl.cfg.CaseGotoOpt;
import evl.cfg.CaseOptEntry;
import evl.cfg.Goto;
import evl.cfg.IfGoto;
import evl.expression.Expression;
import evl.expression.reference.Reference;
import fun.Fun;
import fun.NullTraverser;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.CallStmt;
import fun.statement.CaseOpt;
import fun.statement.CaseStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.Statement;
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.variable.FuncVariable;

/**
 * Translates body of functions from FUN to EVL, including translation into Basic Block representation
 *
 * @author urs
 *
 */
class MakeBasicBlocks extends NullTraverser<BasicBlock, BasicBlock> {
  final private FunToEvl fta;
  final private LinkedList<BasicBlock> bblist = new LinkedList<BasicBlock>();

  public MakeBasicBlocks(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  public BasicBlockList translate(Block body, List<FuncVariable> param) {
    BasicBlock head = makeBb(body.getInfo());

    for (FuncVariable var : param) {
      // this is later translated into an SSA variable
      evl.variable.FuncVariable ev = (evl.variable.FuncVariable) fta.traverse(var, null);
      evl.statement.Assignment vardef = new evl.statement.Assignment(var.getInfo(), new Reference(new ElementInfo(), ev), new Reference(new ElementInfo(), ev));
      head.getCode().add(vardef);
    }

    BasicBlock last = visit(body, null);
    BasicBlock exit = makeBb(body.getInfo());
    exit.setEnd((BasicBlockEnd) fta.traverse(new ReturnVoid(body.getInfo()), null));
    addGoto(last, exit);
    addGoto(head, bblist.get(1));

    removeUnreachable(bblist);

    BasicBlockList bbBody = new BasicBlockList(body.getInfo());
    bbBody.getBasicBlocks().addAll(bblist);

    return bbBody;
  }

  private BasicBlock makeBb(ElementInfo info) {
    BasicBlock bb = new BasicBlock(info, "BB_" + bblist.size());
    bblist.add(bb);
    return bb;
  }

  private IfGoto makeIf(evl.expression.Expression cond, BasicBlock bbThen, BasicBlock bbElse) {
    IfGoto ifStmt = new IfGoto(cond.getInfo());
    ifStmt.setCondition(cond);
    ifStmt.setThenBlock(bbThen);
    ifStmt.setElseBlock(bbElse);
    return ifStmt;
  }

  private void addGoto(BasicBlock code, BasicBlock dst) {
    List<evl.statement.Statement> lst = code.getCode();
    if (code.getEnd() == null) {
      code.setEnd(new Goto(dst.getInfo(), dst));
    }
  }

  static private void removeUnreachable(LinkedList<BasicBlock> vertices) {
    Set<BasicBlock> reachable = new HashSet<BasicBlock>();
    LinkedList<BasicBlock> test = new LinkedList<BasicBlock>();
    test.add(vertices.getFirst());

    while (!test.isEmpty()) {
      BasicBlock u = test.pop();
      if (!reachable.contains(u)) {
        reachable.add(u);
        test.addAll(u.getEnd().getJumpDst());
      }
    }

    vertices.retainAll(reachable);
  }

  @Override
  protected BasicBlock visitDefault(Fun obj, BasicBlock param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected BasicBlock visitBlock(Block obj, BasicBlock param) {
    BasicBlock bb = makeBb(obj.getInfo());

    for (Statement stmt : obj.getStatements()) {
      BasicBlock nbb = visit(stmt, bb);
      if (nbb.getEnd() != null) {
        // TODO add warning for unreachable code?
        return nbb;
      }
      if (nbb != bb) {
        addGoto(bb, nbb);
      }
      bb = nbb;
    }

    return bb;
  }

  @Override
  protected BasicBlock visitReturnExpr(ReturnExpr obj, BasicBlock param) {
    assert (param.getEnd() == null);
    param.setEnd((BasicBlockEnd) fta.traverse(obj, null));
    return param;
  }

  @Override
  protected BasicBlock visitReturnVoid(ReturnVoid obj, BasicBlock param) {
    assert (param.getEnd() == null);
    param.setEnd((BasicBlockEnd) fta.traverse(obj, null));
    return param;
  }

  @Override
  protected BasicBlock visitWhile(While obj, BasicBlock param) {
    BasicBlock head = makeBb(obj.getInfo());
    BasicBlock sub = visit(obj.getBody(), null);
    BasicBlock exit = makeBb(obj.getCondition().getInfo());

    addGoto(param, head);
    addGoto(sub, head);

    IfGoto ifStmt = makeIf((evl.expression.Expression) fta.traverse(obj.getCondition(), null), sub, exit);
    head.setEnd(ifStmt);

    return exit;
  }

  @Override
  protected BasicBlock visitIfStmt(IfStmt obj, BasicBlock param) {
    BasicBlock join = makeBb(obj.getInfo());
    for (IfOption opt : obj.getOption()) {
      BasicBlock bbthen = visit(opt.getCode(), null);
      addGoto(bbthen, join);

      BasicBlock bbelse = makeBb(obj.getInfo());
      IfGoto ifStmt = makeIf((Expression) fta.traverse(opt.getCondition(), null), bbthen, bbelse);
      assert (param.getEnd() == null);
      param.setEnd(ifStmt);

      param = bbelse;
    }
    BasicBlock lastOpt = visit(obj.getDefblock(), null);
    addGoto(param, lastOpt);
    addGoto(lastOpt, join);

    return join;
  }

  @Override
  protected BasicBlock visitCaseStmt(CaseStmt obj, BasicBlock param) {
    BasicBlock join = makeBb(obj.getInfo());

    CaseGoto co = new CaseGoto(obj.getInfo());
    co.setCondition((Expression) fta.traverse(obj.getCondition(), null));
    assert (param.getEnd() == null);
    param.setEnd(co);

    for (CaseOpt itr : obj.getOption()) {
      BasicBlock caseBb = visit(itr.getCode(), null);
      addGoto(caseBb, join);
      List<CaseOptEntry> optlist = new ArrayList<CaseOptEntry>(obj.getOption().size());
      for (fun.statement.CaseOptEntry opt : itr.getValue()) {
        optlist.add((CaseOptEntry) fta.traverse(opt, null));
      }
      CaseGotoOpt cgo = new CaseGotoOpt(itr.getInfo(), optlist, caseBb);
      co.getOption().add(cgo);
    }
    BasicBlock otherBb = visit(obj.getOtherwise(), null);
    addGoto(otherBb, join);
    co.setOtherwise(otherBb);

    return join;
  }

  @Override
  protected BasicBlock visitAssignment(Assignment obj, BasicBlock param) {
    param.getCode().add((evl.statement.Statement) fta.traverse(obj, null));
    return param;
  }

  @Override
  protected BasicBlock visitVarDef(VarDefStmt obj, BasicBlock param) {
    param.getCode().add((evl.statement.Statement) fta.traverse(obj, null));
    return param;
  }

  @Override
  protected BasicBlock visitCallStmt(CallStmt obj, BasicBlock param) {
    param.getCode().add((evl.statement.Statement) fta.traverse(obj, null));
    return param;
  }

}