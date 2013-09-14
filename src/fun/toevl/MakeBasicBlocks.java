package fun.toevl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import common.ElementInfo;
import common.NameFactory;

import error.ErrorType;
import error.RError;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.statement.bbend.CaseGoto;
import evl.statement.bbend.CaseGotoOpt;
import evl.statement.bbend.CaseOptEntry;
import evl.statement.bbend.Goto;
import evl.statement.bbend.IfGoto;
import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.statement.bbend.BasicBlockEnd;
import evl.statement.normal.NormalStmt;
import evl.type.TypeRef;
import evl.type.special.VoidType;
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
  private evl.variable.FuncVariable result;
  private BasicBlock exit;

  public MakeBasicBlocks(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  public BasicBlockList translate(Block body, List<FuncVariable> param, TypeRef retType) {
    BasicBlock head = new BasicBlock(body.getInfo(), "BB_entry");
    exit = new BasicBlock(body.getInfo(), "BB_exit");

    if( retType.getRef() instanceof VoidType ) {
      result = null;
      exit.setEnd(new evl.statement.bbend.ReturnVoid(body.getInfo()));
    } else {
      result = new evl.variable.FuncVariable(body.getInfo(), NameFactory.getNew(), retType);
      head.getCode().add(new evl.statement.normal.VarDefStmt(body.getInfo(), result));
      exit.setEnd(new evl.statement.bbend.ReturnExpr(body.getInfo(), new Reference(body.getInfo(), result)));
    }

    BasicBlock last = visit(body, makeBb(body.getInfo()));
    addGoto(last, exit);
    assert ( !bblist.isEmpty() );
    addGoto(head, bblist.get(0));
    removeUnreachable(head, exit, bblist, body.getInfo());

    BasicBlockList bbBody = new BasicBlockList(body.getInfo(), head, exit);
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
    List<NormalStmt> lst = code.getCode();
    if( code.getEnd() == null ) {
      code.setEnd(new Goto(dst.getInfo(), dst));
    }
  }

  static private void removeUnreachable(BasicBlock head, BasicBlock exit, Collection<BasicBlock> vertices, ElementInfo info) {
    Set<BasicBlock> reachable = new HashSet<BasicBlock>();
    LinkedList<BasicBlock> test = new LinkedList<BasicBlock>();
    test.add(head);

    while( !test.isEmpty() ) {
      BasicBlock u = test.pop();
      if( !reachable.contains(u) ) {
        reachable.add(u);
        test.addAll(u.getEnd().getJumpDst());
      }
    }

    Set<BasicBlock> unreachable = new HashSet<BasicBlock>(vertices);
    unreachable.removeAll(reachable);

    vertices.retainAll(reachable);
    if( !reachable.contains(exit) ) {
      RError.err(ErrorType.Error, info, "function does not return");
    }

  }

  @Override
  protected BasicBlock visitDefault(Fun obj, BasicBlock param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected BasicBlock visitBlock(Block obj, BasicBlock param) {
    assert ( param != null );

    for( Statement stmt : obj.getStatements() ) {
      BasicBlock nbb = visit(stmt, param);
      if( nbb.getEnd() != null ) {
        // TODO add warning for unreachable code?
        return nbb;
      }
      if( nbb != param ) {
        addGoto(param, nbb);
      }
      param = nbb;
    }

    return param;
  }

  @Override
  protected BasicBlock visitReturnExpr(ReturnExpr obj, BasicBlock param) {
    assert ( param != null );
    assert ( param.getEnd() == null );

    if( result == null ) {
      RError.err(ErrorType.Error, obj.getInfo(), "Function has no return value");
      return param;
    }

    Expression retVal = (Expression) fta.traverse(obj.getExpr(), null);
    evl.statement.normal.Assignment ass = new evl.statement.normal.Assignment(obj.getInfo(), new Reference(obj.getInfo(), result), retVal);
    param.getCode().add(ass);

    param.setEnd(new Goto(obj.getInfo(), exit));
    return param;
  }

  @Override
  protected BasicBlock visitReturnVoid(ReturnVoid obj, BasicBlock param) {
    assert ( param != null );
    assert ( param.getEnd() == null );
    param.setEnd((BasicBlockEnd) fta.traverse(obj, null));
    return param;
  }

  @Override
  protected BasicBlock visitWhile(While obj, BasicBlock param) {
    assert ( param != null );
    BasicBlock head = makeBb(obj.getInfo());
    BasicBlock sub = visit(obj.getBody(), makeBb(obj.getInfo()));
    BasicBlock exit = makeBb(obj.getCondition().getInfo());

    addGoto(param, head);
    addGoto(sub, head);

    IfGoto ifStmt = makeIf((evl.expression.Expression) fta.traverse(obj.getCondition(), null), sub, exit);
    head.setEnd(ifStmt);

    return exit;
  }

  @Override
  protected BasicBlock visitIfStmt(IfStmt obj, BasicBlock param) {
    assert ( param != null );
    BasicBlock join = makeBb(obj.getInfo());
    for( IfOption opt : obj.getOption() ) {
      BasicBlock bbthen = visit(opt.getCode(), makeBb(opt.getCode().getInfo()));
      addGoto(bbthen, join);

      BasicBlock bbelse = makeBb(obj.getInfo());
      IfGoto ifStmt = makeIf((Expression) fta.traverse(opt.getCondition(), null), bbthen, bbelse);
      assert ( param.getEnd() == null );
      param.setEnd(ifStmt);

      param = bbelse;
    }
    BasicBlock lastOpt = visit(obj.getDefblock(), makeBb(obj.getDefblock().getInfo()));
    addGoto(param, lastOpt);
    addGoto(lastOpt, join);

    return join;
  }

  @Override
  protected BasicBlock visitCaseStmt(CaseStmt obj, BasicBlock param) {
    assert ( param != null );
    BasicBlock join = makeBb(obj.getInfo());

    CaseGoto co = new CaseGoto(obj.getInfo());
    co.setCondition((Expression) fta.traverse(obj.getCondition(), null));
    assert ( param.getEnd() == null );
    param.setEnd(co);

    for( CaseOpt itr : obj.getOption() ) {
      BasicBlock caseBb = visit(itr.getCode(), makeBb(itr.getCode().getInfo()));
      addGoto(caseBb, join);
      List<CaseOptEntry> optlist = new ArrayList<CaseOptEntry>(obj.getOption().size());
      for( fun.statement.CaseOptEntry opt : itr.getValue() ) {
        optlist.add((CaseOptEntry) fta.traverse(opt, null));
      }
      CaseGotoOpt cgo = new CaseGotoOpt(itr.getInfo(), optlist, caseBb);
      co.getOption().add(cgo);
    }
    BasicBlock otherBb = visit(obj.getOtherwise(), makeBb(obj.getOtherwise().getInfo()));
    addGoto(otherBb, join);
    co.setOtherwise(otherBb);

    return join;
  }

  @Override
  protected BasicBlock visitAssignment(Assignment obj, BasicBlock param) {
    param.getCode().add((evl.statement.normal.NormalStmt) fta.traverse(obj, null));
    return param;
  }

  @Override
  protected BasicBlock visitVarDef(VarDefStmt obj, BasicBlock param) {
    param.getCode().add((evl.statement.normal.NormalStmt) fta.traverse(obj, null));
    return param;
  }

  @Override
  protected BasicBlock visitCallStmt(CallStmt obj, BasicBlock param) {
    param.getCode().add((evl.statement.normal.NormalStmt) fta.traverse(obj, null));
    return param;
  }
}
