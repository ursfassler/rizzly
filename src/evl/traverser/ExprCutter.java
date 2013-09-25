package evl.traverser;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;
import common.NameFactory;

import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.composition.ImplComposition;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.binop.BinaryExp;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FunctionBase;
import evl.knowledge.KnowPath;
import evl.knowledge.KnowSimpleExpr;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.statement.bbend.CaseGoto;
import evl.statement.bbend.Goto;
import evl.statement.bbend.IfGoto;
import evl.statement.bbend.ReturnExpr;
import evl.statement.bbend.ReturnVoid;
import evl.statement.bbend.Unreachable;
import evl.statement.normal.Assignment;
import evl.statement.normal.CallStmt;
import evl.statement.normal.GetElementPtr;
import evl.statement.normal.LoadStmt;
import evl.statement.normal.NormalStmt;
import evl.statement.normal.StackMemoryAlloc;
import evl.statement.normal.StoreStmt;
import evl.statement.normal.TypeCast;
import evl.statement.normal.VarDefInitStmt;
import evl.statement.normal.VarDefStmt;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.Range;
import evl.variable.SsaVariable;

//TODO also split relation operands (only ref to var or constant)
//TODO check if everything is still ok
/**
 * Splits expression down to two operands per expression and maximal one function call.
 * 
 * @author urs
 * 
 */
public class ExprCutter extends NullTraverser<Void, Void> {

  private StmtTraverser st;

  public ExprCutter(KnowledgeBase kb) {
    super();
    this.st = new StmtTraverser(kb);
  }

