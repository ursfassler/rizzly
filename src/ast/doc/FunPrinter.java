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

package ast.doc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import util.Writer;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.AsynchroniusConnection;
import ast.data.component.composition.Connection;
import ast.data.component.composition.EndpointRaw;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.component.composition.ImplComposition;
import ast.data.component.composition.Queue;
import ast.data.component.composition.SubCallbacks;
import ast.data.component.composition.SynchroniusConnection;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.ArrayValue;
import ast.data.expression.BoolValue;
import ast.data.expression.RecordValue;
import ast.data.expression.TypeCast;
import ast.data.expression.UnionValue;
import ast.data.expression.UnsafeUnionValue;
import ast.data.expression.binop.And;
import ast.data.expression.binop.BinaryExp;
import ast.data.expression.binop.Div;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.Greaterequal;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Lessequal;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Mod;
import ast.data.expression.binop.Mul;
import ast.data.expression.binop.Notequal;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.reference.DummyLinkTarget;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefIndex;
import ast.data.expression.reference.RefTemplCall;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.expression.unop.UnaryExp;
import ast.data.file.RizzlyFile;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSignal;
import ast.data.function.header.FuncSlot;
import ast.data.function.header.FuncSubHandlerEvent;
import ast.data.function.header.FuncSubHandlerQuery;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FuncReturnType;
import ast.data.function.template.DefaultValueTemplate;
import ast.data.raw.RawComponent;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CaseOpt;
import ast.data.statement.IfOption;
import ast.data.statement.MsgPush;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VarDefStmt;
import ast.data.template.Template;
import ast.data.type.base.EnumElement;
import ast.data.type.base.RangeType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.NamedElementType;
import ast.data.type.composed.UnionType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.out.AliasType;
import ast.data.type.out.SIntType;
import ast.data.type.out.UIntType;
import ast.data.type.template.ArrayTemplate;
import ast.data.type.template.RangeTemplate;
import ast.data.type.template.TypeType;
import ast.data.type.template.TypeTypeTemplate;
import ast.data.variable.Constant;
import ast.data.variable.DefVariable;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.traverser.NullTraverser;

import common.Designator;
import common.Direction;
import common.Metadata;

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
  protected Void visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  protected String getId(Named obj) {
    return "_" + Integer.toHexString(obj.hashCode());
  }

  protected String getId(Ast obj, Void name) {
    return "_" + Integer.toHexString(obj.hashCode());
  }

  protected Designator getObjPath(ast.data.expression.reference.BaseRef<Named> obj) {
    return new Designator();
  }

  private void list(Iterable<? extends Ast> list, String sep, Void param) {
    Iterator<? extends Ast> itr = list.iterator();
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

  private void visitListNl(List<? extends Ast> list, Void param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
      xw.nl();
    }
  }

  private void visitOptList(String before, String sep, String after, Iterable<? extends Ast> list) {
    Iterator<? extends Ast> itr = list.iterator();
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

  private void writeMeta(Ast obj) {
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
    for (Ast itr : obj.children) {
      visit(itr, param);
      xw.nl();
    }
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitCompUse(ast.data.component.composition.CompUse obj, Void param) {
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

  private void printStateBody(ast.data.component.hfsm.State obj) {
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
    if (!((obj.guard instanceof BoolValue) && (((ast.data.expression.BoolValue) obj.guard).value == true))) {
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
  protected Void visitStringType(ast.data.type.base.StringType obj, Void param) {
    xw.wa("<String>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitVoidType(ast.data.type.special.VoidType obj, Void param) {
    xw.wa("<∅>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitEnumType(ast.data.type.base.EnumType obj, Void param) {
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
  protected Void visitRecordType(ast.data.type.composed.RecordType obj, Void param) {
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
  protected Void visitArrayType(ast.data.type.base.ArrayType obj, Void param) {
    xw.wa(obj.name, getId(obj, param));
    return null;
  }

  @Override
  protected Void visitBooleanType(ast.data.type.base.BooleanType obj, Void param) {
    xw.wa(obj.name, getId(obj, param));
    return null;
  }

  @Override
  protected Void visitArrayTemplate(ArrayTemplate obj, Void param) {
    xw.wa(ArrayTemplate.NAME, getId(obj, param));
    return null;
  }

  @Override
  protected Void visitIntegerType(ast.data.type.special.IntegerType obj, Void param) {
    xw.wa("<Z>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitNaturalType(ast.data.type.special.NaturalType obj, Void param) {
    xw.wa("<N>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitAnyType(ast.data.type.special.AnyType obj, Void param) {
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
  protected Void visitCallStmt(ast.data.statement.CallStmt obj, Void param) {
    visit(obj.call, null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ast.data.statement.ReturnExpr obj, Void param) {
    xw.kw("return");
    xw.wr(" ");
    visit(obj.expr, null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(ast.data.statement.ReturnVoid obj, Void param) {
    xw.kw("return");
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitWhileStmt(ast.data.statement.WhileStmt obj, Void param) {
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
  protected Void visitForStmt(ast.data.statement.ForStmt obj, Void param) {
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
  protected Void visitIfStmt(ast.data.statement.IfStmt obj, Void param) {
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
  protected Void visitCaseStmt(ast.data.statement.CaseStmt obj, Void param) {
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
  protected Void visitCaseOptRange(ast.data.statement.CaseOptRange obj, Void param) {
    visit(obj.start, null);
    xw.wr("..");
    visit(obj.end, null);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(ast.data.statement.CaseOptValue obj, Void param) {
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
  protected Void visitAnyValue(ast.data.expression.AnyValue obj, Void param) {
    xw.kw("_");
    return null;
  }

  @Override
  protected Void visitBoolValue(ast.data.expression.BoolValue obj, Void param) {
    xw.kw(obj.value ? "True" : "False");
    return null;
  }

  @Override
  protected Void visitNumber(ast.data.expression.Number obj, Void param) {
    xw.wr(obj.value.toString());
    return null;
  }

  @Override
  protected Void visitStringValue(ast.data.expression.StringValue obj, Void param) {
    xw.wr("'");
    xw.wr(obj.value);
    xw.wr("'");
    return null;
  }

  @Override
  protected Void visitNamedValue(ast.data.expression.NamedValue obj, Void param) {
    xw.wr(obj.name);
    xw.wr(" := ");
    visit(obj.value, null);
    return null;
  }

  @Override
  protected Void visitNamedElementsValue(ast.data.expression.NamedElementsValue obj, Void param) {
    xw.wr("[");
    list(obj.value, ", ", param);
    xw.wr("]");
    return null;
  }

  @Override
  protected Void visitTupleValue(ast.data.expression.TupleValue obj, Void param) {
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

  private void wrRef(ast.data.expression.reference.BaseRef<Named> obj) {
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
  protected Void visitRefName(ast.data.expression.reference.RefName obj, Void param) {
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
      visit(((ast.data.variable.DefVariable) obj).def, param);
    }
    return null;
  }

  @Override
  protected Void visitConstant(ast.data.variable.Constant obj, Void param) {
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

  protected void visitOptList(String name, AstList<? extends Ast> items) {
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