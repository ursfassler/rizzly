package evl.traverser.debug;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.function.impl.FuncPrivateVoid;
import evl.other.IfaceUse;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.Namespace;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.type.Type;
import evl.variable.ConstGlobal;

/**
 * Inserts a message call whenever an event is sent
 *
 * @author urs
 *
 */
public class EventSendDebugCallAdder extends NullTraverser<Void, Void> {

  private StmtTraverser st;

  public EventSendDebugCallAdder(FuncPrivateVoid debugSend, ArrayList<String> names) {
    super();
    st = new StmtTraverser(debugSend, names);
  }

  public static void process(Evl obj, ArrayList<String> names, FuncPrivateVoid debugSend) {
    EventSendDebugCallAdder reduction = new EventSendDebugCallAdder(debugSend, names);
    reduction.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    if( !( obj instanceof Type ) ) {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
    return null;
  }

  @Override
  protected Void visitConstGlobal(ConstGlobal obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamedList(NamedList<Named> obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    visitItr(obj.getInternalFunction(), null);
    visitItr(obj.getInputFunc(), null);
    visitItr(obj.getSubComCallback(), null);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    st.traverse(obj, null);
    return null;
  }
}

class StmtTraverser extends DefTraverser<Void, List<Statement>> {

  private FuncPrivateVoid debugSend;
  private ArrayList<String> names;
  static private ElementInfo info = new ElementInfo();

  public StmtTraverser(FuncPrivateVoid debugSend, ArrayList<String> names) {
    super();
    this.debugSend = debugSend;
    this.names = names;
  }

  @Override
  protected Void visitBlock(Block obj, List<Statement> param) {
    List<Statement> sl = new ArrayList<Statement>();
    for( Statement stmt : obj.getStatements() ) {
      visit(stmt, sl);
      sl.add(stmt);
    }
    obj.getStatements().clear();
    obj.getStatements().addAll(sl);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, List<Statement> param) {
    super.visitReference(obj, param);

    for( int i = 0; i < obj.getOffset().size(); i++ ) {
      RefItem ref = obj.getOffset().get(i);
      if( ref instanceof RefCall ) {
        assert ( i == obj.getOffset().size() - 1 );
        if( obj.getLink() instanceof IfaceUse ) {
          assert ( i == 1 );
          IfaceUse use = (IfaceUse) obj.getLink();
          String funcName = ( (RefName) obj.getOffset().get(0) ).getName();

          int numIface = names.indexOf(use.getName());
          if( numIface >= 0 ) {
            int numFunc = names.indexOf(funcName);
            assert ( numIface >= 0 );
            assert ( numFunc >= 0 );

            param.add(makeCall(debugSend, numFunc, numIface));
          } else {
            assert ( use.getName().equals("_debug") );
          }
        }
      }
    }
    return null;
  }

  private CallStmt makeCall(FuncPrivateVoid func, int numFunc, int numIface) {
    // Self._sendMsg( numFunc, numIface );
    List<Expression> actParam = new ArrayList<Expression>();
    actParam.add(new Number(info, BigInteger.valueOf(numFunc)));
    actParam.add(new Number(info, BigInteger.valueOf(numIface)));

    Reference call = new Reference(info, func);
    call.getOffset().add(new RefCall(info, actParam));

    return new CallStmt(info, call);
  }
}
