package pir.traverser;

import java.util.HashSet;

import pir.Traverser;
import pir.expression.ArithmeticOp;
import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.Reference;
import pir.expression.Relation;
import pir.expression.StringValue;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefHead;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.function.FuncWithBody;
import pir.function.FuncWithRet;
import pir.function.Function;
import pir.function.impl.FuncImplRet;
import pir.function.impl.FuncImplVoid;
import pir.function.impl.FuncProtoRet;
import pir.function.impl.FuncProtoVoid;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.StateVariable;
import pir.other.Variable;
import pir.statement.Assignment;
import pir.statement.Block;
import pir.statement.CallStmt;
import pir.statement.CaseEntry;
import pir.statement.CaseOptRange;
import pir.statement.CaseOptValue;
import pir.statement.CaseStmt;
import pir.statement.IfStmt;
import pir.statement.IfStmtEntry;
import pir.statement.ReturnValue;
import pir.statement.ReturnVoid;
import pir.statement.VarDefStmt;
import pir.statement.WhileStmt;
import pir.type.EnumElement;
import pir.type.EnumType;
import pir.type.NamedElement;
import pir.type.StringType;
import pir.type.Type;

abstract public class TypeReplacer<T> extends Traverser<Type, T> {

  @Override
  protected Type visitVariable(Variable obj, T param) {
    obj.setType(visit(obj.getType(), param));
    return null;
  }

  @Override
  protected Type visitFunction(Function obj, T param) {
    if (obj instanceof FuncWithRet) {
      ((FuncWithRet) obj).setRetType(visit(((FuncWithRet) obj).getRetType(), param));
    }
    visitList(obj.getArgument(), param);
    if (obj instanceof FuncWithBody) {
      visit(((FuncWithBody) obj).getBody(), param);
    }
    return null;
  }

  @Override
  protected Type visitFuncImplVoid(FuncImplVoid obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitFuncImplRet(FuncImplRet obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitFuncProtoVoid(FuncProtoVoid obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitFuncProtoRet(FuncProtoRet obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitStringType(StringType obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefHead(RefHead obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefCall(RefCall obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefName(RefName obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitReturnVoid(ReturnVoid obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitReturnValue(ReturnValue obj, T param) {
    return null;
  }

  @Override
  protected Type visitEnumElement(EnumElement obj, T param) {
    obj.setType((EnumType) visit(obj.getType(), param));
    return null;
  }

  @Override
  protected Type visitIfStmtEntry(IfStmtEntry obj, T param) {
    return visit(obj.getCode(), param);
  }

  @Override
  protected Type visitCaseEntry(CaseEntry obj, T param) {
    return visit(obj.getCode(), param);
  }

  @Override
  protected Type visitCaseOptValue(CaseOptValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitCaseOptRange(CaseOptRange obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitConstant(Constant obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitStateVariable(StateVariable obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitFuncVariable(FuncVariable obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitNamedElement(NamedElement obj, T param) {
    obj.setType(visit(obj.getType(), param));
    return null;
  }

  @Override
  protected Type visitBoolValue(BoolValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitStringValue(StringValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitArrayValue(ArrayValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitNumber(Number obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitArithmeticOp(ArithmeticOp obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitUnaryExpr(UnaryExpr obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRelation(Relation obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitReference(Reference obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitVarDefStmt(VarDefStmt obj, T param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected Type visitCallStmt(CallStmt obj, T param) {
    return null;
  }

  @Override
  protected Type visitAssignment(Assignment obj, T param) {
    return null;
  }

  @Override
  protected Type visitCaseStmt(CaseStmt obj, T param) {
    visitList(obj.getEntries(), param);
    visit(obj.getOtherwise(), param);
    return null;
  }

  @Override
  protected Type visitWhile(WhileStmt obj, T param) {
    return visit(obj.getBlock(), param);
  }

  @Override
  protected Type visitIfStmt(IfStmt obj, T param) {
    visitList(obj.getOption(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected Type visitBlock(Block obj, T param) {
    visitList(obj.getStatement(), param);
    return null;
  }

  @Override
  protected Type visitProgram(Program obj, T param) {
    visitList(obj.getConstant(), param);
    visitList(obj.getFunction(), param);
    HashSet<Type> ns = new HashSet<Type>();
    for (Type type : obj.getType()) {
      Type nt = visit(type, param);
      assert (nt != null);
      ns.add(nt);
    }
    obj.getType().clear();
    obj.getType().addAll(ns);
    visitList(obj.getVariable(), param);
    return null;
  }
}
