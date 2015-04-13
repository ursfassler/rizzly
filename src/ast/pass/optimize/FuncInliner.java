package ast.pass.optimize;

import java.util.List;
import java.util.Set;

import util.Pair;
import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.TupleValue;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.CallStmt;
import ast.data.statement.Return;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.variable.FuncVariable;
import ast.doc.SimpleGraph;
import ast.knowledge.KnowBacklink;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.helper.GraphHelper;
import ast.traverser.other.CallgraphMaker;
import ast.traverser.other.ClassGetter;
import ast.traverser.other.StmtReplacer;
import error.RError;

public class FuncInliner extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    SimpleGraph<Ast> callGraph = CallgraphMaker.make(ast, kb);
    GraphHelper.doTransitiveClosure(callGraph);
    Set<Pair<Ast, Ast>> callset = callGraph.edgeSet();

    FuncInlinerWorker worker = new FuncInlinerWorker(callset, kb);
    worker.traverse(ast, null);
  }
}

class FuncInlinerWorker extends StmtReplacer<Void> {
  private final KnowBacklink kbl;
  private final KnowUniqueName kun;
  private final Set<Pair<Ast, Ast>> callset;

  public FuncInlinerWorker(Set<Pair<Ast, Ast>> callset, KnowledgeBase kb) {
    super();
    kbl = kb.getEntry(KnowBacklink.class);
    kun = kb.getEntry(KnowUniqueName.class);
    this.callset = callset;
  }

  @Override
  protected List<Statement> visitCallStmt(CallStmt obj, Void param) {
    if (obj.call.link instanceof Function) {
      Function func = (Function) obj.call.link;

      List<Statement> fr = visit(func, param);
      assert (fr == null);

      if (canInline(func)) {
        assert (obj.call.offset.size() == 1);
        RefCall call = (RefCall) obj.call.offset.get(0);
        return inline(func, call.actualParameter);
      }
    }

    return null;
  }

  private boolean canInline(Function func) {
    return isPrivate(func) && hasNoReturnStatements(func) && hasFewStatements(func) && onlyOnceUsed(func) && isNotRecursive(func);
  }

  private boolean isNotRecursive(Function func) {
    Pair<Function, Function> selfCall = new Pair<Function, Function>(func, func);
    return !callset.contains(selfCall);
  }

  private boolean onlyOnceUsed(Function func) {
    int refCount = kbl.get(func).size();
    return refCount <= 1;
  }

  private boolean hasFewStatements(Function func) {
    return func.body.statements.size() <= 1;
  }

  private boolean isPrivate(Function func) {
    return func.property == FunctionProperty.Private;
  }

  private boolean hasNoReturnStatements(Function func) {
    return ClassGetter.getRecursive(Return.class, func.body).isEmpty();
  }

  private List<Statement> inline(Function func, TupleValue actualParameter) {
    RError.ass(func.param.size() == actualParameter.value.size(), "expected same number of arguments");

    func = Copy.copy(func);

    List<Statement> ret = new AstList<Statement>();

    for (int i = 0; i < func.param.size(); i++) {
      FuncVariable inner = func.param.get(i);
      Expression arg = actualParameter.value.get(i);

      inner.name = kun.get(inner.name);

      VarDefStmt def = new VarDefStmt(inner.getInfo(), inner);
      AssignmentSingle ass = new AssignmentSingle(arg.getInfo(), new Reference(ElementInfo.NO, inner), arg);

      ret.add(def);
      ret.add(ass);
    }

    ret.addAll(func.body.statements);

    return ret;
  }

}
