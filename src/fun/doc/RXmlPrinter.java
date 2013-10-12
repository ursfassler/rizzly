package fun.doc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

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
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FuncWithBody;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
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
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateItem;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.knowledge.KnowFunFile;
import fun.knowledge.KnowledgeBase;
import fun.other.Component;
import fun.other.ImplElementary;
import fun.other.Interface;
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
import fun.type.NamedType;
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
import fun.type.template.TypeTypeTemplate;
import fun.type.template.Range;
import fun.type.template.TypeType;
import fun.variable.CompUse;
import fun.variable.TemplateParameter;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.IfaceUse;
import fun.variable.StateVariable;

public class RXmlPrinter extends NullTraverser<Void, Designator> {
  private XmlWriter xw;
  private KnowFunFile kff;

  public RXmlPrinter(XmlWriter xw, KnowledgeBase kb) {
    this.xw = xw;
    kff = kb.getEntry(KnowFunFile.class);
  }

  public static void print(Fun ast, Element parent, KnowledgeBase kb) {
    RXmlPrinter pp = new RXmlPrinter(new XmlWriter(parent), kb);
    pp.traverse(ast, null);
  }

  @Override
  protected Void visitDefault(Fun obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  private void list(List<? extends Fun> list, String sep, Designator param) {
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        xw.wr(sep);
      }
      visit(list.get(i), param);
    }
  }

  private void visitList(List<? extends Fun> list, Designator param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
    }
  }

  private <T extends Named> void visitList(ListOfNamed<T> list, Designator param) {
    Iterator<T> itr = list.iterator();
    while (itr.hasNext()) {
      visit(itr.next(), param);
    }
  }

  private void visitOptList(String before, String after, Collection<? extends Fun> list) {
    if (!list.isEmpty()) {
      xw.wr(before);
      boolean first = true;
      for (Fun itr : list) {
        if (first) {
          first = false;
        } else {
          xw.wr(", ");
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

  private void visitImports(List<Designator> imports, Designator param) {
    if (!imports.isEmpty()) {
      xw.kw("import");
      xw.nl();
      xw.incIndent();
      for (Designator ref : imports) {
        xw.wr(ref.toString("."));
        xw.wr(";");
        xw.nl();
      }
      xw.decIndent();
    }
  }

  private void printEntryExit(String name, ReferenceLinked ref) {
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

  private void printFunc(FunctionHeader func, Designator ns) {
    xw.kw("function ");
    ArrayList<String> nslist = ns.toList();
    ns = new Designator();
    for (String itm : nslist) {
      ns = new Designator(ns, itm);
      // xw.wl(itm, true, itm);
      xw.wr(itm); // TODO write link
      xw.wr(".");
    }
    xw.wa(func);
    visitOptList("(", ")", func.getParam().getList());
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
      xw.kw(";");
      xw.nl();
    }
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, Designator param) {
    assert (param == null);
    writeMeta(obj);
    visitImports(obj.getImports(), null);
    xw.sectionSeparator();
    visitNamedSection("const", obj.getConstant().getList());
    xw.sectionSeparator();
    visitList(obj.getFunction(), null);

    List<TypeGenerator> types = obj.getCompfunc().getItems(TypeGenerator.class);
    List<InterfaceGenerator> ifaces = obj.getCompfunc().getItems(InterfaceGenerator.class);
    List<ComponentGenerator> compos = obj.getCompfunc().getItems(ComponentGenerator.class);
    assert (types.size() + ifaces.size() + compos.size() == obj.getCompfunc().size());

    visitNamedSection("type", types);
    visitNamedSection("interface", ifaces);
    visitNamedSection("component", compos);

    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    assert (param != null);
    param = new Designator(param, obj.getName());
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Designator param) {
    xw.wa(obj);
    xw.wr(": ");
    visit(obj.getType(), null);
    xw.wr(";");
    xw.nl();
    writeMeta(obj);
    return null;
  }

  @Override
  protected Void visitIfaceUse(IfaceUse obj, Designator param) {
    xw.wa(obj);
    xw.wr(": ");
    visit(obj.getType(), null);
    xw.wr(";");
    xw.nl();
    writeMeta(obj);
    return null;
  }

  @Override
  protected Void visitComponent(Component obj, Designator param) {
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
  protected Void visitImplElementary(ImplElementary obj, Designator param) {
    xw.kw("implementation elementary");
    xw.nl();
    xw.incIndent();

    visitNamedSection("const", obj.getConstant().getList());
    visitNamedSection("component", obj.getComponent().getList());
    visitNamedSection("var", obj.getVariable().getList());

    printEntryExit("entry", (ReferenceLinked) obj.getEntryFunc());
    printEntryExit("entry", (ReferenceLinked) obj.getExitFunc());

    visitList(obj.getFunction(), new Designator());
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Designator param) {
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
  protected Void visitConnection(Connection obj, Designator param) {
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
  protected Void visitTypeGenerator(TypeGenerator obj, Designator param) {
    xw.sectionSeparator();
    xw.wa(obj);
    visitOptList("{", "}", obj.getParam().getList());
    xw.wr(" = ");
    visit(obj.getItem(), param);
    xw.nl();
    return null;
  }

  @Override
  protected Void visitComponentGenerator(ComponentGenerator obj, Designator param) {
    xw.sectionSeparator();
    xw.wa(obj);
    visitOptList("{", "}", obj.getParam().getList());
    xw.nl();
    visit(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitInterfaceGenerator(InterfaceGenerator obj, Designator param) {
    xw.wa(obj);
    visitOptList("{", "}", obj.getParam().getList());
    xw.nl();
    visit(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitInterface(Interface obj, Designator param) {
    xw.incIndent();
    visitList(obj.getPrototype(), new Designator());
    xw.decIndent();
    xw.kw("end");
    xw.nl();
    return null;
  }

  // ---- hfsm ----------------------------------------------------------------

  private void printStateBody(State obj) {
    xw.incIndent();

    printEntryExit("entry", (ReferenceLinked) obj.getEntryFuncRef());
    printEntryExit("exit", (ReferenceLinked) obj.getExitFuncRef());
    visitNamedSection("var", obj.getVariable().getList());
    visitList(obj.getBfunc(), new Designator());
    visitList(obj.getItem(), null);

    xw.decIndent();
    xw.kw("end");
    xw.nl();
    xw.sectionSeparator();
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Designator param) {
    xw.kw("implementation hfsm");
    xw.wr("(");
    visit(obj.getTopstate().getInitial(), null);
    xw.wr(")");
    xw.nl();

    printStateBody(obj.getTopstate());

    return null;
  }

  @Override
  protected Void visitStateItem(StateItem obj, Designator param) {
    xw.sectionSeparator();
    return super.visitStateItem(obj, param);
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, Designator param) {
    xw.kw("state");
    xw.wr(" ");
    xw.wa(obj);
    xw.wr("(");
    visit(obj.getInitial(), null);
    xw.wr(")");
    xw.nl();

    printStateBody(obj);
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, Designator param) {
    xw.kw("state");
    xw.wr(" ");
    xw.wa(obj);
    xw.nl();

    printStateBody(obj);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Designator param) {
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

  @Override
  protected Void visitQueryItem(QueryItem obj, Designator param) {
    assert (param == null);
    visit(obj.getFunc(), new Designator(obj.getNamespace()));
    return null;
  }

  // ---- Type ----------------------------------------------------------------

  @Override
  protected Void visitNamedType(NamedType obj, Designator param) {
    xw.wa(obj);
    xw.wr(" = ");
    visit(obj.getType(), null);
    xw.nl();
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, Designator param) {
    visit(obj.getRef(), null);
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, Designator param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, Designator param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, Designator param) {
    xw.kw("Enum");
    xw.nl();
    xw.incIndent();
    visitList(obj.getElement(), null);
    xw.decIndent();
    xw.kw("end");
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, Designator param) {
    xw.wa(obj);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, Designator param) {
    writeNamedElementType(obj, "Union", null);
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, Designator param) {
    writeNamedElementType(obj, "Record", null);
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Designator param) {
    xw.wa(obj);
    xw.wr(" : ");
    visit(obj.getType(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  private void writeNamedElementType(NamedElementType obj, String typename, Designator param) {
    xw.kw(typename);
    xw.nl();
    xw.incIndent();
    visitList(obj.getElement(), null);
    xw.decIndent();
    xw.kw("end");
  }

  @Override
  protected Void visitRange(Range obj, Designator param) {
    xw.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitArray(Array obj, Designator param) {
    xw.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, Designator param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitGenericArray(ArrayTemplate obj, Designator param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitIntegerType(IntegerType obj, Designator param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitNaturalType(NaturalType obj, Designator param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitAnyType(AnyType obj, Designator param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  @Override
  protected Void visitTypeType(TypeType obj, Designator param) {
    xw.wr(TypeTypeTemplate.NAME);
    xw.wr("(");
    visit(obj.getType(), null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitGenericTypeType(TypeTypeTemplate obj, Designator param) {
    xw.kw(obj.getName());
    xw.wr(";");
    return null;
  }

  // ---- Statement -----------------------------------------------------------

  @Override
  protected Void visitBlock(Block obj, Designator param) {
    visitList(obj.getStatements(), param);
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Designator param) {
    visit(obj.getLeft(), null);
    xw.wr(" := ");
    visit(obj.getRight(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Designator param) {
    visit(obj.getVariable(), param);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Designator param) {
    visit(obj.getCall(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, Designator param) {
    xw.kw("return");
    xw.wr(" ");
    visit(obj.getExpr(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, Designator param) {
    xw.kw("return");
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitWhile(While obj, Designator param) {
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
  protected Void visitIfStmt(IfStmt obj, Designator param) {
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
  protected Void visitCaseStmt(CaseStmt obj, Designator param) {
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
  protected Void visitCaseOpt(CaseOpt obj, Designator param) {
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
  protected Void visitCaseOptRange(CaseOptRange obj, Designator param) {
    visit(obj.getStart(), null);
    xw.wr("..");
    visit(obj.getEnd(), null);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Designator param) {
    visit(obj.getValue(), null);
    return null;
  }

  // ---- Expression ----------------------------------------------------------

  @Override
  protected Void visitArithmeticOp(ArithmeticOp obj, Designator param) {
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
  protected Void visitUnaryExpression(UnaryExpression obj, Designator param) {
    xw.wr("(");
    xw.kw(obj.getOp().toString());
    xw.wr(" ");
    visit(obj.getExpr(), null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, Designator param) {
    xw.kw(obj.isValue() ? "True" : "False");
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, Designator param) {
    xw.wr(obj.getValue().toString());
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, Designator param) {
    xw.wr("'");
    xw.wr(obj.getValue());
    xw.wr("'");
    return null;
  }

  @Override
  protected Void visitArrayValue(ArrayValue obj, Designator param) {
    xw.wr("[");
    list(obj.getValue(), ", ", null);
    xw.wr("]");
    return null;
  }

  @Override
  protected Void visitRelation(Relation obj, Designator param) {
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
  protected Void visitReferenceUnlinked(ReferenceUnlinked obj, Designator param) {
    assert (false);
    xw.wr("'");
    xw.wr(obj.getName().toString("."));
    xw.wr("'");
    visitItr(obj.getOffset(), null);
    return null;
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, Designator param) {
    RizzlyFile file = kff.find(obj.getLink()); // FIXME find better way to handle built in functions and so
    Designator path;
    if (file == null) {
      path = new Designator();
    } else {
      path = file.getName();
    }
    String title = obj.getLink().toString();
    xw.wl(obj.getLink(), title, path);
    visitItr(obj.getOffset(), null);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, Designator param) {
    xw.wr(".");
    xw.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, Designator param) {
    xw.wr("(");
    list(obj.getActualParameter(), ", ", null);
    xw.wr(")");
    return null;
  }

  @Override
  protected Void visitRefCompcall(RefTemplCall obj, Designator param) {
    visitOptList("{", "}", obj.getActualParameter()); // FIXME it is a bit hacky, but maybe needed to make output
                                                      // nicer
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, Designator param) {
    xw.wr("[");
    visit(obj.getIndex(), null);
    xw.wr("]");
    return null;
  }

  @Override
  protected Void visitConstPrivate(ConstPrivate obj, Designator param) {
    xw.wa(obj);
    xw.wr(": ");
    visit(obj.getType(), null);
    xw.wr(" = ");
    visit(obj.getDef(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitConstGlobal(ConstGlobal obj, Designator param) {
    xw.wa(obj);
    xw.wr(": ");
    visit(obj.getType(), null);
    xw.wr(" = ");
    visit(obj.getDef(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Designator param) {
    xw.wa(obj);
    xw.wr(": ");
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected Void visitCompfuncParameter(TemplateParameter obj, Designator param) {
    xw.wa(obj);
    xw.wr(": ");
    visit(obj.getType(), null);
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Designator param) {
    xw.wa(obj);
    xw.wr(": ");
    visit(obj.getType(), null);
    xw.wr(";");
    xw.nl();
    return null;
  }

  @Override
  protected Void visitFuncPrivate(FuncPrivateVoid obj, Designator param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Designator param) {
    xw.sectionSeparator();
    printFunc(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Designator param) {
    xw.sectionSeparator();
    printFunc(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncProtRet(FuncProtRet obj, Designator param) {
    printFunc(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncProtVoid(FuncProtVoid obj, Designator param) {
    printFunc(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncGlobal(FuncGlobal obj, Designator param) {
    xw.sectionSeparator();
    assert (param == null);
    printFunc(obj, new Designator());
    return null;
  }

  @Override
  protected Void visitFuncEntryExit(FuncEntryExit obj, Designator param) {
    return null; // it is written otherwise
  }

}
