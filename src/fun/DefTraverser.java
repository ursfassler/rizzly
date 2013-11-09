package fun;

import common.Direction;

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
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.hfsm.ImplHfsm;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.ImplElementary;
import fun.other.ListOfNamed;
import fun.other.Named;
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
import fun.statement.Statement;
import fun.statement.VarDefStmt;
import fun.statement.While;
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
import fun.type.composed.UnionSelector;
import fun.type.composed.UnionType;
import fun.type.template.Array;
import fun.type.template.ArrayTemplate;
import fun.type.template.Range;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeType;
import fun.type.template.TypeTypeTemplate;
import fun.variable.CompUse;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;

public class DefTraverser<R, P> extends Traverser<R, P> {

  protected <T extends Named> R visitList(ListOfNamed<T> list, P param) {
    visitItr(list.getList(), param);
    return null;
  }

  @Override
  protected R visitRizzlyFile(RizzlyFile obj, P param) {
    visitList(obj.getType(), param);
    visitList(obj.getComp(), param);
    visitList(obj.getConstant(), param);
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected R visitImplElementary(ImplElementary obj, P param) {
    visitList(obj.getTemplateParam(), param);

    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);

    visitList(obj.getConstant(), param);
    visitList(obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    return null;
  }

  @Override
  protected R visitImplComposition(ImplComposition obj, P param) {
    visitList(obj.getTemplateParam(), param);

    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);

    visitList(obj.getComponent(), param);
    visitItr(obj.getConnection(), param);
    return null;
  }

  @Override
  protected R visitImplHfsm(ImplHfsm obj, P param) {
    visitList(obj.getTemplateParam(), param);

    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);

    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected R visitNumber(Number obj, P param) {
    return null;
  }

  @Override
  protected R visitUnaryExpression(UnaryExpression obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitRelation(Relation obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitArithmeticOp(ArithmeticOp obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitWhile(While obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitVarDef(VarDefStmt obj, P param) {
    visit(obj.getVariable(), param);
    return null;
  }

  @Override
  protected R visitIfStmt(IfStmt obj, P param) {
    visitItr(obj.getOption(), param);
    visit(obj.getDefblock(), param);
    return null;
  }

  @Override
  protected R visitIfOption(IfOption obj, P param) {
    visit(obj.getCondition(), param);
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected R visitCallStmt(CallStmt obj, P param) {
    visit(obj.getCall(), param);
    return null;
  }

  @Override
  protected R visitAssignment(Assignment obj, P param) {
    visit(obj.getLeft(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected R visitBlock(Block obj, P param) {
    for (Statement stmt : obj.getStatements()) {
      visit(stmt, param);
    }
    return null;
  }

  @Override
  protected R visitUnionType(UnionType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    visit(obj.getSelector(), param);
    visitList(obj.getElement(), param);
    return null;
  }

  @Override
  protected R visitRecordType(RecordType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    visitList(obj.getElement(), param);
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
  protected R visitRefCompcall(RefTemplCall obj, P param) {
    visitItr(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected R visitRefCall(RefCall obj, P param) {
    visitItr(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected R visitArray(Array obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitBooleanType(BooleanType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitEnumType(EnumType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    visitItr(obj.getElement(), param);
    return null;
  }

  @Override
  protected R visitEnumElement(EnumElement obj, P param) {
    visit(obj.getType(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitReturnExpr(ReturnExpr obj, P param) {
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected R visitReturnVoid(ReturnVoid obj, P param) {
    return null;
  }

  @Override
  protected R visitVoidType(VoidType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitFunctionType(FunctionType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected R visitTypeAlias(TypeAlias obj, P param) {
    visitList(obj.getTemplateParam(), param);
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected R visitCompUse(CompUse obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitFuncVariable(FuncVariable obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitStateVariable(StateVariable obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitConstPrivate(ConstPrivate obj, P param) {
    visit(obj.getType(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitConstGlobal(ConstGlobal obj, P param) {
    visit(obj.getType(), param);
    visit(obj.getDef(), param);
    return null;
  }

  @Override
  protected R visitTemplateParameter(TemplateParameter obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitReference(Reference obj, P param) {
    visitItr(obj.getOffset(), param);
    return null;
  }

  @Override
  protected R visitNamespace(Namespace obj, P param) {
    visitList(obj, param);
    return null;
  }

  @Override
  protected R visitArrayTemplate(ArrayTemplate obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitIntegerType(IntegerType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitNaturalType(NaturalType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitTypeTypeTemplate(TypeTypeTemplate obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitNamedElement(NamedElement obj, P param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected R visitTypeType(TypeType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitAnyType(AnyType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitCaseStmt(CaseStmt obj, P param) {
    visit(obj.getCondition(), param);
    visitItr(obj.getOption(), param);
    visit(obj.getOtherwise(), param);
    return null;
  }

  @Override
  protected R visitCaseOpt(CaseOpt obj, P param) {
    visitItr(obj.getValue(), param);
    visit(obj.getCode(), param);
    return null;
  }

  @Override
  protected R visitCaseOptRange(CaseOptRange obj, P param) {
    visit(obj.getStart(), param);
    visit(obj.getEnd(), param);
    return null;
  }

  @Override
  protected R visitCaseOptValue(CaseOptValue obj, P param) {
    visit(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitBoolValue(BoolValue obj, P param) {
    return null;
  }

  @Override
  protected R visitConnection(Connection obj, P param) {
    visit(obj.getEndpoint(Direction.in), param);
    visit(obj.getEndpoint(Direction.out), param);
    return null;
  }

  @Override
  protected R visitStringValue(StringValue obj, P param) {
    return null;
  }

  @Override
  protected R visitStringType(StringType obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitArrayValue(ArrayValue obj, P param) {
    visitItr(obj.getValue(), param);
    return null;
  }

  @Override
  protected R visitStateSimple(StateSimple obj, P param) {
    visit(obj.getEntryFuncRef(), param);
    visit(obj.getExitFuncRef(), param);
    visitItr(obj.getVariable(), param);
    visitItr(obj.getItemList(), param);
    return null;
  }

  @Override
  protected R visitStateComposite(StateComposite obj, P param) {
    visitItr(obj.getVariable(), param);
    visitItr(obj.getItemList(), param);
    visit(obj.getInitial(), param);
    visit(obj.getEntryFuncRef(), param);
    visit(obj.getExitFuncRef(), param);
    return null;
  }

  @Override
  protected R visitTransition(Transition obj, P param) {
    visit(obj.getSrc(), param);
    visit(obj.getDst(), param);
    visit(obj.getEvent(), param);
    visitItr(obj.getParam(), param);
    visit(obj.getGuard(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncPrivateRet(FuncPrivateRet obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncPrivateVoid(FuncPrivateVoid obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncProtRet(FuncProtRet obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    return null;
  }

  @Override
  protected R visitFuncProtVoid(FuncProtVoid obj, P param) {
    visitItr(obj.getParam(), param);
    return null;
  }

  @Override
  protected R visitFuncGlobal(FuncGlobal obj, P param) {
    visitList(obj.getTemplateParam(), param);
    visitItr(obj.getParam(), param);
    visit(obj.getRet(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncPrivate(FuncPrivateVoid obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitFuncEntryExit(FuncEntryExit obj, P param) {
    visitItr(obj.getParam(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected R visitRange(Range obj, P param) {
    return null;
  }

  @Override
  protected R visitRangeTemplate(RangeTemplate obj, P param) {
    visitList(obj.getTemplateParam(), param);
    return null;
  }

  @Override
  protected R visitUnionSelector(UnionSelector obj, P param) {
    return null;
  }

}