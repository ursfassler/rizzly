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

abstract public class NullTraverser<R, P> extends Traverser<R, P> {

  abstract protected R doDefault(PirObject obj, P param);

  @Override
  protected R visitCaseEntry(CaseEntry obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBitAnd(BitAnd obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitWhileStmt(WhileStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitVarDefStmt(VarDefStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitConstant(Constant obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStructType(StructType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitArrayType(ArrayType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitUnsignedType(UnsignedType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitProgram(Program obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitRangeType(RangeType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitSignedType(SignedType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitTypeCast(TypeCast obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitTypeRef(TypeRef obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitPointerType(PointerType obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitDiv(Div obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitEqual(Equal obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitGreater(Greater obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitGreaterequal(Greaterequal obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitLess(Less obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitLessequal(Lessequal obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitMinus(Minus obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitMod(Mod obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitMul(Mul obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitNotequal(Notequal obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitBitOr(BitOr obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitPlus(Plus obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitShl(Shl obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitShr(Shr obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitNot(Not obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitUminus(Uminus obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitLogicAnd(LogicAnd obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitLogicOr(LogicOr obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitLogicXand(LogicXand obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitLogicXor(LogicXor obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncImplRet(FuncImplRet obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncProtoRet(FuncProtoRet obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncImplVoid(FuncImplVoid obj, P param) {
    return doDefault(obj, param);
  }

  @Override
  protected R visitFuncProtoVoid(FuncProtoVoid obj, P param) {
    return doDefault(obj, param);
  }
}
