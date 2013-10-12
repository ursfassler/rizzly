package fun.doc;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import common.Designator;
import common.Direction;

import error.ErrorType;
import error.RError;
import evl.doc.StreamWriter;
import fun.Fun;
import fun.FunBase;
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
import fun.generator.Generator;
import fun.hfsm.ImplHfsm;
import fun.hfsm.QueryItem;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.Component;
import fun.other.ImplElementary;
import fun.other.Interface;
import fun.other.ListOfNamed;
import fun.other.Named;
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
import fun.type.template.RangeTemplate;
import fun.type.template.TypeTypeTemplate;
import fun.type.template.Range;
import fun.type.template.TypeType;
import fun.variable.CompUse;
import fun.variable.TemplateParameter;
import fun.variable.Constant;
import fun.variable.FuncVariable;
import fun.variable.IfaceUse;
import fun.variable.StateVariable;
import fun.variable.Variable;

public class PrettyPrinter extends NullTraverser<Void, StreamWriter> {

  public static void print(Collection<? extends Fun> list, String filename) {
    PrettyPrinter pp = new PrettyPrinter();
    try {
      pp.visitItr(list, new StreamWriter(new PrintStream(filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static void print(Fun ast, String filename) {
    PrettyPrinter pp = new PrettyPrinter();
    try {
      pp.traverse(ast, new StreamWriter(new PrintStream(filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected Void visitDefault(Fun obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  private void list(List<? extends Fun> list, String sep, StreamWriter param) {
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        param.wr(sep);
      }
      visit(list.get(i), param);
    }
  }

  private void visitList(List<? extends Fun> list, StreamWriter param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
    }
  }

  private <T extends Named> void visitList(ListOfNamed<T> list, StreamWriter param) {
    Iterator<T> itr = list.iterator();
    while (itr.hasNext()) {
      visit(itr.next(), param);
    }
  }

  private void wrId(Fun obj, StreamWriter wr) {
    wr.wr("[" + obj.hashCode() % 10000 + "]");
  }

  private void visitImports(List<Designator> imports, StreamWriter param) {
    if (!imports.isEmpty()) {
      param.wr("import");
      param.nl();
      param.incIndent();
      for (Designator ref : imports) {
        param.wr(ref.toString("."));
        param.wr(";");
        param.nl();
      }
      param.decIndent();
      param.nl();
    }
  }

  private void visitInterfaceDecl(String type, ListOfNamed<? extends Variable> listOfNamed, StreamWriter param) {
    if (!listOfNamed.isEmpty()) {
      param.wr(type);
      param.nl();
      param.incIndent();
      visitList(listOfNamed, param);
      param.decIndent();
      param.nl();
    }
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, StreamWriter param) {
    param.wr("// file: ");
    param.wr(obj.getName().toString());
    param.nl();
    visitImports(obj.getImports(), param);
    param.nl();
    visitList(obj.getConstant(), param);
    visitList(obj.getFunction(), param);
    return null;
  }

  @Override
  protected Void visitInterface(Interface obj, StreamWriter param) {
    param.wr("Interface");
    param.nl();

    param.incIndent();
    visitList(obj.getPrototype(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  protected void visitOptList(String name, ListOfNamed<? extends Named> type, StreamWriter param) {
    if (type.isEmpty()) {
      return;
    }
    param.wr(name);
    param.nl();
    param.incIndent();
    visitList(type, param);
    param.decIndent();
  }

  protected void visitOptList(String name, List<? extends FunBase> type, StreamWriter param) {
    if (type.isEmpty()) {
      return;
    }
    param.wr(name);
    param.nl();
    param.incIndent();
    visitList(type, param);
    param.decIndent();
  }

  @Override
  protected Void visitNamespace(Namespace obj, StreamWriter param) {
    param.wr("namespace '");
    param.wr(obj.getName());
    param.wr("'");
    param.nl();
    param.incIndent();
    visitList(obj.getItems(), param);
    visitList(obj.getSpaces(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, StreamWriter param) {
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIfaceUse(IfaceUse obj, StreamWriter param) {
    param.wr(";");
    param.nl();
    return null;
  }

  private void compHeader(Component obj, StreamWriter param) {
    param.wr("Component");
    param.nl();

    param.incIndent();
    param.nl();
    visitInterfaceDecl("input", obj.getIface(Direction.in), param);
    visitInterfaceDecl("output", obj.getIface(Direction.out), param);
    param.decIndent();
  }

  @Override
  protected Void visitComponent(Component obj, StreamWriter param) {
    compHeader(obj, param);
    param.incIndent();
    super.visitComponent(obj, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, StreamWriter param) {
    param.wr("implementation elementary");
    param.nl();
    param.nl();

    visitOptList("const", obj.getConstant(), param);
    visitOptList("component", obj.getComponent(), param);
    visitOptList("var", obj.getVariable(), param);
    visitOptList(obj.getFunction().getName(), obj.getFunction(), param);

    param.wr("entry: ");
    visit(obj.getEntryFunc(), param);
    param.wr(";");
    param.nl();
    param.wr("exit: ");
    visit(obj.getExitFunc(), param);
    param.wr(";");
    param.nl();
    param.nl();

    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, StreamWriter param) {
    param.wr("implementation composition");
    param.nl();
    param.nl();

    visitOptList("component", obj.getComponent(), param);
    visitOptList("connection", obj.getConnection(), param);

    return null;
  }

  @Override
  protected Void visitConnection(Connection obj, StreamWriter param) {
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
    visit(obj.getEndpoint(Direction.out), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitFunctionHeader(FunctionHeader obj, StreamWriter param) {
    param.wr("function ");
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr("(");
    list(obj.getParam().getList(), "; ", param);
    param.wr(")");
    if (obj instanceof FuncWithReturn) {
      param.wr(":");
      visit(((FuncWithReturn) obj).getRet(), param);
    }
    if (obj instanceof FuncWithBody) {
      param.nl();
      param.incIndent();
      visit(((FuncWithBody) obj).getBody(), param);
      param.decIndent();
      param.wr("end");
      param.nl();
    } else {
      param.wr(";");
    }
    param.nl();
    return null;
  }

  @Override
  protected Void visitGenerator(Generator obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr("{");
    list(obj.getParam().getList(), "; ", param);
    param.wr("} = ");
    visit(obj.getItem(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  // ---- Type ----------------------------------------------------------------

  @Override
  protected Void visitNamedComponent(NamedComponent obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    visit(obj.getComp(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNamedInterface(NamedInterface obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    visit(obj.getIface(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNamedType(NamedType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    visit(obj.getType(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, StreamWriter param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, StreamWriter param) {
    param.wr("Enum");
    param.nl();
    param.incIndent();
    visitList(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, StreamWriter param) {
    param.wr(obj.getName().toString());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    writeNamedElementType(obj, "Union", param);
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, StreamWriter param) {
    writeNamedElementType(obj, "Record", param);
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" : ");
    visit(obj.getType(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  private void writeNamedElementType(NamedElementType obj, String typename, StreamWriter param) {
    param.wr(typename);
    param.nl();
    param.incIndent();
    visitList(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
  }

  @Override
  protected Void visitGenericRange(RangeTemplate obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRange(Range obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitArray(Array obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitGenericArray(ArrayTemplate obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitIntegerType(IntegerType obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitNaturalType(NaturalType obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitAnyType(AnyType obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitTypeType(TypeType obj, StreamWriter param) {
    param.wr(TypeTypeTemplate.NAME);
    param.wr("(");
    visit(obj.getType(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitGenericTypeType(TypeTypeTemplate obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  // ---- Statement -----------------------------------------------------------

  @Override
  protected Void visitBlock(Block obj, StreamWriter param) {
    visitList(obj.getStatements(), param);
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, StreamWriter param) {
    visit(obj.getLeft(), param);
    param.wr(" := ");
    visit(obj.getRight(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, StreamWriter param) {
    visit(obj.getVariable(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, StreamWriter param) {
    visit(obj.getCall(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, StreamWriter param) {
    param.wr("return ");
    visit(obj.getExpr(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, StreamWriter param) {
    param.wr("return;");
    param.nl();
    return null;
  }

  @Override
  protected Void visitWhile(While obj, StreamWriter param) {
    param.wr("while ");
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
  protected Void visitIfStmt(IfStmt obj, StreamWriter param) {
    assert (!obj.getOption().isEmpty());

    boolean first = true;
    for (IfOption itr : obj.getOption()) {
      if (first) {
        param.wr("if ");
        first = false;
      } else {
        param.wr("ef ");
      }
      visit(itr.getCondition(), param);
      param.wr(" then");
      param.nl();
      param.incIndent();
      visit(itr.getCode(), param);
      param.decIndent();
    }

    param.wr("else");
    param.nl();
    param.incIndent();
    visit(obj.getDefblock(), param);
    param.decIndent();
    param.wr("end");
    param.nl();

    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, StreamWriter param) {
    param.wr("case ");
    visit(obj.getCondition(), param);
    param.wr(" of");
    param.nl();
    param.incIndent();
    visitItr(obj.getOption(), param);
    if (!obj.getOtherwise().getStatements().isEmpty()) {
      param.wr("else");
      param.nl();
      param.incIndent();
      visit(obj.getOtherwise(), param);
      param.decIndent();
      param.wr("end");
      param.nl();
    }
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseOpt(CaseOpt obj, StreamWriter param) {
    list(obj.getValue(), ",", param);
    param.wr(":");
    param.nl();
    param.incIndent();
    visit(obj.getCode(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, StreamWriter param) {
    visit(obj.getStart(), param);
    param.wr("..");
    visit(obj.getEnd(), param);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, StreamWriter param) {
    visit(obj.getValue(), param);
    return null;
  }

  // ---- Expression ----------------------------------------------------------

  @Override
  protected Void visitArithmeticOp(ArithmeticOp obj, StreamWriter param) {
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
  protected Void visitUnaryExpression(UnaryExpression obj, StreamWriter param) {
    param.wr("(");
    param.wr(obj.getOp().toString());
    param.wr(" ");
    visit(obj.getExpr(), param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, StreamWriter param) {
    param.wr(obj.isValue() ? "True" : "False");
    return null;
  }

  @Override
  protected Void visitNumber(Number obj, StreamWriter param) {
    param.wr(obj.getValue().toString());
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, StreamWriter param) {
    param.wr("'");
    param.wr(obj.getValue());
    param.wr("'");
    return null;
  }

  @Override
  protected Void visitArrayValue(ArrayValue obj, StreamWriter param) {
    param.wr("[");
    list(obj.getValue(), ", ", param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitRelation(Relation obj, StreamWriter param) {
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
  protected Void visitVariable(Variable obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(": ");
    visit(obj.getType(), param);
    super.visitVariable(obj, param);
    return null;
  }

  @Override
  protected Void visitCompfuncParameter(TemplateParameter obj, StreamWriter param) {
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, StreamWriter param) {
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, StreamWriter param) {
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, StreamWriter param) {
    param.wr(" = ");
    visit(obj.getDef(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitReferenceUnlinked(ReferenceUnlinked obj, StreamWriter param) {
    param.wr("'");
    param.wr(obj.getName().toString("."));
    param.wr("'");
    visitItr(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, StreamWriter param) {
    param.wr(obj.getLink().getName());
    wrId(obj.getLink(), param);
    visitItr(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, StreamWriter param) {
    param.wr(".");
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, StreamWriter param) {
    param.wr("(");
    list(obj.getActualParameter(), ", ", param);
    param.wr(")");
    return null;
  }

  @Override
  protected Void visitRefCompcall(RefTemplCall obj, StreamWriter param) {
    param.wr("{");
    list(obj.getActualParameter(), ",", param);
    param.wr("}");
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, StreamWriter param) {
    param.wr("[");
    visit(obj.getIndex(), param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, StreamWriter param) {
    param.wr("implementation hfsm");
    param.nl();
    param.nl();

    visit(obj.getTopstate(), param);

    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, StreamWriter param) {
    param.wr("state ");
    param.wr(obj.getName());
    wrId(obj, param);
    param.nl();
    param.incIndent();
    printStateContent(obj, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, StreamWriter param) {
    param.wr("state ");
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr("(");
    visit(obj.getInitial(), param);
    param.wr(")");
    param.nl();
    param.incIndent();
    printStateContent(obj, param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  protected void printStateContent(State obj, StreamWriter param) {
    param.wr("entry: ");
    visit(obj.getEntryFuncRef(), param);
    param.wr(";");
    param.nl();
    param.wr("exit: ");
    visit(obj.getExitFuncRef(), param);
    param.wr(";");
    param.nl();
    param.nl();

    visitOptList("var", obj.getVariable(), param);
    visitList(obj.getBfunc(), param);
    visitList(obj.getItem(), param);
  }

  @Override
  protected Void visitTransition(Transition obj, StreamWriter param) {
    param.wr("transition");
    param.nl();
    param.incIndent();

    visit(obj.getSrc(), param);
    param.wr(" to ");
    visit(obj.getDst(), param);
    param.wr(" by ");
    visit(obj.getEvent(), param);
    param.wr("(");
    list(obj.getParam().getList(), "; ", param);
    param.wr(")");
    param.wr(" if ");
    visit(obj.getGuard(), param);
    param.wr(" do");
    param.nl();
    param.incIndent();
    visit(obj.getBody(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();

    param.decIndent();

    return null;
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, StreamWriter param) {
    param.wr("namespace '");
    param.wr(obj.getNamespace());
    param.wr("'");
    param.nl();
    param.incIndent();
    visit(obj.getFunc(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

}