  public static void process(Namespace classes, KnowledgeBase kb) {
    ExprCutter cutter = new ExprCutter(kb);
    cutter.traverse(classes, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    // throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    return null;
  }

  @Override
  protected Void visitItr(Iterable<? extends Evl> list, Void param) {
    // against comodification error
    ArrayList<Evl> old = new ArrayList<Evl>();
    for( Evl itr : list ) {
      old.add(itr);
    }
    return super.visitItr(old, param);
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitNamedList(NamedList<Named> obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    if( obj instanceof FuncWithBody ) {
      visit(( (FuncWithBody) obj ).getBody(), null);
    }
    return null;
  }

  @Override
  protected Void visitBasicBlockList(BasicBlockList obj, Void param) {
    for( BasicBlock bb : obj.getAllBbs() ) {
      st.traverse(bb, null);
    }
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    visitItr(obj.getInternalFunction(), param);
    visitItr(obj.getInputFunc(), param);
    visitItr(obj.getSubComCallback(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    return null;
  }
}

class StmtTraverser extends NullTraverser<Void, List<NormalStmt>> {

  private KnowledgeBase kb;
  private Cutter cutter;
  private CallDetector cd = new CallDetector();

  public StmtTraverser(KnowledgeBase kb) {
    super();
    this.kb = kb;
    cutter = new Cutter(kb);
  }

  @Override
  protected Void visitDefault(Evl obj, List<NormalStmt> param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, List<NormalStmt> param) {
    // we do not check Phi statements since they can only reference variables

    List<NormalStmt> sl = new ArrayList<NormalStmt>();
    for( NormalStmt stmt : obj.getCode() ) {
      visit(stmt, sl);
      sl.add(stmt);
    }
    visit(obj.getEnd(), sl);

    obj.getCode().clear();
    obj.getCode().addAll(sl);

    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, List<NormalStmt> param) {
    obj.setCall((Reference) cutter.traverse(obj.getCall(), param));
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, List<NormalStmt> param) {
    obj.setRight(cutter.traverse(obj.getRight(), param));
    visit(obj.getLeft(), param);

    boolean rs = KnowSimpleExpr.isSimple(obj.getRight());
    boolean ls = KnowSimpleExpr.isSimple(obj.getLeft());

    if( !rs & !ls ) {
      SsaVariable var = cutter.extract(obj.getRight(), param);
      obj.setRight(new Reference(new ElementInfo(), var));
    }

    return null;
  }

  @Override
  protected Void visitGetElementPtr(GetElementPtr obj, List<NormalStmt> param) {
    obj.setAddress((Reference) cutter.traverse(obj.getAddress(), param));
//    boolean ls = KnowSimpleExpr.isSimple(obj.getAddress());
    return null;
  }

  @Override
  protected Void visitLoadStmt(LoadStmt obj, List<NormalStmt> param) {
    obj.setAddress((Reference) cutter.traverse(obj.getAddress(), param));
    return null;
  }

  @Override
  protected Void visitStoreStmt(StoreStmt obj, List<NormalStmt> param) {
    obj.setAddress((Reference) cutter.traverse(obj.getAddress(), param));
    Expression expr = cutter.traverse(obj.getExpr(), param);
    if( !KnowSimpleExpr.isSimple(expr) ) {
      SsaVariable var = cutter.extract(expr, param);
      expr = new Reference(new ElementInfo(), var);
    }
    obj.setExpr(expr);
    return null;
  }

  @Override
  protected Void visitStackMemoryAlloc(StackMemoryAlloc obj, List<NormalStmt> param) {
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, List<NormalStmt> param) {
    if( !KnowSimpleExpr.isSimple(obj.getValue()) ) {
      SsaVariable var = cutter.extract(obj.getValue(), param);
      obj.setValue(new Reference(obj.getInfo(), var));
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, List<NormalStmt> param) {
    visitItr(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, List<NormalStmt> param) {
    if( !KnowSimpleExpr.isSimple(obj.getIndex()) ) {
      SsaVariable var = cutter.extract(obj.getIndex(), param);
      obj.setIndex(new Reference(obj.getInfo(), var));
    }
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, List<NormalStmt> param) {
    super.visitRefCall(obj, param);
    for( int i = 0; i < obj.getActualParameter().size(); i++ ) {
      Expression expr = obj.getActualParameter().get(i);
      if( cd.traverse(expr, null) ) {
        SsaVariable var = cutter.extract(expr, param);
        obj.getActualParameter().set(i, new Reference(obj.getInfo(), var));
      }
    }
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, List<NormalStmt> param) {
    return null;
  }

  @Override
  protected Void visitCaseGoto(CaseGoto obj, List<NormalStmt> param) {
    if( !KnowSimpleExpr.isSimple(obj.getCondition()) ) {
      SsaVariable var = cutter.extract(obj.getCondition(), param);
      obj.setCondition(new Reference(obj.getInfo(), var));
    } else {
    }
    return null;
  }

  @Override
  protected Void visitIfGoto(IfGoto obj, List<NormalStmt> param) {
    if( !KnowSimpleExpr.isSimple(obj.getCondition()) ) {
      SsaVariable var = cutter.extract(obj.getCondition(), param);
      obj.setCondition(new Reference(obj.getInfo(), var));
    } else {
    }
    return null;
  }

  @Override
  protected Void visitGoto(Goto obj, List<NormalStmt> param) {
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, List<NormalStmt> param) {
    return null;
  }

  @Override
  protected Void visitVarDefInitStmt(VarDefInitStmt obj, List<NormalStmt> param) {
    obj.setInit(cutter.traverse(obj.getInit(), param));
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, List<NormalStmt> param) {
    obj.setExpr(cutter.traverse(obj.getExpr(), param));
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, List<NormalStmt> param) {
    return null;
  }

  @Override
  protected Void visitUnreachable(Unreachable obj, List<NormalStmt> param) {
    return null;
  }
}


class Cutter extends ExprReplacer<List<NormalStmt>> {

  private KnowledgeBase kb;
  private CallDetector cd = new CallDetector();

  public Cutter(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  private Type getType(Expression obj) {
    Type type = ExpressionTypeChecker.process(obj, kb);

    KnowPath ka = kb.getEntry(KnowPath.class);
    Designator path = ka.find(type);

    if( path == null ) {
      assert ( type instanceof Range ); // should not be Unsigned (more like type != VoidType)
      kb.getRoot().add(type);
    }

    return type;
  }

  SsaVariable extract(Expression obj, List<NormalStmt> param) {
    ElementInfo info = obj.getInfo();
    Type type = getType(obj);
    SsaVariable var = new SsaVariable(info, NameFactory.getNew(), new TypeRef(obj.getInfo(), type));
    param.add(new VarDefInitStmt(info, var, obj));
    return var;
  }

  @Override
  protected Expression visitReference(Reference obj, List<NormalStmt> param) {
    List<RefItem> olist = new ArrayList<RefItem>(obj.getOffset());
    obj.getOffset().clear();
    Reference ret = obj;
    for( int i = 0; i < olist.size(); i++ ) {
      RefItem itr = olist.get(i);
      visit(itr, param);
      ret.getOffset().add(itr);
      if( ( i < olist.size() - 1 ) && ( itr instanceof RefCall ) ) {
        SsaVariable var = extract(ret, param);
        ret = new Reference(itr.getInfo(), var);
      }
    }
    return ret;
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, List<NormalStmt> param) {
    super.visitRefIndex(obj, param);
    if( cd.traverse(obj.getIndex(), null) ) {
      SsaVariable var = extract(obj.getIndex(), param);
      obj.setIndex(new Reference(obj.getInfo(), var));
    }
    return null;
  }

  @Override
  protected Expression visitRefCall(RefCall obj, List<NormalStmt> param) {
    super.visitRefCall(obj, param);
    for( int i = 0; i < obj.getActualParameter().size(); i++ ) {
      Expression expr = obj.getActualParameter().get(i);
      if( cd.traverse(expr, null) ) {
        SsaVariable var = extract(expr, param);
        obj.getActualParameter().set(i, new Reference(obj.getInfo(), var));
      }
    }
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, List<NormalStmt> param) {
    return super.visitRefName(obj, param);
  }

  @Override
  protected Expression visitBinaryExp(BinaryExp obj, List<NormalStmt> param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));

    SsaVariable var = extract(obj, param);// TODO needed?

    return new Reference(obj.getInfo(), var);
  }

}

class CallDetector extends NullTraverser<Boolean, Void> {

  @Override
  protected Boolean visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitReference(Reference obj, Void param) {
    for( RefItem itr : obj.getOffset() ) {
      if( visit(itr, null) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected Boolean visitRefCall(RefCall obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitRefName(RefName obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitRefIndex(RefIndex obj, Void param) {
    return visit(obj.getIndex(), null);
  }

  @Override
  protected Boolean visitNumber(Number obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitBoolValue(BoolValue obj, Void param) {
    return false;
  }
}
