package fun.doc;

import java.util.ArrayList;
import java.util.Collection;
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
import fun.function.FuncWithBody;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.Component;
import fun.other.Generator;
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
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.type.base.AnyType;
import fun.type.base.BooleanType;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.TypeAlias;
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
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;

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

  protected Designator getObjPath(Reference obj) {
    return new Designator();
  }

  private void list(List<? extends Fun> list, String sep, Void param) {
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        xw.wr(sep);
      }
      visit(list.get(i), param);
    }
  }

  private void visitList(List<? extends Fun> list, Void param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
    }
  }

  private <T extends Named> void visitList(ListOfNamed<T> list, Void param) {
    Iterator<T> itr = list.iterator();
    while (itr.hasNext()) {
      visit(itr.next(), param);
    }
  }

  private void visitOptList(String before, String sep, String after, Collection<? extends Fun> list) {
    if (!list.isEmpty()) {
      xw.wr(before);
      boolean first = true;
      for (Fun itr : list) {
        if (first) {
          first = false;
        } else {
          xw.wr(sep);
        }
        visit(itr, null);
      }
      xw.wr(after);
    }
  }

  private void visitNamedSection(String name, Collection<? extends Fun> data) {
    if (!data.isEmpty()) {
      xw.sectionSeparator();
      xw.kw(name);
      xw.nl();
      xw.incIndent();
      visitItr(data, null);
      xw.decIndent();
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

  private void printEntryExit(String name, Reference ref) {
    assert (ref.getOffset().isEmpty());
    FuncEntryExit func = (FuncEntryExit) ref.getLink();
    if (!func.getBody().getStatements().isEmpty()) {
      xw.kw(name);
      xw.nl();
      xw.incIndent();
      visit(func.getBody(), null);
      xw.decIndent();
      xw.kw("end");
      xw.nl();
    }
  }

  private void printFunc(FunctionHeader func, boolean nofunc) {
    if (!nofunc) {
      xw.kw("function ");
    }
    xw.wa(func.getName(), getId(func));
    xw.wr("(");
    visitOptList("", "; ", "", func.getParam().getList());
    xw.wr(")");
    if (func instanceof FuncWithReturn) {
      xw.wr(":");
      visit(((FuncWithReturn) func).getRet(), null);
    }
    if (func instanceof FuncWithBody) {
      xw.nl();
      xw.incIndent();
      visit(((FuncWithBody) func).getBody(), null);
      xw.decIndent();
      xw.kw("end");
      xw.nl();
    } else {
      xw.wr(";");
      xw.nl();
    }
  }

  // TODO write that before generators like component
  private void wrGen(Generator obj) {
    xw.sectionSeparator();
    xw.wa(obj.getName(), getId(obj));
    visitOptList("{", "; ", "}", obj.getTemplateParam().getList());
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, Void param) {
    assert (param == null);
    writeMeta(obj);
    visitImports(obj.getImports(), null);
    xw.sectionSeparator();
    visitNamedSection("const", obj.getConstant().getList());
    xw.sectionSeparator();
    visitList(obj.getFunction(), null);

    visitNamedSection("type", obj.getType().getList());
    visitList(obj.getFunction(), null);
    visitNamedSection("component", obj.getComp().getList());

    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
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

  @Override
  protected Void visitComponent(Component obj, Void param) {
    wrGen(obj);
    xw.incIndent();
    visitNamedSection("input", obj.getIface(Direction.in).getList());
    xw.sectionSeparator();
    visitNamedSection("output", obj.getIface(Direction.out).getList());
    xw.sectionSeparator();
    xw.decIndent();
    super.visitComponent(obj, null);
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    xw.kw("implementation elementary");
    xw.nl();
    xw.incIndent();

    visitNamedSection("const", obj.getConstant().getList());
    visitNamedSection("var", obj.getVariable().getList());

    printEntryExit("entry", obj.getEntryFunc());
    printEntryExit("entry", obj.getExitFunc());

    visitList(obj.getFunction(), param);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    xw.kw("implementation composition");
    xw.nl();
    writeMeta(obj);
    xw.incIndent();

    visitNamedSection("component", obj.getComponent().getList());
    xw.sectionSeparator();
    visitNamedSection("connection", obj.getConnection());
    xw.sectionSeparator();
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

  @Override
  protected Void visitRangeTemplate(RangeTemplate obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(";");
    xw.nl();
    return null;
  }

  // ---- hfsm ----------------------------------------------------------------

  private void printStateBody(State obj) {
    xw.incIndent();

    printEntryExit("entry", obj.getEntryFuncRef());
    printEntryExit("exit", obj.getExitFuncRef());
    visitNamedSection("var", obj.getVariable().getList());
    visitList(obj.getItemList(), null);

    xw.decIndent();
    xw.kw("end");
    xw.nl();
    xw.sectionSeparator();
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    xw.kw("implementation hfsm");
    xw.wr("(");
    visit(obj.getTopstate().getInitial(), null);
    xw.wr(")");
    xw.nl();

    printStateBody(obj.getTopstate());

    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, Void param) {
    xw.kw("state");
    xw.wr(" ");
    xw.wa(obj.getName(), getId(obj));
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
    xw.wr(" ");
    xw.wa(obj.getName(), getId(obj));
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
    list(obj.getParam().getList(), "; ", null);
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
  protected Void visitTypeAlias(TypeAlias obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(" = ");
    visit(obj.getRef(), null);
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(" = ");
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(" = ");
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(" = ");
    xw.kw("Enum");
    xw.nl();
    xw.incIndent();
    visitList(obj.getElement(), null);
    xw.decIndent();
    xw.kw("end");
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(";");
    xw.nl();
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
    wrGen(obj);
    xw.wr(" = ");
    xw.kw(typename);
    xw.nl();
    xw.incIndent();
    visitList(obj.getElement(), null);
    xw.decIndent();
    xw.kw("end");
    xw.nl();
  }

  @Override
  protected Void visitRange(Range obj, Void param) {
    xw.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitArray(Array obj, Void param) {
    xw.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, Void param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitArrayTemplate(ArrayTemplate obj, Void param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitIntegerType(IntegerType obj, Void param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitNaturalType(NaturalType obj, Void param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitAnyType(AnyType obj, Void param) {
    xw.kw(obj.getName());
    xw.wr(";");
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
    xw.kw(obj.getName());
    xw.wr(";");
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
    visit(obj.getLeft(), null);
    xw.wr(" := ");
    visit(obj.getRight(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Void param) {
    visit(obj.getVariable(), param);
    xw.wr(";");
    xw.nl();
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
    visitItr(obj.getOption(), null);
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
  protected Void visitArrayValue(ArrayValue obj, Void param) {
    xw.wr("[");
    list(obj.getValue(), ", ", null);
    xw.wr("]");
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
  protected Void visitReference(Reference obj, Void param) {
    Designator path = getObjPath(obj);
    String hint = obj.getLink().toString();
    xw.wl(obj.getLink().getName(), hint, path.toString(), getId(obj.getLink()));
    visitItr(obj.getOffset(), null);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, Void param) {
    xw.wr(".");
    xw.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, Void param) {
    xw.wr("(");
    list(obj.getActualParameter(), ", ", null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitRefCompcall(RefTemplCall obj, Void param) {
    visitOptList("{", ", ", "}", obj.getActualParameter()); // FIXME it is a bit hacky, but maybe needed to make output
    // nicer
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, Void param) {
    xw.wr("[");
    visit(obj.getIndex(), null);
    xw.wr("]");
    return null;
  }

  @Override
  protected Void visitConstPrivate(ConstPrivate obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(": ");
    visit(obj.getType(), null);
    xw.wr(" = ");
    visit(obj.getDef(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitConstGlobal(ConstGlobal obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(": ");
    visit(obj.getType(), null);
    xw.wr(" = ");
    visit(obj.getDef(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(": ");
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected Void visitTemplateParameter(TemplateParameter obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(": ");
    visit(obj.getType(), null);
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Void param) {
    xw.wa(obj.getName(), getId(obj));
    xw.wr(": ");
    visit(obj.getType(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitFuncPrivate(FuncPrivateVoid obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    xw.sectionSeparator();
    printFunc(obj, false);
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    xw.sectionSeparator();
    printFunc(obj, false);
    return null;
  }

  @Override
  protected Void visitFuncProtRet(FuncProtRet obj, Void param) {
    printFunc(obj, false);
    return null;
  }

  @Override
  protected Void visitFuncProtVoid(FuncProtVoid obj, Void param) {
    printFunc(obj, false);
    return null;
  }

  @Override
  protected Void visitFuncGlobal(FuncGlobal obj, Void param) {
    // xw.sectionSeparator();
    printFunc(obj, false);
    return null;
  }

  @Override
  protected Void visitFuncEntryExit(FuncEntryExit obj, Void param) {
    return null; // it is written otherwise
  }

}
