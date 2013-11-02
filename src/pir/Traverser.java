package pir;

import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Expression;
import pir.expression.Number;
import pir.expression.StringValue;
import pir.expression.TypeCast;
import pir.expression.binop.ArithmeticExp;
import pir.expression.binop.BinaryExp;
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
import pir.expression.binop.Logical;
import pir.expression.binop.Minus;
import pir.expression.binop.Mod;
import pir.expression.binop.Mul;
import pir.expression.binop.Notequal;
import pir.expression.binop.Plus;
import pir.expression.binop.Relation;
import pir.expression.binop.Shl;
import pir.expression.binop.Shr;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefItem;
import pir.expression.reference.RefName;
import pir.expression.reference.Reference;
import pir.expression.unop.Not;
import pir.expression.unop.Uminus;
import pir.expression.unop.UnaryExp;
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
import pir.statement.CaseStmt;
import pir.statement.IfStmt;
import pir.statement.Return;
import pir.statement.ReturnExpr;
import pir.statement.ReturnVoid;
import pir.statement.Statement;
import pir.statement.VarDefStmt;
import pir.statement.WhileStmt;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.IntType;
import pir.type.NamedElemType;
import pir.type.NamedElement;
import pir.type.PointerType;
import pir.type.RangeType;
import pir.type.SignedType;
import pir.type.StringType;
import pir.type.StructType;
import pir.type.Type;
import pir.type.TypeRef;
import pir.type.UnionType;
import pir.type.UnsignedType;
import pir.type.VoidType;

abstract public class Traverser<R, P> {

  public R traverse(Pir obj, P param) {
    return visit(obj, param);
  }

  protected void visitList(Iterable<? extends Pir> list, P param) {
    for (Pir itr : list) {
      visit(itr, param);
    }
  }

