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

package fun.doc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import util.Writer;

import common.Designator;
import common.Direction;
import common.Metadata;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.component.Component;
import evl.data.component.composition.AsynchroniusConnection;
import evl.data.component.composition.Connection;
import evl.data.component.composition.EndpointRaw;
import evl.data.component.composition.EndpointSelf;
import evl.data.component.composition.EndpointSub;
import evl.data.component.composition.ImplComposition;
import evl.data.component.composition.Queue;
import evl.data.component.composition.SubCallbacks;
import evl.data.component.composition.SynchroniusConnection;
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateSimple;
import evl.data.component.hfsm.Transition;
import evl.data.expression.ArrayValue;
import evl.data.expression.BoolValue;
import evl.data.expression.RecordValue;
import evl.data.expression.TypeCast;
import evl.data.expression.UnionValue;
import evl.data.expression.UnsafeUnionValue;
import evl.data.expression.binop.And;
import evl.data.expression.binop.BinaryExp;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Or;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import evl.data.expression.reference.DummyLinkTarget;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefTemplCall;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.expression.unop.BitNot;
import evl.data.expression.unop.LogicNot;
import evl.data.expression.unop.Not;
import evl.data.expression.unop.Uminus;
import evl.data.expression.unop.UnaryExp;
import evl.data.file.RizzlyFile;
import evl.data.function.Function;
import evl.data.function.FunctionProperty;
import evl.data.function.header.FuncFunction;
import evl.data.function.header.FuncProcedure;
import evl.data.function.header.FuncQuery;
import evl.data.function.header.FuncResponse;
import evl.data.function.header.FuncSignal;
import evl.data.function.header.FuncSlot;
import evl.data.function.header.FuncSubHandlerEvent;
import evl.data.function.header.FuncSubHandlerQuery;
import evl.data.function.ret.FuncReturnNone;
import evl.data.function.ret.FuncReturnTuple;
import evl.data.function.ret.FuncReturnType;
import evl.data.function.template.DefaultValueTemplate;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Block;
import evl.data.statement.CaseOpt;
import evl.data.statement.IfOption;
import evl.data.statement.MsgPush;
import evl.data.statement.VarDefInitStmt;
import evl.data.statement.VarDefStmt;
import evl.data.type.base.EnumElement;
import evl.data.type.base.RangeType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.NamedElementType;
import evl.data.type.composed.UnionType;
import evl.data.type.composed.UnsafeUnionType;
import evl.data.type.out.AliasType;
import evl.data.type.out.SIntType;
import evl.data.type.out.UIntType;
import evl.data.type.template.ArrayTemplate;
import evl.data.type.template.RangeTemplate;
import evl.data.type.template.TypeType;
import evl.data.type.template.TypeTypeTemplate;
import evl.data.variable.Constant;
import evl.data.variable.DefVariable;
import evl.data.variable.FuncVariable;
import evl.data.variable.StateVariable;
import evl.data.variable.Variable;
import evl.traverser.NullTraverser;
import fun.other.RawComponent;
import fun.other.RawComposition;
import fun.other.RawElementary;
import fun.other.RawHfsm;
import fun.other.Template;

/**
 * Prints formated FUN objects to a specific writer.
 *
 * @author urs
 *
 */
public class FunPrinter extends NullTraverser<Void, Void> {
  private Writer xw;

  public FunPrinter(Writer xw) {
    this.xw = xw;
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  protected String getId(Named obj) {
    return "_" + Integer.toHexString(obj.hashCode());
  }

  protected String getId(Evl obj, Void name) {
    return "_" + Integer.toHexString(obj.hashCode());
  }

  protected Designator getObjPath(evl.data.expression.reference.BaseRef<Named> obj) {
    return new Designator();
  }

  private void list(Iterable<? extends Evl> list, String sep, Void param) {
    Iterator<? extends Evl> itr = list.iterator();
    boolean first = true;
    while (itr.hasNext()) {
      if (first) {
        first = false;
      } else {
        xw.wr(sep);
      }
      visit(itr.next(), param);
    }
  }

  private void visitListNl(List<? extends Evl> list, Void param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
      xw.nl();
    }
  }

