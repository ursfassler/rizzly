package fun;

import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.ArithmeticOp;
import fun.expression.ArrayValue;
import fun.expression.BoolValue;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefCompcall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.generator.ComponentGenerator;
import fun.generator.InterfaceGenerator;
import fun.generator.TypeGenerator;
import fun.hfsm.ImplHfsm;
import fun.hfsm.QueryItem;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.ImplElementary;
import fun.other.Interface;
import fun.other.NamedComponent;
import fun.other.NamedInterface;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.CallStmt;
import fun.statement.CaseOpt;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;
import fun.statement.CaseStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.type.NamedType;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.FunctionType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.TypeAlias;
import fun.type.base.VoidType;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.type.genfunc.Array;
import fun.type.genfunc.GenericArray;
import fun.type.genfunc.GenericRange;
import fun.type.genfunc.GenericTypeType;
import fun.type.genfunc.GenericUnsigned;
import fun.type.genfunc.Range;
import fun.type.genfunc.TypeType;
import fun.variable.CompUse;
import fun.variable.CompfuncParameter;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.IfaceUse;
import fun.variable.StateVariable;

abstract public class NullTraverser<R, P> extends Traverser<R, P> {

  protected abstract R visitDefault(Fun obj, P param);

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefName(RefName obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefIndex(RefIndex obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCompUse(CompUse obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplElementary(ImplElementary obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitInterface(Interface obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncPrivate(FuncPrivateVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRecordType(RecordType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumType(EnumType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitWhile(While obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitVarDef(VarDefStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitUnaryExpression(UnaryExpression obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRelation(Relation obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRefCompcall(RefCompcall obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFunctionType(FunctionType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeAlias(TypeAlias obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamespace(Namespace obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitGenericUnsigned(GenericUnsigned obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCompfuncParameter(CompfuncParameter obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIntegerType(IntegerType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNaturalType(NaturalType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitGenericTypeType(GenericTypeType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitGenericArray(GenericArray obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitArray(Array obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRizzlyFile(RizzlyFile obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIfaceUse(IfaceUse obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeType(TypeType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitAnyType(AnyType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOpt(CaseOpt obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitConnection(Connection obj, P param) {
    return visitDefault(obj, param);
  }

  protected R visitReferenceUnlinked(ReferenceUnlinked obj, P param) {
    return visitDefault(obj, param);
  }

  protected R visitReferenceLinked(ReferenceLinked obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitImplHfsm(ImplHfsm obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateSimple(StateSimple obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitStateComposite(StateComposite obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTransition(Transition obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitQueryItem(QueryItem obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedType(NamedType obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitTypeGenerator(TypeGenerator obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncPrivateRet(FuncPrivateRet obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncProtRet(FuncProtRet obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncProtVoid(FuncProtVoid obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncGlobal(FuncGlobal obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitIfOption(IfOption obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitFuncEntryExit(FuncEntryExit obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitInterfaceGenerator(InterfaceGenerator obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedInterface(NamedInterface obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitComponentGenerator(ComponentGenerator obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitNamedComponent(NamedComponent obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitRange(Range obj, P param) {
    return visitDefault(obj, param);
  }

  @Override
  protected R visitGenericRange(GenericRange obj, P param) {
    return visitDefault(obj, param);
  }

}
