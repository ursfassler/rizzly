package cir.traverser;

import java.util.HashSet;

import cir.Traverser;
import cir.expression.ArrayValue;
import cir.expression.BinaryOp;
import cir.expression.BoolValue;
import cir.expression.Number;
import cir.expression.Reference;
import cir.expression.StringValue;
import cir.expression.UnaryOp;
import cir.expression.reference.RefCall;
import cir.expression.reference.RefHead;
import cir.expression.reference.RefIndex;
import cir.expression.reference.RefName;
import cir.function.Function;
import cir.function.FunctionImpl;
import cir.function.FunctionPrototype;
import cir.function.LibFunction;
import cir.library.CLibrary;
import cir.other.Constant;
import cir.other.FuncVariable;
import cir.other.Program;
import cir.other.StateVariable;
import cir.other.Variable;
import cir.statement.Assignment;
import cir.statement.Block;
import cir.statement.CallStmt;
import cir.statement.CaseEntry;
import cir.statement.CaseStmt;
import cir.statement.IfStmt;
import cir.statement.ReturnValue;
import cir.statement.ReturnVoid;
import cir.statement.VarDefStmt;
import cir.statement.WhileStmt;
import cir.type.ArrayType;
import cir.type.BooleanType;
import cir.type.EnumElement;
import cir.type.EnumType;
import cir.type.IntType;
import cir.type.NamedElement;
import cir.type.StringType;
import cir.type.StructType;
import cir.type.Type;
import cir.type.TypeAlias;
import cir.type.UnionType;
import cir.type.VoidType;

public class TypeReplacer<T> extends Traverser<Type, T> {

  @Override
  protected Type visitVariable(Variable obj, T param) {
    obj.setType(visit(obj.getType(), param));
    return null;
  }

  @Override
  protected Type visitFunction(Function obj, T param) {
    obj.setRetType(visit(obj.getRetType(), param));
    visitList(obj.getArgument(), param);
    super.visitFunction(obj, param);
    return null;
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
  protected Type visitCaseEntry(CaseEntry obj, T param) {
    return visit(obj.getCode(), param);
  }

  @Override
  protected Type visitConstant(Constant obj, T param) {
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
  protected Type visitFunctionImpl(FunctionImpl obj, T param) {
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Type visitFunctionPrototype(FunctionPrototype obj, T param) {
    return null;
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
  protected Type visitIf(IfStmt obj, T param) {
    visit(obj.getThenBlock(), param);
    visit(obj.getElseBlock(), param);
    return null;
  }

  @Override
  protected Type visitBlock(Block obj, T param) {
    visitList(obj.getStatement(), param);
    return null;
  }

  @Override
  protected Type visitProgram(Program obj, T param) {
    // visitList(obj.getConstant(), param);
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

  @Override
  protected Type visitStateVariable(StateVariable obj, T param) {
    obj.setType(visit(obj.getType(), param));
    return null;
  }

  @Override
  protected Type visitBinaryOp(BinaryOp obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitUnaryOp(UnaryOp obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitReference(Reference obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitLibFunction(LibFunction obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitCLibrary(CLibrary obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitTypeAlias(TypeAlias obj, T param) {
    obj.setRef(visit(obj.getRef(), param));
    return obj;
  }

  @Override
  protected Type visitVoidType(VoidType obj, T param) {
    return obj;
  }

  @Override
  protected Type visitBoolValue(BoolValue obj, T param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitEnumType(EnumType obj, T param) {
    return obj;
  }

  @Override
  protected Type visitStructType(StructType obj, T param) {
    visitList(obj.getElements(), param);
    return obj;
  }

  @Override
  protected Type visitUnionType(UnionType obj, T param) {
    visitList(obj.getElements(), param);
    return obj;
  }

  @Override
  protected Type visitBooleanType(BooleanType obj, T param) {
    return obj;
  }

  @Override
  protected Type visitStringType(StringType obj, T param) {
    return obj;
  }

  @Override
  protected Type visitArrayType(ArrayType obj, T param) {
    obj.setType(visit(obj.getType(), param));
    return obj;
  }

  @Override
  protected Type visitIntType(IntType obj, T param) {
    return obj;
  }
}
