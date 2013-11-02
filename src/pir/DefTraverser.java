package pir;

import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.TypeCast;
import pir.expression.binop.BitAnd;
import pir.expression.binop.BitOr;
import pir.expression.binop.Div;
import pir.expression.binop.Equal;
import pir.expression.binop.Greater;
import pir.expression.binop.Greaterequal;
import pir.expression.binop.Less;
import pir.expression.binop.Lessequal;
import pir.expression.binop.LogicAnd;
import pir.expression.binop.LogicOr;
import pir.expression.binop.LogicXand;
import pir.expression.binop.LogicXor;
import pir.expression.binop.Minus;
import pir.expression.binop.Mod;
import pir.expression.binop.Mul;
import pir.expression.binop.Notequal;
import pir.expression.binop.Plus;
import pir.expression.binop.Shl;
import pir.expression.binop.Shr;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.expression.reference.Reference;
import pir.expression.unop.Not;
import pir.expression.unop.Uminus;
import pir.function.impl.FuncImplRet;
import pir.function.impl.FuncImplVoid;
import pir.function.impl.FuncProtoRet;
import pir.function.impl.FuncProtoVoid;
import pir.other.Constant;
import pir.other.FuncVariable;
import pir.other.Program;
import pir.other.StateVariable;
import pir.statement.Assignment;
import pir.statement.Block;
import pir.statement.CallStmt;
import pir.statement.CaseEntry;
import pir.statement.CaseStmt;
import pir.statement.IfStmt;
import pir.statement.ReturnExpr;
import pir.statement.ReturnVoid;
import pir.statement.VarDefStmt;
import pir.statement.WhileStmt;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.NamedElement;
import pir.type.PointerType;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.TypeRef;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

public class DefTraverser<R, P> extends Traverser<R, P> {

  @Override
  protected R visitProgram(Program obj, P param) {
    visitList(obj.getType(), param);
    visitList(obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected R visitUnsignedType(UnsignedType obj, P param) {
    return null;
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    visit(obj.getSrc(), param);
    visit(obj.getDst(), param);
    return null;
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return null;
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return null;
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    visit(obj.getIndex(), param);
    return null;
  }

  @Override
  protected R visitStructType(StructType obj, P param) {
    visitList(obj.getElements(), param);
    return null;
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    visitList(obj.getElements(), param);
    return null;
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return null;
  }

  @Override
  protected R visitConstant(Constant obj, P param) {
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return null;
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return null;
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    visitList(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return null;
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return null;
  }

  @Override
  protected R visitSignedType(SignedType obj, P param) {
    return null;
  }

  @Override
  protected R visitFuncImplRet(FuncImplRet obj, P param) {
    visitList(obj.getArgument(), param);
    visit(obj.getRetType(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncImplVoid(FuncImplVoid obj, P param) {
    visitList(obj.getArgument(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncProtoRet(FuncProtoRet obj, P param) {
    visitList(obj.getArgument(), param);
    visit(obj.getRetType(), param);
    return null;
  }

  @Override
  protected R visitFuncProtoVoid(FuncProtoVoid obj, P param) {
    visitList(obj.getArgument(), param);
    return null;
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    visit(obj.getCast(), param);
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitTypeRef(TypeRef obj, P param) {
    return null;
  }

  @Override
  protected R visitPointerType(PointerType obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitPlus(Plus obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitMul(Mul obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitDiv(Div obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitMod(Mod obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitBitAnd(BitAnd obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitBitOr(BitOr obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitShl(Shl obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitShr(Shr obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitEqual(Equal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitNotequal(Notequal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLess(Less obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLessequal(Lessequal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitGreater(Greater obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitGreaterequal(Greaterequal obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitNot(Not obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitUminus(Uminus obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitLogicAnd(LogicAnd obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLogicOr(LogicOr obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLogicXand(LogicXand obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitLogicXor(LogicXor obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    visitList(obj.getStatement(), param);
    return null;
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getThenBlock(), param);
    visit(obj.getElseBlock(), param);
    return null;
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    visitList(obj.getOffset(), param);
    return null;
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    visitList(obj.getParameter(), param);
    return null;
  }

  @Override
  protected R visitWhileStmt(WhileStmt obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getBlock(), param);
    return null;
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    visit(obj.getCondition(), param);
    visitList(obj.getEntries(), param);
    visit(obj.getOtherwise(), param);
    return null;
  }

  @Override
  protected R visitVarDefStmt(VarDefStmt obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitCaseEntry(CaseEntry obj, P param) {
    visit(obj.getCode(), param);
    return null;
  }

}
