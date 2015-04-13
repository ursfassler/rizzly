package ast.pass.others;

import java.util.List;

import ast.ElementInfo;
import ast.copy.Copy;
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
import ast.knowledge.KnowBacklink;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.other.ClassGetter;
import ast.traverser.other.StmtReplacer;
import error.RError;

public class FuncInliner extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    FuncInlinerWorker worker = new FuncInlinerWorker(kb);
    worker.traverse(ast, null);
  }
}

class FuncInlinerWorker extends StmtReplacer<Function> {
  private final KnowBacklink kbl;
  private final KnowUniqueName kun;

  public FuncInlinerWorker(KnowledgeBase kb) {
    super();
    kbl = kb.getEntry(KnowBacklink.class);
    kun = kb.getEntry(KnowUniqueName.class);
  }

  @Override
  protected List<Statement> visitFunction(Function obj, Function param) {
    if (obj == param) {
      // recursion
      return null;
    }
    return super.visitFunction(obj, obj);
  }

  @Override
  protected List<Statement> visitCallStmt(CallStmt obj, Function param) {
    // TODO also if a function is called in a expression

    if (obj.call.link instanceof Function) {
      assert (param != null);
      Function func = (Function) obj.call.link;

      List<Statement> fr = visit(func, param);
      assert (fr == null);

      if (doInline(func) && (func != param)) {
        // RError.err(ErrorType.Hint, param.getName() + " :: " +
        // func.getName());
        assert (obj.call.offset.size() == 1); // FIXME maybe not true in the
                                              // future
        RefCall call = (RefCall) obj.call.offset.get(0);
        return inline(func, call.actualParameter);
      }
    }

    return null;
  }

  private boolean doInline(Function func) {
    if (func.property == FunctionProperty.Private) {
      if (ClassGetter.getRecursive(Return.class, func.body).isEmpty()) {
        int refCount = kbl.get(func).size();
        // assert(refCount > 0); //XXX why is this not true?

        if (refCount == 1) {
          return true;
        }

        if (func.body.statements.size() == 1) {
          return true;
        }
      }
    }
    return false;
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
