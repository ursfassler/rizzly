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

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.NullTraverser;
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
import fun.expression.reference.BaseRef;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.expression.reference.SimpleRef;
import fun.function.FuncFunction;
import fun.function.FuncHeader;
import fun.function.FuncImpl;
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
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.CompImpl;
import fun.other.ImplElementary;
import fun.other.Named;
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
import fun.statement.ForStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.VoidType;
import fun.type.composed.NamedElement;
import fun.type.composed.NamedElementType;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.type.template.Array;
import fun.type.template.ArrayTemplate;
import fun.type.template.Range;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeType;
import fun.type.template.TypeTypeTemplate;
import fun.variable.CompUse;
import fun.variable.Constant;
import fun.variable.DefVariable;
import fun.variable.Variable;

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
  protected Void visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  protected String getId(Named obj) {
    return "_" + Integer.toHexString(obj.hashCode());
  }

  protected String getId(Fun obj, Void name) {
    return "_" + Integer.toHexString(obj.hashCode());
  }

  protected Designator getObjPath(BaseRef obj) {
    return new Designator();
  }

  private void list(Iterable<? extends Fun> list, String sep, Void param) {
    Iterator<? extends Fun> itr = list.iterator();
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

  private void visitListNl(List<? extends Fun> list, Void param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
      xw.nl();
    }
  }

  private void visitOptList(String before, String sep, String after, Iterable<? extends Fun> list) {
    Iterator<? extends Fun> itr = list.iterator();
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

  private void writeMeta(Fun obj) {
    ArrayList<Metadata> metadata = obj.getInfo().getMetadata();
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
    if (!func.getStatements().isEmpty()) {
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
    xw.wa(obj.getName(), getId(obj));
    xw.nl();
    xw.incIndent();
    for (Fun itr : obj.getChildren()) {
      visit(itr, param);
      xw.nl();
    }
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(": ");
    visit(obj.getType(), null);
    xw.wr(";");
    xw.nl();
    writeMeta(obj);
    return null;
  }

  private void compHeader(CompImpl obj) {
    xw.kw("Component");
    xw.nl();
    xw.incIndent();
    writeMeta(obj);
    visitList(obj.getIface(), null);
    xw.decIndent();
    xw.kw("implementation ");
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
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
  protected Void visitImplComposition(ImplComposition obj, Void param) {
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
    visit(obj.getEndpoint(Direction.in), null);
    switch (obj.getType()) {
      case sync:
        xw.wr(" -> ");
        break;
      case async:
        xw.wr(" >> ");
        break;
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Not yet implemented connection type: " + obj.getType());
    }
    visit(obj.getEndpoint(Direction.out), null);
    xw.wr(";");
    xw.nl();
    writeMeta(obj);
    return null;
  }

  // ---- hfsm ----------------------------------------------------------------

  @Override
  protected Void visitTemplate(Template obj, Void param) {
    String id = getId(obj, param);
    xw.wa(obj.getName(), id);
    xw.wr("{");
    list(obj.getTempl(), "; ", param);
    xw.wr("}");
    xw.wr(" = ");
    visit(obj.getObject(), param);
    return null;
  }

  private void printStateBody(State obj) {
    xw.incIndent();

    printEntryExit("entry", obj.getEntryFunc());
    printEntryExit("exit", obj.getExitFunc());
    visitListNl(obj.getItemList(), null);

    xw.decIndent();
    xw.kw("end");
    xw.nl();
    xw.sectionSeparator();
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
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
    xw.kw("state");
    xw.wr("(");
    visit(obj.getInitial(), null);
    xw.wr(")");
    xw.nl();

    printStateBody(obj);
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, Void param) {
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

    visit(obj.getSrc(), null);
    xw.wr(" ");
    xw.kw("to");
    xw.wr(" ");
    visit(obj.getDst(), null);
    xw.wr(" ");
    xw.kw("by");
    xw.wr(" ");
    visit(obj.getEvent(), null);
    xw.wr("(");
    list(obj.getParam(), "; ", null);
    xw.wr(")");
    if (!((obj.getGuard() instanceof BoolValue) && (((BoolValue) obj.getGuard()).isValue() == true))) {
      xw.wr(" ");
      xw.kw("if");
      xw.wr(" ");
      visit(obj.getGuard(), null);
    }
    if (obj.getBody().getStatements().isEmpty()) {
      xw.wr(";");
      xw.nl();
    } else {
      xw.wr(" ");
      xw.kw("do");
      xw.nl();
      xw.incIndent();
      visit(obj.getBody(), null);
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
  protected Void visitStringType(StringType obj, Void param) {
    xw.wa("<String>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, Void param) {
    xw.wa("<∅>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, Void param) {
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
  protected Void visitUnionType(UnionType obj, Void param) {
    writeNamedElementType(obj, "Union", null);
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, Void param) {
    writeNamedElementType(obj, "Record", null);
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(" : ");
    visit(obj.getType(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  private void writeNamedElementType(NamedElementType obj, String typename, Void param) {
    xw.wa(typename, getId(obj, param));
    xw.nl();
    xw.incIndent();
    visitList(obj.getElement(), null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
  }

  @Override
  protected Void visitRange(Range obj, Void param) {
    xw.wa("<" + obj.getLow().toString() + ".." + obj.getHigh().toString() + ">", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitArray(Array obj, Void param) {
    xw.wa(obj.getName(), getId(obj, param));
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, Void param) {
    xw.wa(obj.getName(), getId(obj, param));
    return null;
  }

  @Override
  protected Void visitArrayTemplate(ArrayTemplate obj, Void param) {
    xw.wa(ArrayTemplate.NAME, getId(obj, param));
    return null;
  }

  @Override
  protected Void visitIntegerType(IntegerType obj, Void param) {
    xw.wa("<Z>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitNaturalType(NaturalType obj, Void param) {
    xw.wa("<N>", getId(obj, param));
    return null;
  }

  @Override
  protected Void visitAnyType(AnyType obj, Void param) {
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
    visitList(obj.getStatements(), param);
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Void param) {
    visitList(obj.getLeft(), null);
    xw.wr(" := ");
    visit(obj.getRight(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitVarDefStmt(VarDefStmt obj, Void param) {
    visitList(obj.getVariable(), param);
    xw.wr(" = ");
    visit(obj.getInitial(), param);
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Void param) {
    visit(obj.getCall(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, Void param) {
    xw.kw("return");
    xw.wr(" ");
    visit(obj.getExpr(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, Void param) {
    xw.kw("return");
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitWhile(While obj, Void param) {
    xw.kw("while");
    xw.wr(" ");
    visit(obj.getCondition(), null);
    xw.kw(" do");
    xw.nl();
    xw.incIndent();
    visit(obj.getBody(), null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitForStmt(ForStmt obj, Void param) {
    xw.kw("for");
    xw.wr(" ");
    xw.wa(obj.getIterator().getName(), getId(obj.getIterator()));
    xw.kw(" in ");
    visit(obj.getIterator().getType(), param);
    xw.kw(" do");
    xw.nl();
    xw.incIndent();
    visit(obj.getBlock(), null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, Void param) {
    assert (!obj.getOption().isEmpty());

    boolean first = true;
    for (IfOption itr : obj.getOption()) {
      if (first) {
        xw.kw("if");
        xw.wr(" ");
        first = false;
      } else {
        xw.kw("ef");
        xw.wr(" ");
      }
      visit(itr.getCondition(), null);
      xw.wr(" ");
      xw.kw("then");
      xw.nl();
      xw.incIndent();
      visit(itr.getCode(), null);
      xw.decIndent();
    }

    if (!obj.getDefblock().getStatements().isEmpty()) {
      xw.kw("else");
      xw.nl();
      xw.incIndent();
      visit(obj.getDefblock(), null);
      xw.decIndent();
    }
    xw.kw("end");
    xw.nl();

    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, Void param) {
    xw.kw("case");
    xw.wr(" ");
    visit(obj.getCondition(), null);
    xw.wr(" ");
    xw.kw("of");
    xw.nl();
    xw.incIndent();
    visitList(obj.getOption(), null);
    if (!obj.getOtherwise().getStatements().isEmpty()) {
      xw.kw("else");
      xw.nl();
      xw.incIndent();
      visit(obj.getOtherwise(), null);
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
    list(obj.getValue(), ",", null);
    xw.wr(":");
    xw.nl();
    xw.incIndent();
    visit(obj.getCode(), null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Void param) {
    visit(obj.getStart(), null);
    xw.wr("..");
    visit(obj.getEnd(), null);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Void param) {
    visit(obj.getValue(), null);
    return null;
  }

  // ---- Expression ----------------------------------------------------------

  @Override
  protected Void visitArithmeticOp(ArithmeticOp obj, Void param) {
    xw.wr("(");
    visit(obj.getLeft(), null);
    xw.wr(" ");
    xw.kw(obj.getOp().toString());
    xw.wr(" ");
    visit(obj.getRight(), null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitUnaryExpression(UnaryExpression obj, Void param) {
    xw.wr("(");
    xw.kw(obj.getOp().toString());
    xw.wr(" ");
    visit(obj.getExpr(), null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitAnyValue(AnyValue obj, Void param) {
    xw.kw("_");
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, Void param) {
    xw.kw(obj.isValue() ? "True" : "False");
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, Void param) {
    xw.wr(obj.getValue().toString());
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, Void param) {
    xw.wr("'");
    xw.wr(obj.getValue());
    xw.wr("'");
    return null;
  }

  @Override
  protected Void visitNamedValue(NamedValue obj, Void param) {
    xw.wr(obj.getName());
    xw.wr(" := ");
    visit(obj.getValue(), null);
    return null;
  }

  @Override
  protected Void visitNamedElementsValue(NamedElementsValue obj, Void param) {
    xw.wr("[");
    list(obj.getValue(), ", ", param);
    xw.wr("]");
    return null;
  }

  @Override
  protected Void visitTupleValue(TupleValue obj, Void param) {
    xw.wr("(");
    list(obj.getValue(), ", ", null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitRelation(Relation obj, Void param) {
    xw.wr("(");
    visit(obj.getLeft(), null);
    xw.wr(" ");
    xw.wr(obj.getOp().toString());
    xw.wr(" ");
    visit(obj.getRight(), null);
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
    visitList(obj.getOffset(), null);
    return null;
  }

  private void wrRef(BaseRef obj) {
    Designator path = getObjPath(obj);
    if (path == null) {
      path = new Designator();  // TODO: ok?
    }
    String hint = obj.getLink().toString();
    String name;
    if (obj.getLink() instanceof Named) {
      name = obj.getLink().getName();
    } else {
      name = "???";
    }
    if (obj.getLink() instanceof DummyLinkTarget) {
      name = "\"" + name + "\"";
    }
    xw.wl(name, hint, path.toString(), getId(obj.getLink(), null));
  }

  @Override
  protected Void visitRefName(RefName obj, Void param) {
    xw.wr(".");
    xw.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, Void param) {
    xw.wr("[");
    visit(obj.getIndex(), param);
    xw.wr("]");
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, Void param) {
    visit(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Void visitRefTemplCall(RefTemplCall obj, Void param) {
    visitOptList("{", ", ", "}", obj.getActualParameter()); // FIXME it is a bit hacky, but maybe needed to make output
    // nicer
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(" : ");
    if (obj instanceof Constant) {
      xw.kw("const ");
    }
    visit(obj.getType(), param);
    if (obj instanceof DefVariable) {
      xw.wr(" = ");
      visit(((DefVariable) obj).getDef(), param);
    }
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    super.visitConstant(obj, param);
    xw.wr(";");
    xw.nl();
    return null;
  }

  private void printFuncImpl(FuncImpl obj) {
    xw.nl();
    xw.incIndent();
    visit(obj.getBody(), null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
  }

  @Override
  protected Void visitFuncReturnTuple(FuncReturnTuple obj, Void param) {
    xw.wr(":");
    xw.wr("(");
    visitOptList("", "; ", "", obj.getParam());
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitFuncReturnType(FuncReturnType obj, Void param) {
    xw.wr(":");
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected Void visitFuncReturnNone(FuncReturnNone obj, Void param) {
    return null;
  }

  private void printFunctionHeader(FuncHeader obj) {
    xw.wr("[");
    xw.wr(getId(obj, null));
    xw.wr("]");
    xw.wr("(");
    visitOptList("", "; ", "", obj.getParam());
    xw.wr(")");
    visit(obj.getRet(), null);
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

}
