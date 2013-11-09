package evl.traverser;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;
import common.NameFactory;

import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.TypeCast;
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
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.type.Type;
import evl.type.TypeRef;
import evl.variable.FuncVariable;

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
    for (Evl itr : list) {
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
    if (obj instanceof FuncWithBody) {
      visit(((FuncWithBody) obj).getBody(), null);
    }
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, Void param) {
    st.traverse(obj, null);
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    visitItr(obj.getFunction(), param);
    visitItr(obj.getSubCallback(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    return null;
  }
}

class StmtTraverser extends NullTraverser<Void, List<Statement>> {

  private KnowledgeBase kb;
  private Cutter cutter;
  private CallDetector cd = new CallDetector();

  public StmtTraverser(KnowledgeBase kb) {
    super();
    this.kb = kb;
    cutter = new Cutter(kb);
  }

  @Override
  protected Void visitDefault(Evl obj, List<Statement> param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitBlock(Block obj, List<Statement> param) {
    List<Statement> list = new ArrayList<Statement>();
    for (Statement stmt : obj.getStatements()) {
      visit(stmt, list);
      assert (!list.contains(stmt));
      list.add(stmt);
    }
    obj.getStatements().clear();
    obj.getStatements().addAll(list);
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, List<Statement> param) {
    obj.setCall((Reference) cutter.traverse(obj.getCall(), param));
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, List<Statement> param) {
    obj.setRight(cutter.traverse(obj.getRight(), param));
    visit(obj.getLeft(), param);

    boolean rs = KnowSimpleExpr.isSimple(obj.getRight());
    boolean ls = KnowSimpleExpr.isSimple(obj.getLeft());

    if (!rs & !ls) {
      FuncVariable var = cutter.extract(obj.getRight(), param);
      obj.setRight(new Reference(new ElementInfo(), var));
    }

    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, List<Statement> param) {
    if (!KnowSimpleExpr.isSimple(obj.getValue())) {
      FuncVariable var = cutter.extract(obj.getValue(), param);
      obj.setValue(new Reference(obj.getInfo(), var));
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, List<Statement> param) {
    visitItr(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, List<Statement> param) {
    if (!KnowSimpleExpr.isSimple(obj.getIndex())) {
      FuncVariable var = cutter.extract(obj.getIndex(), param);
      obj.setIndex(new Reference(obj.getInfo(), var));
    }
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, List<Statement> param) {
    super.visitRefCall(obj, param);
    for (int i = 0; i < obj.getActualParameter().size(); i++) {
      Expression expr = obj.getActualParameter().get(i);
      if (cd.traverse(expr, null)) {
        FuncVariable var = cutter.extract(expr, param);
        obj.getActualParameter().set(i, new Reference(obj.getInfo(), var));
      }
    }
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, List<Statement> param) {
    return null;
  }

  @Override
  protected Void visitWhileStmt(WhileStmt obj, List<Statement> param) {
    assert (false);
    // FIXME the following code is wrong. If we extract code from the condition, it has to go into all incoming edges of
    // the condition
    // => also in the body of the loop
    if (!KnowSimpleExpr.isSimple(obj.getCondition())) {
      FuncVariable var = cutter.extract(obj.getCondition(), param);
      obj.setCondition(new Reference(obj.getInfo(), var));
    }
    visit(obj.getBody(), null);
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, List<Statement> param) {
    for (IfOption opt : obj.getOption()) {
      if (!KnowSimpleExpr.isSimple(opt.getCondition())) {
        FuncVariable var = cutter.extract(opt.getCondition(), param);
        opt.setCondition(new Reference(obj.getInfo(), var));
      }
    }
    for (IfOption opt : obj.getOption()) {
      visit(opt.getCode(), null);
    }
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, List<Statement> param) {
    if (!KnowSimpleExpr.isSimple(obj.getCondition())) {
      FuncVariable var = cutter.extract(obj.getCondition(), param);
      obj.setCondition(new Reference(obj.getInfo(), var));
    }
    for (CaseOpt opt : obj.getOption()) {
      visitItr(opt.getValue(), param);
      visit(opt.getCode(), null);
    }
    visit(obj.getOtherwise(), null);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, List<Statement> param) {
    assert (KnowSimpleExpr.isSimple(obj.getValue()));
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, List<Statement> param) {
    assert (KnowSimpleExpr.isSimple(obj.getStart()));
    assert (KnowSimpleExpr.isSimple(obj.getEnd()));
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, List<Statement> param) {
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, List<Statement> param) {
    obj.setExpr(cutter.traverse(obj.getExpr(), param));
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, List<Statement> param) {
    return null;
  }

}

class Cutter extends ExprReplacer<List<Statement>> {

  private KnowledgeBase kb;
  private KnowType kt;
  private CallDetector cd = new CallDetector();

  public Cutter(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kt = kb.getEntry(KnowType.class);
  }

  private Type getType(Expression obj) {
    Type type = kt.get(obj);

    KnowPath ka = kb.getEntry(KnowPath.class);
    assert (ka.find(type) != null);

    return type;
  }

  FuncVariable extract(Expression obj, List<Statement> param) {
    ElementInfo info = obj.getInfo();
    Type type = getType(obj);
    FuncVariable var = new FuncVariable(info, NameFactory.getNew(), new TypeRef(obj.getInfo(), type));
    param.add(new VarDefStmt(info, var));
    param.add(new Assignment(info, new Reference(info, var), obj));
    return var;
  }

  @Override
  protected Expression visitReference(Reference obj, List<Statement> param) {
    List<RefItem> olist = new ArrayList<RefItem>(obj.getOffset());
    obj.getOffset().clear();
    Reference ret = obj;
    for (int i = 0; i < olist.size(); i++) {
      RefItem itr = olist.get(i);
      visit(itr, param);
      ret.getOffset().add(itr);
      if ((i < olist.size() - 1) && (itr instanceof RefCall)) {
        FuncVariable var = extract(ret, param);
        ret = new Reference(itr.getInfo(), var);
      }
    }
    return ret;
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, List<Statement> param) {
    super.visitRefIndex(obj, param);
    if (cd.traverse(obj.getIndex(), null)) {
      FuncVariable var = extract(obj.getIndex(), param);
      obj.setIndex(new Reference(obj.getInfo(), var));
    }
    return null;
  }

  @Override
  protected Expression visitRefCall(RefCall obj, List<Statement> param) {
    super.visitRefCall(obj, param);
    for (int i = 0; i < obj.getActualParameter().size(); i++) {
      Expression expr = obj.getActualParameter().get(i);
      if (cd.traverse(expr, null)) {
        FuncVariable var = extract(expr, param);
        obj.getActualParameter().set(i, new Reference(obj.getInfo(), var));
      }
    }
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, List<Statement> param) {
    return super.visitRefName(obj, param);
  }

  @Override
  protected Expression visitBinaryExp(BinaryExp obj, List<Statement> param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));

    FuncVariable var = extract(obj, param);// TODO needed?

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
    for (RefItem itr : obj.getOffset()) {
      if (visit(itr, null)) {
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
