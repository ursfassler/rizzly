package ast.pass.optimize;

import java.util.List;
import java.util.Set;

import util.Pair;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.value.TupleValue;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefCall;
import ast.data.reference.RefFactory;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.CallStmt;
import ast.data.statement.Return;
import ast.data.statement.Statement;
import ast.data.statement.VarDefStmt;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.other.CallgraphMaker;
import ast.dispatcher.other.StmtReplacer;
import ast.doc.SimpleGraph;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.helper.GraphHelper;
import ast.repository.query.Match;
import ast.repository.query.Referencees.ReferenceesFactory;
import ast.repository.query.Referencees.ReferenceesReader;
import ast.specification.IsClass;
import error.RError;

public class FuncInliner implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    SimpleGraph<Ast> callGraph = CallgraphMaker.make(ast, kb);
    GraphHelper.doTransitiveClosure(callGraph);
    Set<Pair<Ast, Ast>> callset = callGraph.edgeSet();

    ReferenceesReader referencees = (new ReferenceesFactory()).produce(ast);

    FuncInlinerWorker worker = new FuncInlinerWorker(callset, referencees, kb);
    worker.traverse(ast, null);
  }
}

class FuncInlinerWorker extends StmtReplacer<Void> {
  private final ReferenceesReader referencees;
  private final KnowUniqueName kun;
  private final Set<Pair<Ast, Ast>> callset;

  public FuncInlinerWorker(Set<Pair<Ast, Ast>> callset, ReferenceesReader referencees, KnowledgeBase kb) {
    kun = kb.getEntry(KnowUniqueName.class);
    this.callset = callset;
    this.referencees = referencees;
  }

  @Override
  protected List<Statement> visitCallStmt(CallStmt obj, Void param) {
    OffsetReference callref = (OffsetReference) obj.call;
    LinkedAnchor anchor = (LinkedAnchor) callref.getAnchor();

    if (anchor.getLink() instanceof Function) {
      Function func = (Function) anchor.getLink();

      List<Statement> fr = visit(func, param);
      assert (fr == null);

      if (canInline(func)) {
        assert (callref.getOffset().size() == 1);
        RefCall call = (RefCall) callref.getOffset().get(0);
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
    int refCount = referencees.getReferencees(func).size();
    return refCount <= 1;
  }

  private boolean hasFewStatements(Function func) {
    return func.body.statements.size() <= 1;
  }

  private boolean isPrivate(Function func) {
    return func.property == FunctionProperty.Private;
  }

  private boolean hasNoReturnStatements(Function func) {
    return Match.hasNoItem(func.body, new IsClass(Return.class));
  }

  private List<Statement> inline(Function func, TupleValue actualParameter) {
    RError.ass(func.param.size() == actualParameter.value.size(), "expected same number of arguments");

    func = Copy.copy(func);

    List<Statement> ret = new AstList<Statement>();

    for (int i = 0; i < func.param.size(); i++) {
      FunctionVariable inner = func.param.get(i);
      Expression arg = actualParameter.value.get(i);

      inner.setName(kun.get(inner.getName()));

      VarDefStmt def = new VarDefStmt(inner);
      def.metadata().add(inner.metadata());
      AssignmentSingle ass = new AssignmentSingle(RefFactory.withOffset(inner), arg);
      ass.metadata().add(arg.metadata());

      ret.add(def);
      ret.add(ass);
    }

    ret.addAll(func.body.statements);

    return ret;
  }

}
