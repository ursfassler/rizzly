package evl.traverser.typecheck;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.CaseOptRange;
import evl.cfg.CaseOptValue;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.composition.ImplComposition;
import evl.expression.ArithmeticOp;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.Relation;
import evl.expression.UnaryExpression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.Transition;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.While;
import evl.type.Type;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.Variable;

public class VarInitCheck extends NullTraverser<Void, Void> {

  public static void process(Namespace impls) {
    VarInitCheck check = new VarInitCheck();
    check.traverse(impls, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
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
    visitItr(obj.getInputFunc(), param);
    visitItr(obj.getInternalFunction(), param);
    visitItr(obj.getSubComCallback(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitItr(obj.getFunction(), param);
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    VarInitTraverser traverser = new VarInitTraverser();
    traverser.traverse(obj, null);
    return null;
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, Void param) {
    VarInitTraverser traverser = new VarInitTraverser();
    traverser.traverse(obj.getFunc(), null);
    return null;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    if (obj instanceof FuncWithBody) {
      VarInitTraverser traverser = new VarInitTraverser();
      traverser.traverse(obj, null);
    }
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    return null;
  }

  @Override
  protected Void visitInterface(Interface obj, Void param) {
    return null;
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    return null;
  }

}

class VarInitTraverser extends NullTraverser<Boolean, Set<Variable>> {
  private boolean isVoid;

  private void exprCheck(Expression right, boolean b, Set<Variable> param) {
    VarInitExprCheck check = new VarInitExprCheck();
    check.check(right, b, param);
  }

  @Override
  protected Boolean visitDefault(Evl obj, Set<Variable> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitFunctionBase(FunctionBase obj, Set<Variable> param) {
    assert (param == null);
    isVoid = !(obj instanceof FuncWithReturn);
    param = new HashSet<Variable>();
    for (Variable arg : obj.getParam()) {
      param.add(arg);
    }
    boolean doesReturn = visit(((FuncWithBody) obj).getBody(), param);
    if (doesReturn && !isVoid) {
      RError.err(ErrorType.Error, obj.getInfo(), "Missing return value");
    }
    return null;
  }

  @Override
  protected Boolean visitTransition(Transition obj, Set<Variable> param) {
    assert (param == null);
    isVoid = true;
    param = new HashSet<Variable>();
    for (Variable arg : obj.getParam()) {
      param.add(arg);
    }
    boolean doesReturn = visit(obj.getBody(), param);
    if (doesReturn && !isVoid) {
      RError.err(ErrorType.Error, obj.getInfo(), "Missing return value");
    }
    return null;
  }

  @Override
  protected Boolean visitBlock(Block obj, Set<Variable> param) {
    boolean doesReturn = true;
    for (Statement stmt : obj.getStatements()) {
      if (!doesReturn) {
        RError.err(ErrorType.Error, stmt.getInfo(), "Unreachable code");
      }
      doesReturn = visit(stmt, param);
    }
    return doesReturn;
  }

  @Override
  protected Boolean visitAssignment(Assignment obj, Set<Variable> param) {
    exprCheck(obj.getRight(), false, param);
    exprCheck(obj.getLeft(), true, param);
    return true;
  }

  @Override
  protected Boolean visitReturnExpr(ReturnExpr obj, Set<Variable> param) {
    if (isVoid) {
      RError.err(ErrorType.Error, obj.getInfo(), "Function does not return a value");
    }
    exprCheck(obj.getExpr(), false, param);
    return false;
  }

  @Override
  protected Boolean visitReturnVoid(ReturnVoid obj, Set<Variable> param) {
    if (!isVoid) {
      RError.err(ErrorType.Error, obj.getInfo(), "Missing return value");
    }
    return false;
  }

  private Set<Variable> intersection(Set<Set<Variable>> sets) {
    if (sets.isEmpty()) {
      return new HashSet<Variable>();
    } else if (sets.size() == 1) {
      return sets.iterator().next();
    } else {
      Set<Variable> ret = new HashSet<Variable>();
      Iterator<Set<Variable>> itr = sets.iterator();
      ret.addAll(itr.next());
      while (itr.hasNext()) {
        ret.retainAll(itr.next());
      }
      return ret;
    }
  }

  @Override
  protected Boolean visitIf(IfStmt obj, Set<Variable> param) {
    boolean doesReturn = false;
    Set<Set<Variable>> subinit = new HashSet<Set<Variable>>();

    {
      Set<Variable> optinit = new HashSet<Variable>(param);
      boolean subreturn = visit(obj.getDefblock(), optinit);
      if (subreturn) {
        doesReturn = true;
        subinit.add(optinit);
      }
    }

    for (IfOption opt : obj.getOption()) {
      Set<Variable> optinit = new HashSet<Variable>(param);
      exprCheck(opt.getCondition(), false, param);
      boolean subreturn = visit(opt.getCode(), optinit);

      if (subreturn) {
        doesReturn = true;
        subinit.add(optinit);
      }
    }

    Set<Variable> def = intersection(subinit);
    param.addAll(def);

    return doesReturn;
  }

  @Override
  protected Boolean visitCaseStmt(CaseStmt obj, Set<Variable> param) {
    boolean doesReturn = false;
    Set<Set<Variable>> subinit = new HashSet<Set<Variable>>();

    exprCheck(obj.getCondition(), false, param);

    {
      Set<Variable> optinit = new HashSet<Variable>(param);
      boolean subreturn = visit(obj.getOtherwise(), optinit);
      if (subreturn) {
        doesReturn = true;
        subinit.add(optinit);
      }
    }

    for (CaseOpt opt : obj.getOption()) {
      Set<Variable> optinit = new HashSet<Variable>(param);
      boolean subreturn = visit(opt, optinit);

      if (subreturn) {
        doesReturn = true;
        subinit.add(optinit);
      }
    }

    Set<Variable> def = union(subinit);
    Set<Variable> add = new HashSet<Variable>(def);
    add.removeAll(param);
    param.addAll(def);

    // FIXME make it sure
    for (Variable var : add) {
      RError.err(ErrorType.Warning, obj.getInfo(), "Not sure if variable is initialized at the end of case statement: " + var);
    }
    RError.err(ErrorType.Warning, obj.getInfo(), "Not sure if control flow returns from case statement");

    doesReturn = true;

    return doesReturn;
  }

  private Set<Variable> union(Set<Set<Variable>> subinit) {
    Set<Variable> ret = new HashSet<Variable>();
    for (Set<Variable> itr : subinit) {
      ret.addAll(itr);
    }
    return ret;
  }

  @Override
  protected Boolean visitCaseOpt(CaseOpt obj, Set<Variable> param) {
    visitItr(obj.getValue(), param);
    return visit(obj.getCode(), param);
  }

  @Override
  protected Boolean visitCaseOptRange(CaseOptRange obj, Set<Variable> param) {
    exprCheck(obj.getStart(), false, param);
    exprCheck(obj.getEnd(), false, param);
    return null;
  }

  @Override
  protected Boolean visitCaseOptValue(CaseOptValue obj, Set<Variable> param) {
    exprCheck(obj.getValue(), false, param);
    return null;
  }

  @Override
  protected Boolean visitCallStmt(CallStmt obj, Set<Variable> param) {
    exprCheck(obj.getCall(), false, param);
    return true;
  }

  @Override
  protected Boolean visitVarDef(VarDefStmt obj, Set<Variable> param) {
    visit(obj.getVariable(), param);
    return true;
  }

  @Override
  protected Boolean visitFuncVariable(FuncVariable obj, Set<Variable> param) {
    return true;
  }

  @Override
  protected Boolean visitWhile(While obj, Set<Variable> param) {
    exprCheck(obj.getCondition(), false, param);
    boolean subreturn = visit(obj.getBody(), new HashSet<Variable>(param));
    if (!subreturn) {
      // TODO check if condition is always true
    }
    return true;
  }

}

class VarInitExprData {
  final public Boolean write;
  final public Set<Variable> initialized;

  public VarInitExprData(Boolean write, Set<Variable> initialized) {
    super();
    this.write = write;
    this.initialized = initialized;
  }
}

class VarInitExprCheck extends NullTraverser<Void, VarInitExprData> {

  public void check(Expression expr, boolean write, Set<Variable> initialized) {
    traverse(expr, new VarInitExprData(write, initialized));
  }

  @Override
  protected Void visitDefault(Evl obj, VarInitExprData param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, VarInitExprData param) {
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, VarInitExprData param) {
    return null;
  }

  // TODO check local variables
  @Override
  protected Void visitReference(Reference obj, VarInitExprData param) {
    visitItr(obj.getOffset(), param);

    Named item = obj.getLink();

    if (item instanceof FuncVariable) {
      Variable var = (Variable) item;
      if (param.write) {
        param.initialized.add(var);
      } else {
        if (!param.initialized.contains(var)) {
          RError.err(ErrorType.Error, obj.getInfo(), "Variable not initialized: " + var);
        }
      }
    }
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, VarInitExprData param) {
    assert (!param.write);
    visitItr(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, VarInitExprData param) {
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, VarInitExprData param) {
    check(obj.getIndex(), false, param.initialized);
    return null;
  }

  @Override
  protected Void visitArithmeticOp(ArithmeticOp obj, VarInitExprData param) {
    assert (!param.write);
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected Void visitUnaryExpression(UnaryExpression obj, VarInitExprData param) {
    assert (!param.write);
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected Void visitRelation(Relation obj, VarInitExprData param) {
    assert (!param.write);
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

}
