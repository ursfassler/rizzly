/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package fun;

import java.util.HashMap;
import java.util.Map;

import common.Direction;

import fun.composition.Connection;
import fun.composition.ImplComposition;
import fun.expression.AnyValue;
import fun.expression.ArithmeticOp;
import fun.expression.BoolValue;
import fun.expression.NamedElementsValue;
import fun.expression.NamedValue;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.TupleValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.expression.reference.SimpleRef;
import fun.function.FuncFunction;
import fun.function.FuncProcedure;
import fun.function.FuncQuery;
import fun.function.FuncResponse;
import fun.function.FuncReturnNone;
import fun.function.FuncReturnTuple;
import fun.function.FuncReturnType;
import fun.function.FuncSignal;
import fun.function.FuncSlot;
import fun.function.template.DefaultValueTemplate;
import fun.hfsm.ImplHfsm;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.FunList;
import fun.other.ImplElementary;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.other.Template;
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
import fun.traverser.ReLinker;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.VoidType;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
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

public class Copy {
  public static <T extends Fun> T copy(T obj) {
    CopyFun copier = new CopyFun();
    T nobj = copier.copy(obj);
    ReLinker.process(nobj, copier.getCopied());
    return nobj;
  }
}

class CopyFun extends Traverser<Fun, Void> {
  final private Map<Fun, Fun> copied = new HashMap<Fun, Fun>();

  public Map<Fun, Fun> getCopied() {
    return copied;
  }

  @SuppressWarnings("unchecked")
  public <T extends Fun> T copy(T obj) {
    return (T) visit(obj, null);
  }

  public <T extends Fun> FunList<T> copy(FunList<T> obj) {
    FunList<T> ret = new FunList<T>();
    for (T itr : obj) {
      ret.add(copy(itr));
    }
    return ret;
  }

  @Override
  protected Fun visit(Fun obj, Void param) {
    Fun nobj = copied.get(obj);
    if (nobj == null) {
      nobj = super.visit(obj, param);
      copied.put(obj, nobj);
    }
    return nobj;
  }

  @Override
  protected Fun visitSimpleRef(SimpleRef obj, Void param) {
    return new SimpleRef(obj.getInfo(), obj.getLink());
  }

  @Override
  protected Fun visitReference(Reference obj, Void param) {
    Reference ret = new Reference(obj.getInfo(), obj.getLink());
    ret.getOffset().addAll(copy(obj.getOffset()));
    return ret;
  }

  @Override
  protected Fun visitRefName(RefName obj, Void param) {
    return new RefName(obj.getInfo(), obj.getName());
  }

  @Override
  protected Fun visitRefIndex(RefIndex obj, Void param) {
    return new RefIndex(obj.getInfo(), copy(obj.getIndex()));
  }

  @Override
  protected Fun visitRefCall(RefCall obj, Void param) {
    return new RefCall(obj.getInfo(), copy(obj.getActualParameter()));
  }

  @Override
  protected Fun visitRefTemplCall(RefTemplCall obj, Void param) {
    return new RefTemplCall(obj.getInfo(), copy(obj.getActualParameter()));
  }

  @Override
  protected Fun visitNumber(Number obj, Void param) {
    return new Number(obj.getInfo(), obj.getValue());
  }

  @Override
  protected Fun visitStateSimple(StateSimple obj, Void param) {
    StateSimple ret = new StateSimple(obj.getInfo(), obj.getName());

    ret.getItemList().addAll(copy(obj.getItemList()));
    ret.setEntryFunc(copy(obj.getEntryFunc()));
    ret.setExitFunc(copy(obj.getExitFunc()));

    return ret;
  }

  @Override
  protected Fun visitStateComposite(StateComposite obj, Void param) {
    StateComposite ret = new StateComposite(obj.getInfo(), obj.getName(), obj.getInitial());

    ret.getItemList().addAll(copy(obj.getItemList()));
    ret.setEntryFunc(copy(obj.getEntryFunc()));
    ret.setExitFunc(copy(obj.getExitFunc()));

    return ret;
  }

