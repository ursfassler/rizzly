package fun.traverser;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;

import util.ssa.BbEdge;

import common.ElementInfo;

import fun.DefGTraverser;
import fun.Fun;
import fun.NullTraverser;
import fun.cfg.BasicBlock;
import fun.cfg.BasicBlockEnd;
import fun.cfg.BasicBlockList;
import fun.cfg.CaseGoto;
import fun.cfg.CaseGotoOpt;
import fun.cfg.Goto;
import fun.cfg.IfGoto;
import fun.doc.PrettyPrinter;
import fun.expression.Expression;
import fun.function.FuncWithBody;
import fun.function.FunctionHeader;
import fun.knowledge.KnowledgeBase;
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

public class MakeBasicBlocks extends DefGTraverser<Void, Void> {
  private KnowledgeBase kb;

  public MakeBasicBlocks(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  static public void process(Fun obj, KnowledgeBase kb) {
    MakeBasicBlocks mbb = new MakeBasicBlocks(kb);
    mbb.traverse(obj, null);
  }

  @Override
  protected Void visitFunctionHeader(FunctionHeader obj, Void param) {
    if (obj instanceof FuncWithBody) {
      Block body = (Block) ((FuncWithBody) obj).getBody();
      MbbTranslator translator = new MbbTranslator();
      translator.traverse(body, null);

      BasicBlockList bbBody = new BasicBlockList(body.getInfo());
      bbBody.getBasicBlocks().addAll(translator.getBbset());
      ((FuncWithBody) obj).setBody(bbBody);

      PrettyPrinter.print(obj, kb.getRootdir() + "bb" + obj.getInfo().getLine() + ".rzy");
    }
    return null;
  }

}

/**
 *
 * @author urs
 *
 */
class MbbTranslator extends NullTraverser<BasicBlock, BasicBlock> {
  private LinkedList<BasicBlock> bblist = new LinkedList<BasicBlock>();

  public LinkedList<BasicBlock> getBbset() {
    return bblist;
  }

  private BasicBlock makeBb(ElementInfo info) {
    BasicBlock bb = new BasicBlock(info, bblist.size());
    bblist.add(bb);
    return bb;
  }

  private IfGoto makeIf(Expression cond, BasicBlock bbThen, BasicBlock bbElse) {
    IfGoto ifStmt = new IfGoto(cond.getInfo());
    ifStmt.setCondition(cond);
    ifStmt.setThenBlock(bbThen);
    ifStmt.setElseBlock(bbElse);
    return ifStmt;
  }

  private void addGoto(BasicBlock code, BasicBlock dst) {
    List<Statement> lst = code.getCode();
    if (code.getEnd() == null) {
      code.setEnd(new Goto(dst.getInfo(), dst));
    }
  }

  @Override
  public BasicBlock traverse(Fun obj, BasicBlock param) {
    assert (param == null);
    BasicBlock last = super.traverse(obj, null);
    BasicBlock exit = makeBb(obj.getInfo());
    exit.setEnd(new ReturnVoid(obj.getInfo()));
    addGoto(last, exit);

    removeUnreachable(bblist);

    return null;
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
      bb = visit(stmt, bb);
      if (bb instanceof BasicBlockEnd) {
        return bb;
      }
    }

    return bb;
  }

  @Override
  protected BasicBlock visitReturnExpr(ReturnExpr obj, BasicBlock param) {
    assert (param.getEnd() == null);
    param.setEnd(obj);
    return param;
  }

  @Override
  protected BasicBlock visitReturnVoid(ReturnVoid obj, BasicBlock param) {
    assert (param.getEnd() == null);
    param.setEnd(obj);
    return param;
  }

  @Override
  protected BasicBlock visitWhile(While obj, BasicBlock param) {
    BasicBlock head = makeBb(obj.getInfo());
    BasicBlock sub = visit(obj.getBody(), null);
    BasicBlock exit = makeBb(obj.getCondition().getInfo());

    addGoto(param, head);
    addGoto(sub, head);

    IfGoto ifStmt = makeIf(obj.getCondition(), sub, exit);
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
      IfGoto ifStmt = makeIf(opt.getCondition(), bbthen, bbelse);
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
    co.setCondition(obj.getCondition());
    assert (param.getEnd() == null);
    param.setEnd(co);

    for (CaseOpt itr : obj.getOption()) {
      BasicBlock caseBb = visit(itr.getCode(), null);
      addGoto(caseBb, join);
      CaseGotoOpt cgo = new CaseGotoOpt(itr.getInfo(), itr.getValue(), caseBb);
      co.getOption().add(cgo);
    }
    BasicBlock otherBb = visit(obj.getOtherwise(), null);
    addGoto(otherBb, join);
    co.setOtherwise(otherBb);

    return join;
  }

  @Override
  protected BasicBlock visitAssignment(Assignment obj, BasicBlock param) {
    param.getCode().add(obj);
    return param;
  }

  @Override
  protected BasicBlock visitVarDef(VarDefStmt obj, BasicBlock param) {
    param.getCode().add(0, obj);
    return param;
  }

  @Override
  protected BasicBlock visitCallStmt(CallStmt obj, BasicBlock param) {
    param.getCode().add(obj);
    return param;
  }

}
