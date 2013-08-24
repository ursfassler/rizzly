package fun.traverser.spezializer;

import java.math.BigInteger;
import java.util.List;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.ArithmeticOp;
import fun.expression.ArrayValue;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefCompcall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.impl.FuncGlobal;
import fun.generator.Generator;
import fun.generator.TypeGenerator;
import fun.knowledge.KnowledgeBase;
import fun.traverser.Memory;
import fun.type.NamedType;
import fun.variable.CompfuncParameter;
import fun.variable.ConstGlobal;
import fun.variable.FuncVariable;

public class ExprEvaluator extends NullTraverser<Expression, Memory> {
  private RefExecutor rex;

  public ExprEvaluator(KnowledgeBase kb) {
    super();
    rex = new RefExecutor(kb);
  }

  public static Expression evaluate(Expression obj, Memory mem, KnowledgeBase kb) {
    ExprEvaluator evaluator = new ExprEvaluator(kb);
    return evaluator.traverse(obj, mem);
  }

  private void visitExpList(List<Expression> expList, Memory param) {
    for (int i = 0; i < expList.size(); i++) {
      Expression expr = expList.get(i);
      expr = visit(expr, param);
      expList.set(i, expr);
    }
  }

  @Override
  protected Expression visitFuncVariable(FuncVariable obj, Memory param) {
    assert (param.contains(obj));
    return param.getInt(obj);
  }

  @Override
  protected Expression visitCompfuncParameter(CompfuncParameter obj, Memory param) {
    assert (param.contains(obj));
    return param.getInt(obj);
  }

  @Override
  protected Expression visitDefault(Fun obj, Memory param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitNumber(Number obj, Memory param) {
    return obj;
  }

  @Override
  protected Expression visitStringValue(StringValue obj, Memory param) {
    return obj;
  }

  @Override
  protected Expression visitGenerator(@SuppressWarnings("rawtypes") Generator obj, Memory param) {
    return obj;
  }

  @Override
  protected Expression visitNamedType(NamedType obj, Memory param) {
    // return obj.getType();
    return obj; // TODO ????
  }

  @Override
  protected Expression visitConstGlobal(ConstGlobal obj, Memory param) {
    return visit(obj.getDef(), new Memory()); // new memory because global constant need no context
  }

  @Override
  protected Expression visitReferenceLinked(ReferenceLinked obj, Memory param) {

    Fun item = obj.getLink();

    for (RefItem itr : obj.getOffset()) {
      item = rex.traverse(item, itr);
    }

    return visit(item, param);
  }

  @Override
  protected Expression visitReferenceUnlinked(ReferenceUnlinked obj, Memory param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitRefCall(RefCall obj, Memory param) {
    visitExpList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Expression visitRefCompcall(RefCompcall obj, Memory param) {
    visitExpList(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Expression visitRefName(RefName obj, Memory param) {
    return null;
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, Memory param) {
    obj.setIndex(visit(obj.getIndex(), param));
    return null;
  }

  private int getInt(ElementInfo info, BigInteger rval) {
    if( rval.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 ){
      RError.err(ErrorType.Error, info, "value to big: " + rval.toString() );
    } else if( rval.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0 ){
      RError.err(ErrorType.Error, info, "value to small: " + rval.toString() );
    }
    return rval.intValue();
  }

  @Override
  protected Expression visitArithmeticOp(ArithmeticOp obj, Memory param) {
    Expression left = visit(obj.getLeft(), param);
    Expression right = visit(obj.getRight(), param);

    if ((left instanceof Number) && (right instanceof Number)) {
      BigInteger lval = ((Number) left).getValue();
      BigInteger rval = ((Number) right).getValue();
      BigInteger res;

      switch (obj.getOp()) {
      case AND:
        res = lval.and(rval);
        break;
      case DIV:
        res = lval.divide(rval);
        break;
      case MINUS:
        res = lval.subtract(rval);
        break;
      case MOD:
        res = lval.mod(rval);
        break;
      case MUL:
        res = lval.multiply(rval);
        break;
      case OR:
        res = lval.or(rval);
        break;
      case PLUS:
        res = lval.add(rval);
        break;
      case SHL:
        res = lval.shiftLeft( getInt(obj.getInfo(), rval) );
        break;
      case SHR:
        res = lval.shiftRight( getInt(obj.getInfo(), rval) );
        break;
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj.getOp());
        return obj;
      }
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitRelation(Relation obj, Memory param) {
    Expression left = visit(obj.getLeft(), param);
    Expression right = visit(obj.getRight(), param);

    if ((left instanceof Number) && (right instanceof Number)) {
      BigInteger lval = ((Number) left).getValue();
      BigInteger rval = ((Number) right).getValue();
      boolean res;

      switch (obj.getOp()) {
      case EQUAL:
        res = lval.compareTo(rval) == 0;
        break;
      case GREATER:
        res = lval.compareTo(rval) > 0;
        break;
      case GREATER_EQUEAL:
        res = lval.compareTo(rval) >= 0;
        break;
      case LESS:
        res = lval.compareTo(rval) < 0;
        break;
      case LESS_EQUAL:
        res = lval.compareTo(rval) <= 0;
        break;
      case NOT_EQUAL:
        res = lval.compareTo(rval) != 0;
        break;
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj.getOp());
        return null;
      }
      return new BoolValue(obj.getInfo(), res);
    } else if ((left instanceof BoolValue) && (right instanceof BoolValue)) {
      boolean lval = ((BoolValue) left).isValue();
      boolean rval = ((BoolValue) right).isValue();
      boolean res;

      switch (obj.getOp()) {
      case EQUAL:
        res = lval == rval;
        break;
      case NOT_EQUAL:
        res = lval != rval;
        break;
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj.getOp());
        return obj;
      }
      return new BoolValue(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, Memory param) {
    visitExpList(obj.getValue(), param);
    return obj;
  }

  @Override
  protected Expression visitUnaryExpression(UnaryExpression obj, Memory param) {
    Expression expr = visit(obj.getExpr(), param);

    if ((expr instanceof Number) ) {
      BigInteger eval = ((Number) expr).getValue();
      BigInteger res;

      switch (obj.getOp()) {
      case MINUS:
        res = eval.negate();
        break;
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Operator not yet implemented: " + obj.getOp());
        return obj;
      }
      return new Number(obj.getInfo(), res);
    } else {
      return obj;
    }
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, Memory param) {
    throw new RuntimeException("not yet implemented");
  }

}

class RefExecutor extends NullTraverser<Expression, RefItem> {

  private KnowledgeBase kb;

  public RefExecutor(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Expression visitDefault(Fun obj, RefItem param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitFuncGlobal(FuncGlobal obj, RefItem param) {
    assert (param instanceof RefCall);
    return StmtExecutor.process(obj, ((RefCall) param).getActualParameter(), new Memory(), kb);
  }

  @Override
  protected Expression visitTypeGenerator(TypeGenerator obj, RefItem param) {
    assert (param instanceof RefCompcall);
    return Specializer.processType(obj, ((RefCompcall) param).getActualParameter(), kb);
  }

}