  protected R visit(Pir obj, P param) {
    if (obj == null) {
      throw new RuntimeException("object is null");
    } else if (obj instanceof Program) {
      return visitProgram((Program) obj, param);
    } else if (obj instanceof Statement) {
      return visitStatement((Statement) obj, param);
    } else if (obj instanceof Expression) {
      return visitPExpression((Expression) obj, param);
    } else if (obj instanceof Type) {
      return visitType((Type) obj, param);
    } else if (obj instanceof Function) {
      return visitFunction((Function) obj, param);
    } else if (obj instanceof NamedElement) {
      return visitNamedElement((NamedElement) obj, param);
    } else if (obj instanceof RefItem) {
      return visitRefItem((RefItem) obj, param);
    } else if (obj instanceof Variable) {
      return visitVariable((Variable) obj, param);
    } else if (obj instanceof TypeRef) {
      return visitTypeRef((TypeRef) obj, param);
    } else if (obj instanceof CaseEntry) {
      return visitCaseEntry((CaseEntry) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitStatement(Statement obj, P param) {
    if (obj instanceof Block)
      return visitBlock((Block) obj, param);
    else if (obj instanceof Assignment)
      return visitAssignment((Assignment) obj, param);
    else if (obj instanceof CallStmt)
      return visitCallStmt((CallStmt) obj, param);
    else if (obj instanceof IfStmt)
      return visitIfStmt((IfStmt) obj, param);
    else if (obj instanceof Return)
      return visitReturn((Return) obj, param);
    else if (obj instanceof VarDefStmt)
      return visitVarDefStmt((VarDefStmt) obj, param);
    else if (obj instanceof WhileStmt)
      return visitWhileStmt((WhileStmt) obj, param);
    else if (obj instanceof CaseStmt)
      return visitCaseStmt((CaseStmt) obj, param);
    else
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
  }

  protected R visitFunction(Function obj, P param) {
    if (obj instanceof FuncProtoVoid) {
      return visitFuncProtoVoid((FuncProtoVoid) obj, param);
    } else if (obj instanceof FuncProtoRet) {
      return visitFuncProtoRet((FuncProtoRet) obj, param);
    } else if (obj instanceof FuncImplVoid) {
      return visitFuncImplVoid((FuncImplVoid) obj, param);
    } else if (obj instanceof FuncImplRet) {
      return visitFuncImplRet((FuncImplRet) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitRefItem(RefItem obj, P param) {
    if (obj instanceof RefName) {
      return visitRefName((RefName) obj, param);
    } else if (obj instanceof RefIndex) {
      return visitRefIndex((RefIndex) obj, param);
    } else if (obj instanceof RefCall) {
      return visitRefCall((RefCall) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitPExpression(Expression obj, P param) {
    if (obj instanceof Number) {
      return visitNumber((Number) obj, param);
    } else if (obj instanceof StringValue) {
      return visitStringValue((StringValue) obj, param);
    } else if (obj instanceof ArrayValue) {
      return visitArrayValue((ArrayValue) obj, param);
    } else if (obj instanceof BoolValue) {
      return visitBoolValue((BoolValue) obj, param);
    } else if (obj instanceof BinaryExp) {
      return visitBinaryExp((BinaryExp) obj, param);
    } else if (obj instanceof UnaryExp) {
      return visitUnaryExp((UnaryExp) obj, param);
    } else if (obj instanceof Reference) {
      return visitReference((Reference) obj, param);
    } else if (obj instanceof TypeCast) {
      return visitTypeCast((TypeCast) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitUnaryExp(UnaryExp obj, P param) {
    if (obj instanceof Not) {
      return visitNot((Not) obj, param);
    } else if (obj instanceof Uminus) {
      return visitUminus((Uminus) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitBinaryExp(BinaryExp obj, P param) {
    if (obj instanceof Relation) {
      return visitRelation((Relation) obj, param);
    } else if (obj instanceof ArithmeticExp) {
      return visitArithmetic((ArithmeticExp) obj, param);
    } else if (obj instanceof Logical) {
      return visitLogical((Logical) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitLogical(Logical obj, P param) {
    if (obj instanceof LogicAnd) {
      return visitLogicAnd((LogicAnd) obj, param);
    } else if (obj instanceof LogicOr) {
      return visitLogicOr((LogicOr) obj, param);
    } else if (obj instanceof LogicXand) {
      return visitLogicXand((LogicXand) obj, param);
    } else if (obj instanceof LogicXor) {
      return visitLogicXor((LogicXor) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitArithmetic(ArithmeticExp obj, P param) {
    if (obj instanceof Plus) {
      return visitPlus((Plus) obj, param);
    } else if (obj instanceof Minus) {
      return visitMinus((Minus) obj, param);
    } else if (obj instanceof Mul) {
      return visitMul((Mul) obj, param);
    } else if (obj instanceof Div) {
      return visitDiv((Div) obj, param);
    } else if (obj instanceof Mod) {
      return visitMod((Mod) obj, param);
    } else if (obj instanceof BitAnd) {
      return visitBitAnd((BitAnd) obj, param);
    } else if (obj instanceof BitOr) {
      return visitBitOr((BitOr) obj, param);
    } else if (obj instanceof Shl) {
      return visitShl((Shl) obj, param);
    } else if (obj instanceof Shr) {
      return visitShr((Shr) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitRelation(Relation obj, P param) {
    if (obj instanceof Equal) {
      return visitEqual((Equal) obj, param);
    } else if (obj instanceof Notequal) {
      return visitNotequal((Notequal) obj, param);
    } else if (obj instanceof Less) {
      return visitLess((Less) obj, param);
    } else if (obj instanceof Lessequal) {
      return visitLessequal((Lessequal) obj, param);
    } else if (obj instanceof Greater) {
      return visitGreater((Greater) obj, param);
    } else if (obj instanceof Greaterequal) {
      return visitGreaterequal((Greaterequal) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }

  }

  protected R visitVariable(Variable obj, P param) {
    if (obj instanceof FuncVariable) {
      return visitFuncVariable((FuncVariable) obj, param);
    } else if (obj instanceof StateVariable) {
      return visitStateVariable((StateVariable) obj, param);
    } else if (obj instanceof Constant) {
      return visitConstant((Constant) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitReturn(Return obj, P param) {
    if (obj instanceof ReturnExpr) {
      return visitReturnExpr((ReturnExpr) obj, param);
    } else if (obj instanceof ReturnVoid) {
      return visitReturnVoid((ReturnVoid) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitType(Type obj, P param) {
    if (obj instanceof RangeType) {
      return visitRangeType((RangeType) obj, param);
    } else if (obj instanceof IntType) {
      return visitIntType((IntType) obj, param);
    } else if (obj instanceof BooleanType) {
      return visitBooleanType((BooleanType) obj, param);
    } else if (obj instanceof NamedElemType) {
      return visitNamedElemType((NamedElemType) obj, param);
    } else if (obj instanceof VoidType) {
      return visitVoidType((VoidType) obj, param);
    } else if (obj instanceof ArrayType) {
      return visitArrayType((ArrayType) obj, param);
    } else if (obj instanceof StringType) {
      return visitStringType((StringType) obj, param);
    } else if (obj instanceof PointerType) {
      return visitPointerType((PointerType) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitIntType(IntType obj, P param) {
    if (obj instanceof UnsignedType) {
      return visitUnsignedType((UnsignedType) obj, param);
    } else if (obj instanceof SignedType) {
      return visitSignedType((SignedType) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected R visitNamedElemType(NamedElemType obj, P param) {
    if (obj instanceof StructType) {
      return visitStructType((StructType) obj, param);
    } else if (obj instanceof UnionType) {
      return visitUnionType((UnionType) obj, param);
    } else {
      throw new RuntimeException("Unknow object: " + obj.getClass().getSimpleName());
    }
  }

  protected abstract R visitReference(Reference obj, P param);

  protected abstract R visitCaseStmt(CaseStmt obj, P param);

  protected abstract R visitWhileStmt(WhileStmt obj, P param);

  protected abstract R visitVarDefStmt(VarDefStmt obj, P param);

  protected abstract R visitIfStmt(IfStmt obj, P param);

  protected abstract R visitBlock(Block obj, P param);

  protected abstract R visitCaseEntry(CaseEntry obj, P param);

  protected abstract R visitTypeRef(TypeRef obj, P param);

  protected abstract R visitTypeCast(TypeCast obj, P param);

  protected abstract R visitSignedType(SignedType obj, P param);

  protected abstract R visitRefCall(RefCall obj, P param);

  protected abstract R visitRefIndex(RefIndex obj, P param);

  protected abstract R visitRefName(RefName obj, P param);

  protected abstract R visitReturnVoid(ReturnVoid obj, P param);

  protected abstract R visitReturnExpr(ReturnExpr obj, P param);

  protected abstract R visitConstant(Constant obj, P param);

  protected abstract R visitStateVariable(StateVariable obj, P param);

  protected abstract R visitFuncVariable(FuncVariable obj, P param);

  protected abstract R visitNamedElement(NamedElement obj, P param);

  protected abstract R visitVoidType(VoidType obj, P param);

  protected abstract R visitBoolValue(BoolValue obj, P param);

  protected abstract R visitNumber(Number obj, P param);

  protected abstract R visitArrayValue(ArrayValue obj, P param);

  protected abstract R visitStringValue(StringValue obj, P param);

  protected abstract R visitFuncImplRet(FuncImplRet obj, P param);

  protected abstract R visitFuncProtoRet(FuncProtoRet obj, P param);

  protected abstract R visitFuncImplVoid(FuncImplVoid obj, P param);

  protected abstract R visitFuncProtoVoid(FuncProtoVoid obj, P param);

  protected abstract R visitCallStmt(CallStmt obj, P param);

  protected abstract R visitAssignment(Assignment obj, P param);

  protected abstract R visitStructType(StructType obj, P param);

  protected abstract R visitUnionType(UnionType obj, P param);

  protected abstract R visitBooleanType(BooleanType obj, P param);

  protected abstract R visitStringType(StringType obj, P param);

  protected abstract R visitArrayType(ArrayType obj, P param);

  protected abstract R visitUnsignedType(UnsignedType obj, P param);

  protected abstract R visitRangeType(RangeType obj, P param);

  protected abstract R visitProgram(Program obj, P param);

  protected abstract R visitPointerType(PointerType obj, P param);

  protected abstract R visitPlus(Plus obj, P param);

  protected abstract R visitMinus(Minus obj, P param);

  protected abstract R visitMul(Mul obj, P param);

  protected abstract R visitDiv(Div obj, P param);

  protected abstract R visitMod(Mod obj, P param);

  protected abstract R visitBitAnd(BitAnd obj, P param);

  protected abstract R visitBitOr(BitOr obj, P param);

  protected abstract R visitShl(Shl obj, P param);

  protected abstract R visitShr(Shr obj, P param);

  protected abstract R visitEqual(Equal obj, P param);

  protected abstract R visitNotequal(Notequal obj, P param);

  protected abstract R visitLess(Less obj, P param);

  protected abstract R visitLessequal(Lessequal obj, P param);

  protected abstract R visitGreater(Greater obj, P param);

  protected abstract R visitGreaterequal(Greaterequal obj, P param);

  protected abstract R visitNot(Not obj, P param);

  protected abstract R visitUminus(Uminus obj, P param);

  protected abstract R visitLogicAnd(LogicAnd obj, P param);

  protected abstract R visitLogicOr(LogicOr obj, P param);

  protected abstract R visitLogicXand(LogicXand obj, P param);

  protected abstract R visitLogicXor(LogicXor obj, P param);

}
