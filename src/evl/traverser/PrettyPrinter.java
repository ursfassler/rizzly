package evl.traverser;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import util.StreamWriter;

import common.Direction;
import common.FuncAttr;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.EvlBase;
import evl.NullTraverser;
import evl.composition.Connection;
import evl.composition.EndpointSelf;
import evl.composition.EndpointSub;
import evl.composition.ImplComposition;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.binop.And;
import evl.expression.binop.BinaryExp;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.RefPtrDeref;
import evl.expression.reference.Reference;
import evl.expression.unop.Not;
import evl.expression.unop.Uminus;
import evl.function.FuncIface;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.hfsm.HfsmQueryFunction;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumDefRef;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionSelector;
import evl.type.composed.UnionType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.PointerType;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

public class PrettyPrinter extends NullTraverser<Void, StreamWriter> {

  private boolean writeId;

  public PrettyPrinter(boolean writeId) {
    super();
    this.writeId = writeId;
  }

  public static void print(Evl ast, String filename, boolean writeId) {
    PrettyPrinter pp = new PrettyPrinter(writeId);
    try {
      pp.traverse(ast, new StreamWriter(new PrintStream(filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected Void visitDefault(Evl obj, StreamWriter param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  private void list(List<? extends Evl> list, String sep, StreamWriter param) {
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        param.wr(sep);
      }
      visit(list.get(i), param);
    }
  }

  private void visitList(List<? extends Evl> list, StreamWriter param) {
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

  private void wrId(Evl obj, StreamWriter wr) {
    if (writeId) {
      wr.wr("[" + obj.hashCode() % 10000 + "]");
    }
  }

  @Override
  protected Void visitRizzlyProgram(RizzlyProgram obj, StreamWriter param) {
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

  private void visitInterfaceDecl(String type, ListOfNamed<? extends FuncIface> listOfNamed, StreamWriter param) {
    if (!listOfNamed.isEmpty()) {
      param.wr(type);
      param.nl();
      param.incIndent();
      visitList(listOfNamed, param);
      param.decIndent();
      param.nl();
    }
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

  protected void visitOptList(String name, List<? extends EvlBase> type, StreamWriter param) {
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
    wrId(obj, param);
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
  protected Void visitNamedList(NamedList<Named> obj, StreamWriter param) {
    param.wr("namespace ");
    param.wr(obj.getName());
    wrId(obj, param);
    param.nl();
    param.incIndent();
    visitList(obj.getList(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(": ");
    param.wr(obj.getLink().getName());
    wrId(obj.getLink(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  private void compHeader(Component obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = Component");
    param.nl();

    param.incIndent();
    param.nl();
    visitInterfaceDecl("input", obj.getInput(), param);
    visitInterfaceDecl("output", obj.getOutput(), param);
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

    param.wr("entry: ");
    visit(obj.getEntryFunc(), param);
    param.wr(";");
    param.nl();
    param.wr("exit: ");
    visit(obj.getExitFunc(), param);
    param.wr(";");
    param.nl();
    param.nl();

    visitList(obj.getSubComCallback(), param);
    visitList(obj.getInternalFunction(), param);
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
  protected Void visitFunctionBase(FunctionBase obj, StreamWriter param) {
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
    for (FuncAttr attr : obj.getAttributes()) {
      param.wr(" ");
      param.wr(attr.toString());
    }
    param.nl();
    if (obj instanceof FuncWithBody) {
      param.incIndent();
      visit(((FuncWithBody) obj).getBody(), param);
      param.decIndent();
      param.wr("end");
      param.nl();
    }
    param.nl();
    return null;
  }

  // ---- Type ----------------------------------------------------------------
  @Override
  protected Void visitEnumType(EnumType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = Enum");
    param.nl();
    param.incIndent();
    visitItr(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumDefRef(EnumDefRef obj, StreamWriter param) {
    param.wr(obj.getElem().getName());
    wrId(obj.getElem(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitEnumElement(EnumElement obj, StreamWriter param) {
    param.wr(obj.getName().toString());
    wrId(obj, param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitUnionSelector(UnionSelector obj, StreamWriter param) {
    param.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = Union(");
    visit(obj.getSelector(), param);
    param.wr(")");
    param.nl();
    param.incIndent();
    visitList(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = Record");
    param.nl();
    param.incIndent();
    visitList(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" : ");
    visit(obj.getType(), param);
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = False..True");
    param.nl();
    return null;
  }

  @Override
  protected Void visitPointerType(PointerType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    visit(obj.getType(), param);
    param.wr("^");
    param.nl();
    return null;
  }

  @Override
  protected Void visitRangeValue(RangeValue obj, StreamWriter param) {
    param.wr(obj.toString());
    return null;
  }

  @Override
  protected Void visitNumSet(RangeType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr(obj.getNumbers().getLow().toString());
    param.wr("..");
    param.wr(obj.getNumbers().getHigh().toString());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIntegerType(IntegerType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("ℤ");
    param.nl();
    return null;
  }

  @Override
  protected Void visitNaturalType(NaturalType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("ℕ");
    param.nl();
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("*");
    param.nl();
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr("∅");
    param.nl();
    return null;
  }

  @Override
  protected Void visitArrayType(ArrayType obj, StreamWriter param) {
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr(obj.getSize().toString());
    param.wr(" * ");
    visit(obj.getType(), param);
    param.nl();
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
  protected Void visitWhileStmt(WhileStmt obj, StreamWriter param) {
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
  protected Void visitBinaryExp(BinaryExp obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(" ");
    param.wr(obj.getOpName());
    param.wr(" ");
    visit(obj.getRight(), param);
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
  protected Void visitNot(Not obj, StreamWriter param) {
    param.wr("not ");
    visit(obj.getExpr(), param);
    return null;
  }

  @Override
  protected Void visitUminus(Uminus obj, StreamWriter param) {
    param.wr("- ");
    visit(obj.getExpr(), param);
    return null;
  }

  private void visitBinop(String op, BinaryExp obj, StreamWriter param) {
    param.wr("(");
    visit(obj.getLeft(), param);
    param.wr(" ");
    param.wr(op);
    param.wr(" ");
    visit(obj.getRight(), param);
    param.wr(")");
  }

  @Override
  protected Void visitAnd(And obj, StreamWriter param) {
    visitBinop("and", obj, param);
    return null;
  }

  @Override
  protected Void visitDiv(Div obj, StreamWriter param) {
    visitBinop("/", obj, param);
    return null;
  }

  @Override
  protected Void visitEqual(Equal obj, StreamWriter param) {
    visitBinop("=", obj, param);
    return null;
  }

  @Override
  protected Void visitGreater(Greater obj, StreamWriter param) {
    visitBinop(">", obj, param);
    return null;
  }

  @Override
  protected Void visitGreaterequal(Greaterequal obj, StreamWriter param) {
    visitBinop(">=", obj, param);
    return null;
  }

  @Override
  protected Void visitLess(Less obj, StreamWriter param) {
    visitBinop("<", obj, param);
    return null;
  }

  @Override
  protected Void visitLessequal(Lessequal obj, StreamWriter param) {
    visitBinop("<=", obj, param);
    return null;
  }

  @Override
  protected Void visitMinus(Minus obj, StreamWriter param) {
    visitBinop("-", obj, param);
    return null;
  }

  @Override
  protected Void visitMod(Mod obj, StreamWriter param) {
    visitBinop("mod", obj, param);
    return null;
  }

  @Override
  protected Void visitMul(Mul obj, StreamWriter param) {
    visitBinop("*", obj, param);
    return null;
  }

  @Override
  protected Void visitNotequal(Notequal obj, StreamWriter param) {
    visitBinop("<>", obj, param);
    return null;
  }

  @Override
  protected Void visitOr(Or obj, StreamWriter param) {
    visitBinop("or", obj, param);
    return null;
  }

  @Override
  protected Void visitPlus(Plus obj, StreamWriter param) {
    visitBinop("+", obj, param);
    return null;
  }

  @Override
  protected Void visitShl(Shl obj, StreamWriter param) {
    visitBinop("shl", obj, param);
    return null;
  }

  @Override
  protected Void visitShr(Shr obj, StreamWriter param) {
    visitBinop("shr", obj, param);
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
  protected Void visitTypeRef(TypeRef obj, StreamWriter param) {
    param.wr(obj.getRef().getName());
    wrId(obj.getRef(), param);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, StreamWriter param) {
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
  protected Void visitRefIndex(RefIndex obj, StreamWriter param) {
    param.wr("[");
    visit(obj.getIndex(), param);
    param.wr("]");
    return null;
  }

  @Override
  protected Void visitRefPtrDeref(RefPtrDeref obj, StreamWriter param) {
    param.wr("^");
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
    param.wr(obj.getInitial().getName());
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
    param.wr("entry ");
    visit(obj.getEntryFunc(), param);
    param.wr(";");
    param.nl();
    param.wr("entry ");
    visit(obj.getExitFunc(), param);
    param.wr(";");
    param.nl();
    param.nl();

    visitOptList("var", obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    visitList(obj.getItem(), param);
  }

  @Override
  protected Void visitTransition(Transition obj, StreamWriter param) {
    param.wr("transition");
    param.nl();
    param.incIndent();

    param.wr(obj.getSrc().getName());
    param.wr(" to ");
    param.wr(obj.getDst().getName());
    param.wr(" by ");
    visit(obj.getEventFunc(), param);
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
  protected Void visitHfsmQueryFunction(HfsmQueryFunction obj, StreamWriter param) {
    param.wr("query ");
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr("(");
    list(obj.getParam().getList(), "; ", param);
    param.wr(")");
    if (obj instanceof FuncWithReturn) {
      param.wr(":");
      visit(((FuncWithReturn) obj).getRet(), param);
    }
    for (FuncAttr attr : obj.getAttributes()) {
      param.wr(" ");
      param.wr(attr.toString());
    }
    param.nl();
    if (obj instanceof FuncWithBody) {
      param.incIndent();
      visit(((FuncWithBody) obj).getBody(), param);
      param.decIndent();
      param.wr("end");
      param.nl();
    }
    param.nl();
    return null;
  }

  @Override
  protected Void visitEndpointSelf(EndpointSelf obj, StreamWriter param) {
    param.wr(obj.getIface().getName());
    return null;
  }

  @Override
  protected Void visitEndpointSub(EndpointSub obj, StreamWriter param) {
    param.wr(obj.getComp().getName());
    param.wr(".");
    param.wr(obj.getIface());
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, StreamWriter param) {
    visit(obj.getCast(), param);
    param.wr("(");
    visit(obj.getValue(), param);
    param.wr(")");
    return null;
  }

}
