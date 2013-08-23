package evl.doc;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import common.Direction;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.EvlBase;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.CaseGoto;
import evl.cfg.CaseGotoOpt;
import evl.cfg.CaseOptRange;
import evl.cfg.CaseOptValue;
import evl.cfg.Goto;
import evl.cfg.IfGoto;
import evl.cfg.PhiStmt;
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
import evl.expression.TypeCast;
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
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
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
import evl.statement.VarDefInitStmt;
import evl.statement.VarDefStmt;
import evl.type.TypeRef;
import evl.type.base.BaseType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.StringType;
import evl.type.base.TypeAlias;
import evl.type.composed.NamedElement;
import evl.type.composed.NamedElementType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
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

  private void visitInterfaceDecl(String type, ListOfNamed<? extends Named> listOfNamed, StreamWriter param) {
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
  protected Void visitInterface(Interface obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" = Interface");
    wrId(obj, param);
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

  @Override
  protected Void visitIfaceUse(IfaceUse obj, StreamWriter param) {
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

    param.wr("entry: ");
    visit(obj.getEntryFunc(), param);
    param.wr(";");
    param.nl();
    param.wr("exit: ");
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
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = Enum");
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
    param.wr(obj.getName());
    wrId(obj, param);
    param.wr(" = ");
    param.wr(typename);
    param.nl();
    param.incIndent();
    visitList(obj.getElement(), param);
    param.decIndent();
    param.wr("end");
    param.nl();
    param.nl();
  }

  @Override
  protected Void visitBaseType(BaseType obj, StreamWriter param) {
    param.wr(obj.getName());
    param.wr(" = !internal;");
    param.nl();
    return null;
  }

  // ---- Statement -----------------------------------------------------------

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
  protected Void visitVarDefInitStmt(VarDefInitStmt obj, StreamWriter param) {
    visit(obj.getVariable(), param);
    param.wr(" = ");
    visit(obj.getInit(), param);
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
  protected Void visitFuncVariable(FuncVariable obj, StreamWriter param) {
    return null;
  }

  @Override
  protected Void visitSsaVariable(SsaVariable obj, StreamWriter param) {
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
    param.wr(obj.getEventIface().getName());
    param.wr(".");
    param.wr(obj.getEventFunc());
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
  protected Void visitBasicBlock(BasicBlock obj, StreamWriter param) {
    param.wr(obj.toString());
    param.nl();
    param.incIndent();
    visitItr(obj.getPhi(), param);
    param.wr("--");
    param.nl();
    visitItr(obj.getCode(), param);
    param.wr("--");
    param.nl();
    visit(obj.getEnd(), param);
    param.decIndent();
    param.nl();
    return null;
  }

  @Override
  protected Void visitGoto(Goto obj, StreamWriter param) {
    param.wr("goto ");
    param.wr(obj.getTarget().toString());
    param.wr(";");
    param.nl();
    return null;
  }

  @Override
  protected Void visitIfGoto(IfGoto obj, StreamWriter param) {
    param.wr("if ");
    visit(obj.getCondition(), param);
    param.nl();
    param.incIndent();
    param.wr("then goto ");
    param.wr(obj.getThenBlock().toString());
    param.nl();
    param.wr("else goto ");
    param.wr(obj.getElseBlock().toString());
    param.nl();
    param.decIndent();
    return null;
  }

  @Override
  protected Void visitCaseGoto(CaseGoto obj, StreamWriter param) {
    param.wr("case ");
    visit(obj.getCondition(), param);
    param.wr(" of");
    param.nl();

    param.incIndent();
    visitItr(obj.getOption(), param);

    param.wr("else goto ");
    param.wr(obj.getOtherwise().toString());
    param.nl();

    param.decIndent();
    param.wr("end");
    param.nl();
    return null;
  }

  @Override
  protected Void visitCaseGotoOpt(CaseGotoOpt obj, StreamWriter param) {
    list(obj.getValue(), ",", param);
    param.wr(": goto ");
    param.wr(obj.getDst().toString());
    param.nl();
    return null;
  }

  @Override
  protected Void visitBasicBlockList(BasicBlockList obj, StreamWriter param) {
    visit(obj.getEntry(), param);
    visit(obj.getExit(), param);
    LinkedList<BasicBlock> bbs = new LinkedList<BasicBlock>(obj.getBasicBlocks());
    Collections.sort(bbs, new Comparator<BasicBlock>() {
      @Override
      public int compare(BasicBlock o1, BasicBlock o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    visitItr(bbs, param);
    return null;
  }

  @Override
  protected Void visitPhiStmt(PhiStmt obj, StreamWriter param) {
    visit(obj.getVariable(), param);
    param.wr(" = phi(");
    ArrayList<BasicBlock> args = new ArrayList<BasicBlock>(obj.getInBB());
    Collections.sort(args, new Comparator<BasicBlock>() {
      @Override
      public int compare(BasicBlock arg0, BasicBlock arg1) {
        return arg0.getName().compareTo(arg1.getName()); // TODO correct phi arg ordering?
      }
    });
    for (int i = 0; i < args.size(); i++) {
      if (i > 0) {
        param.wr(",");
      }
      BasicBlock bb = args.get(i);
      Variable var = obj.getArg(bb);
      param.wr(bb.toString());
      param.wr(":");
      param.wr(var.getName());
      wrId(var, param);
    }
    param.wr(");");
    param.nl();
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, StreamWriter param) {
    visit( obj.getRef(), param );
    param.wr(" to ");
    visit(obj.getCast(), param);
    return null;
  }

}