  @Override
  protected Fun visitNamespace(Namespace obj, Void param) {
    Namespace ret = new Namespace(obj.getInfo(), obj.getName());
    ret.addAll(copy(obj.getChildren()));
    return ret;
  }

  @Override
  protected Fun visitRizzlyFile(RizzlyFile obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitConstPrivate(ConstPrivate obj, Void param) {
    return new ConstPrivate(obj.getInfo(), obj.getName(), copy(obj.getType()), copy(obj.getDef()));
  }

  @Override
  protected Fun visitConstGlobal(ConstGlobal obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitFuncVariable(FuncVariable obj, Void param) {
    return new FuncVariable(obj.getInfo(), obj.getName(), copy(obj.getType()));
  }

  @Override
  protected Fun visitStateVariable(StateVariable obj, Void param) {
    return new StateVariable(obj.getInfo(), obj.getName(), copy(obj.getType()), copy(obj.getDef()));
  }

  @Override
  protected Fun visitBooleanType(BooleanType obj, Void param) {
    return new BooleanType();
  }

  @Override
  protected Fun visitVoidType(VoidType obj, Void param) {
    return new VoidType();
  }

  @Override
  protected Fun visitStringType(StringType obj, Void param) {
    return new StringType();
  }

  @Override
  protected Fun visitArray(Array obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitTemplateParameter(TemplateParameter obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitCompUse(CompUse obj, Void param) {
    return new CompUse(obj.getInfo(), obj.getName(), copy(obj.getType()));
  }

  @Override
  protected Fun visitImplHfsm(ImplHfsm obj, Void param) {
    ImplHfsm ret = new ImplHfsm(obj.getInfo(), obj.getName(), copy(obj.getTopstate()));

    ret.getIface().addAll(copy(obj.getIface()));

    return ret;
  }

  @Override
  protected Fun visitImplComposition(ImplComposition obj, Void param) {
    ImplComposition ret = new ImplComposition(obj.getInfo(), obj.getName());

    ret.getIface().addAll(copy(obj.getIface()));
    ret.getInstantiation().addAll(copy(obj.getInstantiation()));
    ret.getConnection().addAll(copy(obj.getConnection()));

    return ret;
  }

  @Override
  protected Fun visitImplElementary(ImplElementary obj, Void param) {
    ImplElementary ret = new ImplElementary(obj.getInfo(), obj.getName());

    ret.getIface().addAll(copy(obj.getIface()));
    ret.getDeclaration().addAll(copy(obj.getDeclaration()));
    ret.getInstantiation().addAll(copy(obj.getInstantiation()));
    ret.setEntryFunc(copy(obj.getEntryFunc()));
    ret.setExitFunc(copy(obj.getExitFunc()));

    return ret;
  }

  @Override
  protected Fun visitTransition(Transition obj, Void param) {
    Transition ret = new Transition(obj.getInfo());
    ret.setSrc(copy(obj.getSrc()));
    ret.setDst(copy(obj.getDst()));
    ret.setEvent(copy(obj.getEvent()));
    ret.setGuard(copy(obj.getGuard()));
    ret.getParam().addAll(copy(obj.getParam()));
    ret.setBody(copy(obj.getBody()));
    return ret;
  }

  @Override
  protected Fun visitConnection(Connection obj, Void param) {
    return new Connection(obj.getInfo(), copy(obj.getEndpoint(Direction.in)), copy(obj.getEndpoint(Direction.out)), obj.getType());
  }

  @Override
  protected Fun visitNamedElement(NamedElement obj, Void param) {
    return new NamedElement(obj.getInfo(), obj.getName(), copy(obj.getType()));
  }

  @Override
  protected Fun visitIntegerType(IntegerType obj, Void param) {
    return new IntegerType();
  }

  @Override
  protected Fun visitTypeType(TypeType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitDefaultValueTemplate(DefaultValueTemplate obj, Void param) {
    return new DefaultValueTemplate();
  }

  @Override
  protected Fun visitTypeTypeTemplate(TypeTypeTemplate obj, Void param) {
    return new TypeTypeTemplate();
  }

  @Override
  protected Fun visitArrayTemplate(ArrayTemplate obj, Void param) {
    return new ArrayTemplate();
  }

  @Override
  protected Fun visitNaturalType(NaturalType obj, Void param) {
    return new NaturalType();
  }

  @Override
  protected Fun visitAnyType(AnyType obj, Void param) {
    return new AnyType();
  }

  @Override
  protected Fun visitRecordType(RecordType obj, Void param) {
    RecordType ret = new RecordType(obj.getInfo(), obj.getName());
    ret.getElement().addAll(copy(obj.getElement()));
    return ret;
  }

  @Override
  protected Fun visitUnionType(UnionType obj, Void param) {
    UnionType ret = new UnionType(obj.getInfo(), obj.getName());
    ret.getElement().addAll(copy(obj.getElement()));
    return ret;
  }

  @Override
  protected Fun visitEnumType(EnumType obj, Void param) {
    EnumType type = new EnumType(obj.getInfo(), obj.getName());
    copied.put(obj, type);
    type.getElement().addAll(copy(obj.getElement()));
    return type;
  }

  @Override
  protected Fun visitEnumElement(EnumElement obj, Void param) {
    return new EnumElement(obj.getInfo(), obj.getName());
  }

  @Override
  protected Fun visitWhile(While obj, Void param) {
    return new While(obj.getInfo(), copy(obj.getCondition()), copy(obj.getBody()));
  }

  @Override
  protected Fun visitCaseStmt(CaseStmt obj, Void param) {
    return new CaseStmt(obj.getInfo(), copy(obj.getCondition()), copy(obj.getOption()), copy(obj.getOtherwise()));
  }

  @Override
  protected Fun visitCaseOptRange(CaseOptRange obj, Void param) {
    return new CaseOptRange(obj.getInfo(), copy(obj.getStart()), copy(obj.getEnd()));
  }

  @Override
  protected Fun visitCaseOptValue(CaseOptValue obj, Void param) {
    return new CaseOptValue(obj.getInfo(), copy(obj.getValue()));
  }

  @Override
  protected Fun visitCaseOpt(CaseOpt obj, Void param) {
    return new CaseOpt(obj.getInfo(), copy(obj.getValue()), copy(obj.getCode()));
  }

  @Override
  protected Fun visitVarDefStmt(VarDefStmt obj, Void param) {
    return new VarDefStmt(obj.getInfo(), copy(obj.getVariable()), copy(obj.getInitial()));
  }

  @Override
  protected Fun visitIfStmt(IfStmt obj, Void param) {
    IfStmt ret = new IfStmt(obj.getInfo());
    ret.getOption().addAll(copy(obj.getOption()));
    ret.setDefblock(copy(obj.getDefblock()));
    return ret;
  }

  @Override
  protected Fun visitIfOption(IfOption obj, Void param) {
    return new IfOption(obj.getInfo(), copy(obj.getCondition()), copy(obj.getCode()));
  }

  @Override
  protected Fun visitCallStmt(CallStmt obj, Void param) {
    return new CallStmt(obj.getInfo(), copy(obj.getCall()));
  }

  @Override
  protected Fun visitAssignment(Assignment obj, Void param) {
    return new Assignment(obj.getInfo(), copy(obj.getLeft()), copy(obj.getRight()));
  }

  @Override
  protected Fun visitReturnExpr(ReturnExpr obj, Void param) {
    return new ReturnExpr(obj.getInfo(), copy(obj.getExpr()));
  }

  @Override
  protected Fun visitReturnVoid(ReturnVoid obj, Void param) {
    return new ReturnVoid(obj.getInfo());
  }

  @Override
  protected Fun visitBlock(Block obj, Void param) {
    Block ret = new Block(obj.getInfo());
    ret.getStatements().addAll(copy(obj.getStatements()));
    return ret;
  }

  @Override
  protected Fun visitUnaryExpression(UnaryExpression obj, Void param) {
    return new UnaryExpression(obj.getInfo(), copy(obj.getExpr()), obj.getOp());
  }

  @Override
  protected Fun visitRelation(Relation obj, Void param) {
    return new Relation(obj.getInfo(), copy(obj.getLeft()), copy(obj.getRight()), obj.getOp());
  }

  @Override
  protected Fun visitArithmeticOp(ArithmeticOp obj, Void param) {
    return new ArithmeticOp(obj.getInfo(), copy(obj.getLeft()), copy(obj.getRight()), obj.getOp());
  }

  @Override
  protected Fun visitBoolValue(BoolValue obj, Void param) {
    return new BoolValue(obj.getInfo(), obj.isValue());
  }

  @Override
  protected Fun visitTupleValue(TupleValue obj, Void param) {
    return new TupleValue(obj.getInfo(), copy(obj.getValue()));
  }

  @Override
  protected Fun visitStringValue(StringValue obj, Void param) {
    return new StringValue(obj.getInfo(), obj.getValue());
  }

  @Override
  protected Fun visitRange(Range obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitRangeTemplate(RangeTemplate obj, Void param) {
    RangeTemplate range = new RangeTemplate();
    return range;
    // throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Fun visitAnyValue(AnyValue obj, Void param) {
    return new AnyValue(obj.getInfo());
  }

  @Override
  protected Fun visitNamedElementsValue(NamedElementsValue obj, Void param) {
    return new NamedElementsValue(obj.getInfo(), copy(obj.getValue()));
  }

  @Override
  protected Fun visitNamedValue(NamedValue obj, Void param) {
    return new NamedValue(obj.getInfo(), obj.getName(), copy(obj.getValue()));
  }

  @Override
  protected Fun visitTemplate(Template obj, Void param) {
    return new Template(obj.getInfo(), obj.getName(), copy(obj.getTempl()), copy(obj.getObject()));
  }

  @Override
  protected Fun visitFuncSlot(FuncSlot obj, Void param) {
    return new FuncSlot(obj.getInfo(), obj.getName(), copy(obj.getParam()), copy(obj.getRet()), copy(obj.getBody()));
  }

  @Override
  protected Fun visitFuncSignal(FuncSignal obj, Void param) {
    return new FuncSignal(obj.getInfo(), obj.getName(), copy(obj.getParam()), copy(obj.getRet()));
  }

  @Override
  protected Fun visitFuncQuery(FuncQuery obj, Void param) {
    return new FuncQuery(obj.getInfo(), obj.getName(), copy(obj.getParam()), copy(obj.getRet()));
  }

  @Override
  protected Fun visitFuncResponse(FuncResponse obj, Void param) {
    return new FuncResponse(obj.getInfo(), obj.getName(), copy(obj.getParam()), copy(obj.getRet()), copy(obj.getBody()));
  }

  @Override
  protected Fun visitFuncFunction(FuncFunction obj, Void param) {
    return new FuncFunction(obj.getInfo(), obj.getName(), copy(obj.getParam()), copy(obj.getRet()), copy(obj.getBody()));
  }

  @Override
  protected Fun visitFuncProcedure(FuncProcedure obj, Void param) {
    return new FuncProcedure(obj.getInfo(), obj.getName(), copy(obj.getParam()), copy(obj.getRet()), copy(obj.getBody()));
  }

  @Override
  protected Fun visitFuncReturnTuple(FuncReturnTuple obj, Void param) {
    return new FuncReturnTuple(obj.getInfo(), copy(obj.getParam()));
  }

  @Override
  protected Fun visitFuncReturnType(FuncReturnType obj, Void param) {
    return new FuncReturnType(obj.getInfo(), copy(obj.getType()));
  }

  @Override
  protected Fun visitFuncReturnNone(FuncReturnNone obj, Void param) {
    return new FuncReturnNone(obj.getInfo());
  }

  @Override
  protected Fun visitDummyLinkTarget(DummyLinkTarget obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }
}