  private void visitOptList(String before, String sep, String after, Iterable<? extends Evl> list) {
    Iterator<? extends Evl> itr = list.iterator();
    if (itr.hasNext()) {
      xw.wr(before);
      boolean first = true;
      while (itr.hasNext()) {
        if (first) {
          first = false;
        } else {
          xw.wr(sep);
        }
        visit(itr.next(), null);
      }
      xw.wr(after);
    }
  }

  private void writeMeta(Evl obj) {
    ArrayList<Metadata> metadata = obj.getInfo().metadata;
    for (Metadata meta : metadata) {
      xw.wc("//" + meta.getKey() + " " + meta.getValue());
      xw.nl();
    }
  }

  private void visitImports(List<Designator> imports, Void param) {
    if (!imports.isEmpty()) {
      xw.kw("import");
      xw.nl();
      xw.incIndent();
      for (Designator ref : imports) {
        xw.wl(ref.toString(), ref.toString(), ref.toString());
        xw.wr(";");
        xw.nl();
      }
      xw.decIndent();
    }
  }

  private void printEntryExit(String name, Block func) {
    if (!func.statements.isEmpty()) {
      xw.kw(name);
      xw.nl();
      xw.incIndent();
      visit(func, null);
      xw.decIndent();
      xw.kw("end");
      xw.nl();
    }
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, Void param) {
    assert (param == null);
    writeMeta(obj);
    visitImports(obj.getImports(), null);
    visitList(obj.getObjects(), param);
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    xw.wa(obj.name, getId(obj));
    xw.nl();
    xw.incIndent();
    for (Evl itr : obj.children) {
      visit(itr, param);
      xw.nl();
    }
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitCompUse(evl.data.component.composition.CompUse obj, Void param) {
    xw.wa(obj.name, getId(obj));
    xw.wr(": ");
    visit(obj.compRef, null);
    xw.wr(";");
    xw.nl();
    writeMeta(obj);
    return null;
  }

  private void compHeader(RawComponent obj) {
    xw.kw("Component");
    xw.nl();
    xw.incIndent();
    writeMeta(obj);
    visitList(obj.getIface(), null);
    xw.decIndent();
    xw.kw("implementation ");
  }

  @Override
  protected Void visitRawElementary(RawElementary obj, Void param) {
    compHeader(obj);
    xw.kw("elementary");
    xw.nl();
    xw.incIndent();
    printEntryExit("entry", obj.getEntryFunc());
    printEntryExit("entry", obj.getExitFunc());
    visitListNl(obj.getDeclaration(), param);
    visitListNl(obj.getInstantiation(), param);

    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitRawComposition(RawComposition obj, Void param) {
    compHeader(obj);
    xw.kw("composition");
    xw.nl();
    xw.incIndent();

    visitListNl(obj.getInstantiation(), param);
    visitListNl(obj.getConnection(), param);

    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitConnection(Connection obj, Void param) {
    visit(obj.endpoint.get(Direction.in), null);
    if (obj instanceof SynchroniusConnection) {
      xw.wr(" -> ");
    } else {
      xw.wr(" >> ");
    }
    visit(obj.endpoint.get(Direction.out), null);
    xw.wr(";");
    xw.nl();
    writeMeta(obj);
    return null;
  }

  // ---- hfsm ----------------------------------------------------------------

  @Override
  protected Void visitTemplate(Template obj, Void param) {
    String id = getId(obj, param);
    xw.wa(obj.name, id);
    xw.wr("{");
    list(obj.getTempl(), "; ", param);
    xw.wr("}");
    xw.wr(" = ");
    visit(obj.getObject(), param);
    return null;
  }

  private void printStateBody(evl.data.component.hfsm.State obj) {
    xw.incIndent();

    printEntryExit("entry", obj.entryFunc.link.body);
    printEntryExit("exit", obj.exitFunc.link.body);
    visitListNl(obj.item, null);

    xw.decIndent();
    xw.kw("end");
    xw.nl();
    xw.sectionSeparator();
  }

  @Override
  protected Void visitRawHfsm(RawHfsm obj, Void param) {
    compHeader(obj);
    xw.kw("hfsm");
    xw.nl();
    xw.incIndent();

    visit(obj.getTopstate(), param);

    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, Void param) {
    xw.wa(obj.name, getId(obj));
    xw.kw(" ");
    xw.kw("state");
    xw.wr("(");
    visit(obj.initial, null);
    xw.wr(")");
    xw.nl();

    printStateBody(obj);
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, Void param) {
    xw.wa(obj.name, getId(obj));
    xw.kw(" ");
    xw.kw("state");
    xw.nl();

    printStateBody(obj);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    xw.kw("transition");
    xw.nl();
    xw.incIndent();

    visit(obj.src, null);
    xw.wr(" ");
    xw.kw("to");
    xw.wr(" ");
    visit(obj.dst, null);
    xw.wr(" ");
    xw.kw("by");
    xw.wr(" ");
    visit(obj.eventFunc, null);
    xw.wr("(");
    list(obj.param, "; ", null);
    xw.wr(")");
    if (!((obj.guard instanceof BoolValue) && (((evl.data.expression.BoolValue) obj.guard).value == true))) {
      xw.wr(" ");
      xw.kw("if");
      xw.wr(" ");
      visit(obj.guard, null);
    }
    if (obj.body.statements.isEmpty()) {
      xw.wr(";");
      xw.nl();
    } else {
      xw.wr(" ");
      xw.kw("do");
      xw.nl();
      xw.incIndent();
      visit(obj.body, null);
      xw.decIndent();
      xw.kw("end");
      xw.nl();
    }

    xw.decIndent();

    return null;
  }

  // ---- Type ----------------------------------------------------------------

  @Override
  protected Void visitRangeTemplate(RangeTemplate obj, Void param) {
    xw.wa("<R{*,*}>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitStringType(evl.data.type.base.StringType obj, Void param) {
    xw.wa("<String>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitVoidType(evl.data.type.special.VoidType obj, Void param) {
    xw.wa("<∅>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitEnumType(evl.data.type.base.EnumType obj, Void param) {
    xw.wa("Enum", getId(obj, param));
    xw.nl();
    xw.incIndent();
    visitList(obj.getElement(), param);
    xw.decIndent();
    xw.kw("end");
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, Void param) {
    return null;
  }

  @Override
  protected Void visitUnsafeUnionType(UnsafeUnionType obj, Void param) {
    writeNamedElementType(obj, "Union", null);
    return null;
  }

  @Override
  protected Void visitRecordType(evl.data.type.composed.RecordType obj, Void param) {
    writeNamedElementType(obj, "Record", null);
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Void param) {
    xw.wa(obj.name, getId(obj));
    xw.wr(" : ");
    visit(obj.typeref, null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  private void writeNamedElementType(NamedElementType obj, String typename, Void param) {
    xw.wa(typename, getId(obj, param));
    xw.nl();
    xw.incIndent();
    visitList(obj.element, null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
  }

  @Override
  protected Void visitRangeType(RangeType obj, Void param) {
    xw.wa("<" + obj.range.low.toString() + ".." + obj.range.high.toString() + ">", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitArrayType(evl.data.type.base.ArrayType obj, Void param) {
    xw.wa(obj.name, getId(obj, param));
    return null;
  }

  @Override
  protected Void visitBooleanType(evl.data.type.base.BooleanType obj, Void param) {
    xw.wa(obj.name, getId(obj, param));
    return null;
  }

  @Override
  protected Void visitArrayTemplate(ArrayTemplate obj, Void param) {
    xw.wa(ArrayTemplate.NAME, getId(obj, param));
    return null;
  }

  @Override
  protected Void visitIntegerType(evl.data.type.special.IntegerType obj, Void param) {
    xw.wa("<Z>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitNaturalType(evl.data.type.special.NaturalType obj, Void param) {
    xw.wa("<N>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitAnyType(evl.data.type.special.AnyType obj, Void param) {
    xw.wa("<*>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitTypeType(TypeType obj, Void param) {
    xw.wr(TypeTypeTemplate.NAME);
    xw.wr("(");
    visit(obj.getType(), null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitTypeTypeTemplate(TypeTypeTemplate obj, Void param) {
    xw.wa("<Type{*}>", getId(obj, param));
    return null;
  }

  // ---- Statement -----------------------------------------------------------

  @Override
  protected Void visitBlock(Block obj, Void param) {
    visitList(obj.statements, param);
    return null;
  }

  @Override
  protected Void visitAssignmentMulti(AssignmentMulti obj, Void param) {
    visitList(obj.left, null);
    xw.wr(" := ");
    visit(obj.right, null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    visitList(obj.variable, param);
    xw.wr(" = ");
    visit(obj.initial, param);
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitCallStmt(evl.data.statement.CallStmt obj, Void param) {
    visit(obj.call, null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(evl.data.statement.ReturnExpr obj, Void param) {
    xw.kw("return");
    xw.wr(" ");
    visit(obj.expr, null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(evl.data.statement.ReturnVoid obj, Void param) {
    xw.kw("return");
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitWhileStmt(evl.data.statement.WhileStmt obj, Void param) {
    xw.kw("while");
    xw.wr(" ");
    visit(obj.condition, null);
    xw.kw(" do");
    xw.nl();
    xw.incIndent();
    visit(obj.body, null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitForStmt(evl.data.statement.ForStmt obj, Void param) {
    xw.kw("for");
    xw.wr(" ");
    xw.wa(obj.iterator.name, getId(obj.iterator));
    xw.kw(" in ");
    visit(obj.iterator.type, param);
    xw.kw(" do");
    xw.nl();
    xw.incIndent();
    visit(obj.block, null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitIfStmt(evl.data.statement.IfStmt obj, Void param) {
    assert (!obj.option.isEmpty());

    boolean first = true;
    for (IfOption itr : obj.option) {
      if (first) {
        xw.kw("if");
        xw.wr(" ");
        first = false;
      } else {
        xw.kw("ef");
        xw.wr(" ");
      }
      visit(itr.condition, null);
      xw.wr(" ");
      xw.kw("then");
      xw.nl();
      xw.incIndent();
      visit(itr.code, null);
      xw.decIndent();
    }

    if (!obj.defblock.statements.isEmpty()) {
      xw.kw("else");
      xw.nl();
      xw.incIndent();
      visit(obj.defblock, null);
      xw.decIndent();
    }
    xw.kw("end");
    xw.nl();

    return null;
  }

  @Override
  protected Void visitCaseStmt(evl.data.statement.CaseStmt obj, Void param) {
    xw.kw("case");
    xw.wr(" ");
    visit(obj.condition, null);
    xw.wr(" ");
    xw.kw("of");
    xw.nl();
    xw.incIndent();
    visitList(obj.option, null);
    if (!obj.otherwise.statements.isEmpty()) {
      xw.kw("else");
      xw.nl();
      xw.incIndent();
      visit(obj.otherwise, null);
      xw.decIndent();
      xw.kw("end");
      xw.nl();
    }
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitCaseOpt(CaseOpt obj, Void param) {
    list(obj.value, ",", null);
    xw.wr(":");
    xw.nl();
    xw.incIndent();
    visit(obj.code, null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitCaseOptRange(evl.data.statement.CaseOptRange obj, Void param) {
    visit(obj.start, null);
    xw.wr("..");
    visit(obj.end, null);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(evl.data.statement.CaseOptValue obj, Void param) {
    visit(obj.value, null);
    return null;
  }

  // ---- Expression ----------------------------------------------------------

  @Override
  protected Void visitUnaryExp(UnaryExp obj, Void param) {
    xw.wr("(");
    xw.kw(obj.getOpName());
    xw.wr(" ");
    visit(obj.expr, null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitAnyValue(evl.data.expression.AnyValue obj, Void param) {
    xw.kw("_");
    return null;
  }

  @Override
  protected Void visitBoolValue(evl.data.expression.BoolValue obj, Void param) {
    xw.kw(obj.value ? "True" : "False");
    return null;
  }

  @Override
  protected Void visitNumber(evl.data.expression.Number obj, Void param) {
    xw.wr(obj.value.toString());
    return null;
  }

  @Override
  protected Void visitStringValue(evl.data.expression.StringValue obj, Void param) {
    xw.wr("'");
    xw.wr(obj.value);
    xw.wr("'");
    return null;
  }

  @Override
  protected Void visitNamedValue(evl.data.expression.NamedValue obj, Void param) {
    xw.wr(obj.name);
    xw.wr(" := ");
    visit(obj.value, null);
    return null;
  }

  @Override
  protected Void visitNamedElementsValue(evl.data.expression.NamedElementsValue obj, Void param) {
    xw.wr("[");
    list(obj.value, ", ", param);
    xw.wr("]");
    return null;
  }

  @Override
  protected Void visitTupleValue(evl.data.expression.TupleValue obj, Void param) {
    xw.wr("(");
    list(obj.value, ", ", null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitSimpleRef(SimpleRef obj, Void param) {
    wrRef(obj);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    wrRef(obj);
    visitList(obj.offset, null);
    return null;
  }

  private void wrRef(evl.data.expression.reference.BaseRef<Named> obj) {
    Designator path = getObjPath(obj);
    if (path == null) {
      path = new Designator(); // TODO: ok?
    }
    String hint = obj.link.toString();
    String name;
    if (obj.link instanceof Named) {
      name = obj.link.name;
    } else {
      name = "???";
    }
    if (obj.link instanceof DummyLinkTarget) {
      name = "\"" + name + "\"";
    }
    xw.wl(name, hint, path.toString(), getId(obj.link, null));
  }

  @Override
  protected Void visitRefName(evl.data.expression.reference.RefName obj, Void param) {
    xw.wr(".");
    xw.wr(obj.name);
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, Void param) {
    xw.wr("[");
    visit(obj.index, param);
    xw.wr("]");
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, Void param) {
    visit(obj.actualParameter, param);
    return null;
  }

  @Override
  protected Void visitRefTemplCall(RefTemplCall obj, Void param) {
    visitOptList("{", ", ", "}", obj.actualParameter); // FIXME it is a bit
    // hacky, but maybe
    // needed to make
    // output
    // nicer
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Void param) {
    xw.wa(obj.name, getId(obj));
    xw.wr(" : ");
    if (obj instanceof Constant) {
      xw.kw("const ");
    }
    visit(obj.type, param);
    if (obj instanceof DefVariable) {
      xw.wr(" = ");
      visit(((evl.data.variable.DefVariable) obj).def, param);
    }
    return null;
  }

  @Override
  protected Void visitConstant(evl.data.variable.Constant obj, Void param) {
    super.visitConstant(obj, param);
    xw.wr(";");
    xw.nl();
    return null;
  }

  private void printFuncImpl(Function obj) {
    xw.nl();
    xw.incIndent();
    visit(obj.body, null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
  }

  @Override
  protected Void visitFuncReturnTuple(FuncReturnTuple obj, Void param) {
    xw.wr(":");
    xw.wr("(");
    visitOptList("", "; ", "", obj.param);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitFuncReturnType(FuncReturnType obj, Void param) {
    xw.wr(":");
    visit(obj.type, param);
    return null;
  }

  @Override
  protected Void visitFuncReturnNone(FuncReturnNone obj, Void param) {
    return null;
  }

  private void printFunctionHeader(Function obj) {
    xw.wr("[");
    xw.wr(getId(obj, null));
    xw.wr("]");
    xw.wr("(");
    visitOptList("", "; ", "", obj.param);
    xw.wr(")");
    visit(obj.ret, null);
  }

  @Override
  protected Void visitFuncFunction(FuncFunction obj, Void param) {
    xw.wr("function");
    printFunctionHeader(obj);
    printFuncImpl(obj);
    return null;
  }

  @Override
  protected Void visitFuncProcedure(FuncProcedure obj, Void param) {
    xw.wr("procedure");
    printFunctionHeader(obj);
    printFuncImpl(obj);
    return null;
  }

  @Override
  protected Void visitFuncSlot(FuncSlot obj, Void param) {
    xw.wr("slot");
    printFunctionHeader(obj);
    printFuncImpl(obj);
    return null;
  }

  @Override
  protected Void visitFuncSignal(FuncSignal obj, Void param) {
    xw.wr("signal");
    printFunctionHeader(obj);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitFuncQuery(FuncQuery obj, Void param) {
    xw.wr("query");
    printFunctionHeader(obj);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitFuncResponse(FuncResponse obj, Void param) {
    xw.wr("response");
    printFunctionHeader(obj);
    printFuncImpl(obj);
    return null;
  }

  @Override
  protected Void visitDefaultValueTemplate(DefaultValueTemplate obj, Void param) {
    xw.wa(obj.getName(), getId(obj, param));
    return null;
  }

  // ////////////////////////////////////////////////////////

  protected void visitOptList(String name, EvlList<? extends Evl> items) {
    if (items.isEmpty()) {
      return;
    }
    xw.wr(name);
    xw.nl();
    xw.incIndent();
    visitList(items, null);
    xw.decIndent();
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Void param) {
    xw.kw("subcallback ");
    visit(obj.compUse, param);
    xw.nl();
    xw.incIndent();
    visitList(obj.func, param);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    xw.nl();
    return null;
  }

  @Override
  protected Void visitComponent(Component obj, Void param) {
    super.visitComponent(obj, param);

    visitListNl(obj.iface, param);
    visitList(obj.function, param);

    xw.nl();
    xw.kw("queue ");
    visit(obj.queue, param);
    xw.nl();

    xw.decIndent();
    xw.kw("end");
    xw.nl();
    xw.nl();
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    xw.kw("elementary ");
    xw.wa(obj.name, getId(obj));
    xw.nl();
    xw.nl();
    xw.incIndent();

    visitListNl(obj.component, param);
    visitListNl(obj.type, param);
    visitListNl(obj.constant, param);
    visitListNl(obj.variable, param);

    xw.kw("entry: ");
    visit(obj.entryFunc, param);
    xw.kw(";");
    xw.nl();
    xw.kw("exit: ");
    visit(obj.exitFunc, param);
    xw.kw(";");
    xw.nl();
    xw.nl();

    visitList(obj.subCallback, param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    xw.kw("implementation composition ");
    xw.wa(obj.name, getId(obj));
    xw.nl();
    xw.nl();
    xw.incIndent();

    visitOptList("component", obj.component);
    visitOptList("connection", obj.connection);

    return null;
  }

  private void writeConnection(Connection obj, String connector, Void param) {
    visit(obj.endpoint.get(Direction.in), param);
    xw.wr(connector);
    visit(obj.endpoint.get(Direction.out), param);
    xw.kw(";");
    xw.nl();
  }

  @Override
  protected Void visitSynchroniusConnection(SynchroniusConnection obj, Void param) {
    writeConnection(obj, " -> ", param);
    return null;
  }

  @Override
  protected Void visitAsynchroniusConnection(AsynchroniusConnection obj, Void param) {
    writeConnection(obj, " >> ", param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Void param) {
    xw.wa(obj.name, getId(obj));
    xw.kw(" ");
    super.visitFunction(obj, param);
    xw.kw("(");
    list(obj.param, "; ", param);
    xw.kw(")");
    visit(obj.ret, param);
    if (obj.property == FunctionProperty.External) {
      xw.kw(" extern");
    }
    if (obj.property == FunctionProperty.Public) {
      xw.kw(" public");
    }
    xw.nl();

    xw.incIndent();
    visit(obj.body, param);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitFuncSubHandlerQuery(FuncSubHandlerQuery obj, Void param) {
    xw.kw("subresponse");
    return null;
  }

  @Override
  protected Void visitFuncSubHandlerEvent(FuncSubHandlerEvent obj, Void param) {
    xw.kw("subslot");
    return null;
  }

  // ---- Type ----------------------------------------------------------------
  @Override
  protected Void visitUnionType(UnionType obj, Void param) {
    xw.kw("Union ");
    xw.wr(obj.name);
    xw.kw("(");
    visit(obj.tag, param);
    xw.kw(")");
    xw.wa(obj.name, getId(obj));
    xw.nl();
    xw.incIndent();
    visitListNl(obj.element, param);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    xw.nl();
    return null;
  }

  @Override
  protected Void visitAliasType(AliasType obj, Void param) {
    xw.wr(obj.name);
    xw.wa(obj.name, getId(obj));
    xw.kw(" = ");
    visit(obj.ref, param);
    xw.nl();
    return null;
  }

  @Override
  protected Void visitSIntType(SIntType obj, Void param) {
    xw.kw("sint" + obj.bytes * 8);
    xw.wa(obj.name, getId(obj));
    xw.kw(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitUIntType(UIntType obj, Void param) {
    xw.kw("uint" + obj.bytes * 8);
    xw.wa(obj.name, getId(obj));
    xw.kw(";");
    xw.nl();
    return null;
  }

  // ---- Statement -----------------------------------------------------------

  @Override
  protected Void visitAssignmentSingle(AssignmentSingle obj, Void param) {
    visit(obj.left, param);
    xw.kw(" := ");
    visit(obj.right, param);
    xw.kw(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Void param) {
    visit(obj.variable, param);
    xw.kw(";");
    xw.nl();
    return null;
  }

  // ---- Expression ----------------------------------------------------------
  @Override
  protected Void visitBinaryExp(BinaryExp obj, Void param) {
    xw.kw("(");
    visit(obj.left, param);
    xw.kw(" ");
    xw.wr(obj.getOpName());
    xw.kw(" ");
    visit(obj.right, param);
    xw.kw(")");
    return null;
  }

  @Override
  protected Void visitArrayValue(ArrayValue obj, Void param) {
    xw.kw("Array(");
    list(obj.value, ", ", param);
    xw.kw(")");
    return null;
  }

  @Override
  protected Void visitUnsafeUnionValue(UnsafeUnionValue obj, Void param) {
    xw.kw("(");
    visit(obj.contentValue, param);
    xw.kw(")");
    return null;
  }

  @Override
  protected Void visitUnionValue(UnionValue obj, Void param) {
    xw.kw("(");
    visit(obj.tagValue, param);
    xw.kw(" := ");
    visit(obj.contentValue, param);
    xw.kw(")");
    return null;
  }

  @Override
  protected Void visitRecordValue(RecordValue obj, Void param) {
    visit(obj.type, param);
    xw.kw("[");
    list(obj.value, ", ", param);
    xw.kw("]");
    return null;
  }

  @Override
  protected Void visitNot(Not obj, Void param) {
    xw.kw("not ");
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected Void visitLogicNot(LogicNot obj, Void param) {
    xw.kw("lnot ");
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected Void visitBitNot(BitNot obj, Void param) {
    xw.kw("bnot ");
    visit(obj.expr, param);
    return null;
  }

  @Override
  protected Void visitUminus(Uminus obj, Void param) {
    xw.kw("- ");
    visit(obj.expr, param);
    return null;
  }

  private void visitBinop(String op, BinaryExp obj, Void param) {
    xw.kw("(");
    visit(obj.left, param);
    xw.kw(" ");
    xw.wr(op);
    xw.kw(" ");
    visit(obj.right, param);
    xw.kw(")");
  }

  @Override
  protected Void visitAnd(And obj, Void param) {
    visitBinop("and", obj, param);
    return null;
  }

  @Override
  protected Void visitDiv(Div obj, Void param) {
    visitBinop("/", obj, param);
    return null;
  }

  @Override
  protected Void visitEqual(Equal obj, Void param) {
    visitBinop("=", obj, param);
    return null;
  }

  @Override
  protected Void visitGreater(Greater obj, Void param) {
    visitBinop(">", obj, param);
    return null;
  }

  @Override
  protected Void visitGreaterequal(Greaterequal obj, Void param) {
    visitBinop(">=", obj, param);
    return null;
  }

  @Override
  protected Void visitLess(Less obj, Void param) {
    visitBinop("<", obj, param);
    return null;
  }

  @Override
  protected Void visitLessequal(Lessequal obj, Void param) {
    visitBinop("<=", obj, param);
    return null;
  }

  @Override
  protected Void visitMinus(Minus obj, Void param) {
    visitBinop("-", obj, param);
    return null;
  }

  @Override
  protected Void visitMod(Mod obj, Void param) {
    visitBinop("mod", obj, param);
    return null;
  }

  @Override
  protected Void visitMul(Mul obj, Void param) {
    visitBinop("*", obj, param);
    return null;
  }

  @Override
  protected Void visitNotequal(Notequal obj, Void param) {
    visitBinop("<>", obj, param);
    return null;
  }

  @Override
  protected Void visitOr(Or obj, Void param) {
    visitBinop("or", obj, param);
    return null;
  }

  @Override
  protected Void visitPlus(Plus obj, Void param) {
    visitBinop("+", obj, param);
    return null;
  }

  @Override
  protected Void visitShl(Shl obj, Void param) {
    visitBinop("shl", obj, param);
    return null;
  }

  @Override
  protected Void visitShr(Shr obj, Void param) {
    visitBinop("shr", obj, param);
    return null;
  }

  @Override
  protected Void visitDefVariable(DefVariable obj, Void param) {
    xw.kw(" = ");
    visit(obj.def, param);
    super.visitDefVariable(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Void param) {
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Void param) {
    xw.kw(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    xw.kw("implementation hfsm ");
    xw.wa(obj.name, getId(obj));
    xw.nl();
    xw.nl();

    visit(obj.topstate, param);

    return null;
  }

  protected void printStateContent(State obj, Void param) {
    xw.kw("entry ");
    visit(obj.entryFunc, param);
    xw.kw(";");
    xw.nl();
    xw.kw("entry ");
    visit(obj.exitFunc, param);
    xw.kw(";");
    xw.nl();
    xw.nl();

    visitListNl(obj.item, param);
  }

  @Override
  protected Void visitEndpointRaw(EndpointRaw obj, Void param) {
    visit(obj.ref, param);
    return null;
  }

  @Override
  protected Void visitEndpointSelf(EndpointSelf obj, Void param) {
    visit(obj.funcRef, param);
    return null;
  }

  @Override
  protected Void visitEndpointSub(EndpointSub obj, Void param) {
    visit(obj.component, param);
    xw.kw(".");
    xw.wr(obj.function);
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, Void param) {
    visit(obj.cast, param);
    xw.kw("(");
    visit(obj.value, param);
    xw.kw(")");
    return null;
  }

  @Override
  protected Void visitMsgPush(MsgPush obj, Void param) {
    xw.kw("!push(");
    visit(obj.queue, param);
    xw.kw(",");
    visit(obj.func, param);
    xw.kw(",[");
    list(obj.data, ",", param);
    xw.kw("]);");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitQueue(Queue obj, Void param) {
    xw.wa(obj.name, getId(obj));
    xw.nl();
    return null;
  }
}
