package evl.traverser;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;
import common.NameFactory;

import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.IfGoto;
import evl.composition.ImplComposition;
import evl.expression.ArithmeticOp;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.Relation;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FunctionBase;
import evl.knowledge.KnowPath;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.statement.Assignment;
import evl.statement.Statement;
import evl.statement.VarDefInitStmt;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.Range;
import evl.variable.SsaVariable;
import evl.variable.Variable;

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
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    visitItr(obj.getInternalFunction(), param);
    visitItr(obj.getInputFunc(), param);
    visitItr(obj.getSubComCallback(), param);
    return null;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    if (obj instanceof FuncWithBody) {
      st.traverse(obj, null);
    }
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

}

class StmtTraverser extends ExprReplacer<List<Statement>> {
  private KnowledgeBase kb;
  private CallDetector cd = new CallDetector();

  public StmtTraverser(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public Type getType(Expression obj) {
    Type type = ExpressionTypeChecker.process(obj, kb);

    KnowPath ka = kb.getEntry(KnowPath.class);
    Designator path = ka.find(type);

    if (path == null) {
      assert (type instanceof Range); // should not be Unsigned (more like type != VoidType)
      kb.getRoot().add(type);
    }

    return type;
  }

  private SsaVariable extract(Expression obj, List<Statement> param) {
    ElementInfo info = obj.getInfo();
    SsaVariable var = new SsaVariable(info, NameFactory.getNew(), getType(obj));
    param.add(new VarDefInitStmt(info, var, obj));
    return var;
  }

  @Override
  protected Expression visitBasicBlock(BasicBlock obj, List<Statement> param) {
    // we do not check Phi statements since they can only reference variables

    List<Statement> sl = new ArrayList<Statement>();
    for (Statement stmt : obj.getCode()) {
      visit(stmt, sl);
      sl.add(stmt);
    }
    visit(obj.getEnd(), sl);
    
    obj.getCode().clear();
    obj.getCode().addAll(sl);

    
    return null;
  }

  @Override
  protected Expression visitAssignment(Assignment obj, List<Statement> param) {
    return super.visitAssignment(obj, param);
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
        SsaVariable var = extract(ret, param);
        ret = new Reference(itr.getInfo(), var);
      }
    }
    return ret;
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, List<Statement> param) {
    super.visitRefIndex(obj, param);
    if (cd.traverse(obj.getIndex(), null)) {
      SsaVariable var = extract(obj.getIndex(), param);
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
        SsaVariable var = extract(expr, param);
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
  protected Expression visitArithmeticOp(ArithmeticOp obj, List<Statement> param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));

    SsaVariable var = extract(obj, param);

    return new Reference(obj.getInfo(), var);
  }

  @Override
  protected Expression visitRelation(Relation obj, List<Statement> param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));
    SsaVariable var = extract(obj, param);
    return new Reference(obj.getInfo(), var);
  }

  @Override
  protected Expression visitIfGoto(IfGoto obj, List<Statement> param) {
    boolean doChange;
    if (obj.getCondition() instanceof Reference) {
      Reference ref = (Reference) obj.getCondition();
      doChange = !(ref.getLink() instanceof Variable);
    } else {
      doChange = true;
    }
    if (doChange) {
      SsaVariable var = extract(obj.getCondition(), param);
      obj.setCondition(new Reference(obj.getInfo(), var));
    } else {
      super.visitIfGoto(obj, param);
    }
    return null;

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