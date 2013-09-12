package evl.doc;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import common.Designator;
import common.Direction;
import common.Scope;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.EvlBase;
import evl.NullTraverser;
import evl.cfg.CaseOptRange;
import evl.cfg.CaseOptValue;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.composition.Connection;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.ArithmeticOp;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Number;
import evl.expression.Relation;
import evl.expression.StringValue;
import evl.expression.UnaryExpression;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.knowledge.KnowPath;
import evl.knowledge.KnowScope;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Interface;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.VarDefStmt;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.NamedElementType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;
import fun.statement.CaseOpt;
import fun.statement.CaseStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.While;
import fun.type.base.TypeAlias;

public class RXmlPrinter extends NullTraverser<Void, XmlWriter> {
  private KnowPath kp;

  public RXmlPrinter(KnowledgeBase kb) {
    kp = kb.getEntry(KnowPath.class);
  }

  public static void print(Evl ast, Element parent, KnowledgeBase kb) {
    RXmlPrinter pp = new RXmlPrinter(kb);
    pp.traverse(ast, new XmlWriter(parent));
  }

  @Override
  protected Void visitDefault(Evl obj, XmlWriter param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  private void list(List<? extends Evl> list, String sep, XmlWriter param) {
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        param.wr(sep);
      }
      visit(list.get(i), param);
    }
  }

  private void visitList(List<? extends Evl> list, XmlWriter param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
    }
  }

  private <T extends Named> void visitList(ListOfNamed<T> list, XmlWriter param) {
    Iterator<T> itr = list.iterator();
    while (itr.hasNext()) {
      visit(itr.next(), param);
    }
  }

  @Override
  protected Void visitRizzlyProgram(RizzlyProgram obj, XmlWriter param) {
    param.nl();
    visitOptList("type", obj.getType(), param);
    visitOptList("const", obj.getConstant(), param);
    visitOptList("var", obj.getVariable(), param);
    param.nl();
    visitList(obj.getFunction(), param);
    param.nl();
    param.nl();
    param.wr(" // ------------------------------------ ");
    param.nl();
    param.nl();
    return null;
  }

  private void visitInterfaceDecl(String type, ListOfNamed<? extends Named> listOfNamed, XmlWriter param) {
    if (!listOfNamed.isEmpty()) {
      param.kw(type);
      param.nl();
      param.incIndent();
      visitList(listOfNamed, param);
      param.decIndent();
      param.nl();
    }
  }

  @Override
  protected Void visitInterface(Interface obj, XmlWriter param) {
    param.kw("Interface");
    param.nl();

    param.incIndent();
    visitList(obj.getPrototype(), param);
    param.decIndent();
    param.kw("end");
    param.nl();
    param.nl();
    return null;
  }

  protected void visitOptList(String name, ListOfNamed<? extends Named> type, XmlWriter param) {
    if (type.isEmpty()) {
      return;
    }
    param.kw(name);
    param.nl();
    param.incIndent();
    visitList(type, param);
    param.decIndent();
  }

  protected void visitOptList(String name, List<? extends EvlBase> type, XmlWriter param) {
    if (type.isEmpty()) {
      return;
    }
    param.kw(name);
    param.nl();
    param.incIndent();
    visitList(type, param);
    param.decIndent();
  }

  @Override
  protected Void visitNamespace(Namespace obj, XmlWriter param) {
    assert (false);
    return null;
  }

  @Override
  protected Void visitNamedList(NamedList<Named> obj, XmlWriter param) {
    // assert (false);
    param.kw("namespace");
    param.wr(" ");
    param.wr(obj.getName());
    param.nl();
    param.incIndent();
    visitItr(obj, param);
    param.decIndent();
    param.kw("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, XmlWriter param) {
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIfaceUse(IfaceUse obj, XmlWriter param) {
    param.wr(";");
    param.nl();
    return null;
  }

  private void compHeader(Component obj, XmlWriter param) {
    param.kw("Component");
    param.nl();

    param.incIndent();
    param.nl();
    visitInterfaceDecl("input", obj.getIface(Direction.in), param);
    visitInterfaceDecl("output", obj.getIface(Direction.out), param);
    param.decIndent();
  }

  @Override
  protected Void visitComponent(Component obj, XmlWriter param) {
    compHeader(obj, param);
    param.incIndent();
    super.visitComponent(obj, param);
    param.decIndent();
    param.kw("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, XmlWriter param) {
    param.kw("implementation elementary");
    param.nl();
    param.nl();

    visitOptList("const", obj.getConstant(), param);
    visitOptList("component", obj.getComponent(), param);
    visitOptList("var", obj.getVariable(), param);

    param.kw("entry: ");
    visit(obj.getEntryFunc(), param);
    param.wr(";");
    param.nl();
    param.kw("exit: ");
    visit(obj.getExitFunc(), param);
    param.wr(";");
    param.nl();
    param.nl();

    visitList(obj.getInputFunc(), param);
    visitList(obj.getSubComCallback(), param);
    visitList(obj.getInternalFunction(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, XmlWriter param) {
    param.kw("implementation composition");
    param.nl();
    param.nl();

    visitOptList("component", obj.getComponent(), param);
    visitOptList("connection", obj.getConnection(), param);

    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, XmlWriter param) {
    param.kw("implementation hfsm");
    param.nl();
    param.nl();
    param.incIndent();

    visit(obj.getTopstate(), param);

    param.decIndent();

    return null;
  }

  @Override
  protected Void visitConnection(Connection obj, XmlWriter param) {
    visit(obj.getEndpoint(Direction.in), param);
    switch (obj.getType()) {
    case sync:
      param.wr(" -> ");
      break;
    case async:
      param.wr(" >> ");
      break;
    default:
      RError.err(ErrorType.Fatal, obj.getInfo(), "Not yet implemented connection type: " + obj.getType());
    }
    visit(obj.getEndpoint(Direction.in), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, XmlWriter param) {
    param.kw("function(");
    list(obj.getParam().getList(), "; ", param);
    param.wr(")");
    if (obj instanceof FuncWithReturn) {
      param.wr(":");
      visit(((FuncWithReturn) obj).getRet(), param);
    }
    param.nl();
    if (obj instanceof FuncWithBody) {
      param.incIndent();
      visit(((FuncWithBody) obj).getBody(), param);
      param.decIndent();
      param.kw("end");
      param.nl();
    }
    param.nl();
    return null;
  }

  // ---- hfsm ----------------------------------------------------------------

  @Override
  protected Void visitStateComposite(StateComposite obj, XmlWriter param) {
    param.kw("state");
    param.wr(" ");
    param.wa(obj.getName(), true);
    param.wr("(");
    param.wr(obj.getInitial().getName());
    param.wr(")");
    param.nl();
    param.incIndent();

    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    visitOptList("var", obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    visitList(obj.getItem(), param);

    param.decIndent();
    param.kw("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, XmlWriter param) {
    param.kw("state");
    param.wr(" ");
    param.wa(obj.getName(), true);
    param.nl();
    param.incIndent();

    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    visitOptList("var", obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    visitList(obj.getItem(), param);

    param.decIndent();
    param.kw("end");
    param.nl();
    param.nl();
    return null;
  }

  public void wrStateEntryExit(String name, Block code, XmlWriter param) {
    param.kw(name);
    param.nl();
    param.incIndent();
    visit(code, param);
    param.decIndent();
    param.kw("end");
    param.nl();
  }

  @Override
  protected Void visitTransition(Transition obj, XmlWriter param) {
    param.kw("transition");
    param.nl();
    param.incIndent();

    param.wr(obj.getSrc().getName());
    param.wr(" ");
    param.kw("to");
    param.wr(" ");
    param.wr(obj.getDst().getName());
    param.wr(" ");
    param.kw("by");
    param.wr(" ");
    param.wr(obj.getEventIface().getName());
    param.wr(".");
    param.wr(obj.getEventFunc());
    param.wr("(");
    list(obj.getParam().getList(), "; ", param);
    param.wr(")");
    param.wr(" ");
    param.kw("if");
    param.wr(" ");
    visit(obj.getGuard(), param);
    param.wr(" ");
    param.kw("do");
    param.nl();
    param.incIndent();
    visit(obj.getBody(), param);
    param.decIndent();
    param.kw("end");
    param.nl();
    param.nl();

    param.decIndent();

    return null;
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, XmlWriter param) {
    param.kw("namespace '");
    param.wr(obj.getNamespace());
    param.wr("'");
    param.nl();
    param.incIndent();
    visit(obj.getFunc(), param);
    param.decIndent();
    param.kw("end");
    param.nl();
    return null;
  }

  // ---- Type ----------------------------------------------------------------

  @Override
  protected Void visitTypeAlias(TypeAlias obj, XmlWriter param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, XmlWriter param) {
    param.wa(obj.getName(), false);
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, XmlWriter param) {
    param.wa(obj.getName(), false);
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, XmlWriter param) {
    param.kw("Enum");
    param.nl();
    param.incIndent();
    visitList(obj.getElement(), param);
    param.decIndent();
    param.kw("end");
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, XmlWriter param) {
    param.wa(obj.getName(), false);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, XmlWriter param) {
    writeNamedElementType(obj, "Union", param);
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, XmlWriter param) {
    writeNamedElementType(obj, "Record", param);
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, XmlWriter param) {
    param.wa(obj.getName(), false);
    param.wr(" : ");
    param.wr(obj.getType().getName());
    param.wr(";");
    param.nl();
    return null;
  }

  private void writeNamedElementType(NamedElementType obj, String typename, XmlWriter param) {
    param.kw(typename);
    param.nl();
    param.incIndent();
    visitList(obj.getElement(), param);
    param.decIndent();
    param.kw("end");
  }

  @Override
  protected Void visitArrayType(ArrayType obj, XmlWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, XmlWriter param) {
    param.wa(obj.getName(), false);
    return null;
  }

  // ---- Statement -----------------------------------------------------------

  @Override
  protected Void visitBlock(Block obj, XmlWriter param) {
    visitListOfNamed(obj.getStatements(), param);
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, XmlWriter param) {
    visit(obj.getLeft(), param);
    param.wr(" := ");
    visit(obj.getRight(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, XmlWriter param) {
    visit(obj.getVariable(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, XmlWriter param) {
    visit(obj.getCall(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, XmlWriter param) {
    param.kw("return");
    param.wr(" ");
    visit(obj.getExpr(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, XmlWriter param) {
    param.kw("return");
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitWhile(While obj, XmlWriter param) {
    param.kw("while");
    param.wr(" ");
    visit(obj.getCondition(), param);
    param.wr(" do");
    param.nl();
    param.incIndent();
    visit(obj.getBody(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIf(IfStmt obj, XmlWriter param) {
    assert (!obj.getOption().isEmpty());

    boolean first = true;
    for (IfOption itr : obj.getOption()) {
      if (first) {
        param.kw("if");
        param.wr(" ");
        first = false;
      } else {
        param.kw("ef");
        param.wr(" ");
      }
      visit(itr.getCondition(), param);
      param.wr(" ");
      param.kw("then");
      param.nl();
      param.incIndent();
      visit(itr.getCode(), param);
      param.decIndent();
    }

    param.kw("else");
    param.nl();
    param.incIndent();
    visit(obj.getDefblock(), param);
    param.decIndent();
    param.kw("end");
    param.nl();

    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, XmlWriter param) {
    param.kw("case");
    param.wr(" ");
    visit(obj.getCondition(), param);
    param.wr(" ");
    param.kw("of");
    param.nl();
    param.incIndent();
    visitItr(obj.getOption(), param);
    if (!obj.getOtherwise().getStatements().isEmpty()) {
      param.kw("else");
      param.nl();
      param.incIndent();
      visit(obj.getOtherwise(), param);
      param.decIndent();
      param.kw("end");
      param.nl();
    }
    param.decIndent();
    param.kw("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseOpt(CaseOpt obj, XmlWriter param) {
    list(obj.getValue(), ",", param);
    param.wr(":");
    param.nl();
    param.incIndent();
    visit(obj.getCode(), param);
    param.decIndent();
    param.kw("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, XmlWriter param) {
    visit(obj.getStart(), param);
    param.wr("..");
    visit(obj.getEnd(), param);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, XmlWriter param) {
    visit(obj.getValue(), param);
    return null;
  }

  // ---- Expression ----------------------------------------------------------

  @Override
  protected Void visitArithmeticOp(ArithmeticOp obj, XmlWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(" ");
    param.kw(obj.getOp().toString());
    param.wr(" ");
    visit(obj.getRight(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitUnaryExpression(UnaryExpression obj, XmlWriter param) {
    param.wr("(");
    param.kw(obj.getOp().toString());
    param.wr(" ");
    visit(obj.getExpr(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, XmlWriter param) {
    param.kw(obj.isValue() ? "True" : "False");
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, XmlWriter param) {
    param.wr(Integer.toString(obj.getValue()));
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, XmlWriter param) {
    param.wr("'");
    param.wr(obj.getValue());
    param.wr("'");
    return null;
  }

  @Override
  protected Void visitArrayValue(ArrayValue obj, XmlWriter param) {
    param.wr("[");
    list(obj.getValue(), ", ", param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitRelation(Relation obj, XmlWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(" ");
    param.wr(obj.getOp().toString());
    param.wr(" ");
    visit(obj.getRight(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, XmlWriter param) {
    Scope scope = KnowScope.get(obj);
    param.wa(obj.getName(), scope == Scope.privat);
    param.wr(": ");
    param.wr(obj.getType().getName());
    super.visitVariable(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, XmlWriter param) {
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, XmlWriter param) {
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, XmlWriter param) {
    param.wr(" = ");
    visit(obj.getDef(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, XmlWriter param) {
    String title = obj.getLink().toString();
    Scope scope = KnowScope.get(obj.getLink());
    if (scope == Scope.global) {
      Designator path = kp.get(obj.getLink());
      param.wl(obj.getLink().getName(), path);
    } else {
      param.wl(obj.getLink().getName(), scope == Scope.privat, title);
    }
    visitItr(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, XmlWriter param) {
    param.wr(".");
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, XmlWriter param) {
    param.wr("(");
    list(obj.getActualParameter(), ", ", param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, XmlWriter param) {
    param.wr("[");
    visit(obj.getIndex(), param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitEndpointSelf(EndpointSelf obj, XmlWriter param) {
    Designator path = kp.get(obj.getIface());
    param.wl(obj.getIface().getName(), path);
    return null;
  }

  @Override
  protected Void visitEndpointSub(EndpointSub obj, XmlWriter param) {
    Designator path = kp.get(obj.getComp());
    param.wl(obj.getComp().getName(), path);
    param.wr(".");
    param.wr(obj.getIface());
    return null;
  }
}
