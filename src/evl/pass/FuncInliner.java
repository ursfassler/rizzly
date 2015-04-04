package evl.pass;

import java.util.List;

import pass.EvlPass;

import common.ElementInfo;
import common.Property;

import error.RError;
import evl.copy.Copy;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.expression.Expression;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.Reference;
import evl.data.function.Function;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.CallStmt;
import evl.data.statement.Return;
import evl.data.statement.Statement;
import evl.data.statement.VarDefStmt;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowBacklink;
import evl.knowledge.KnowUniqueName;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.ClassGetter;
import evl.traverser.other.StmtReplacer;

public class FuncInliner extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    FuncInlinerWorker worker = new FuncInlinerWorker(kb);
    worker.traverse(evl, null);
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
        // RError.err(ErrorType.Hint, param.getName() + " :: " + func.getName());
        assert (obj.call.offset.size() == 1);  // FIXME maybe not true in the future
        RefCall call = (RefCall) obj.call.offset.get(0);
        return inline(func, call.actualParameter);
      }
    }

    return null;
  }

  private boolean doInline(Function func) {
    if (!func.properties().containsKey(Property.Public) && !func.properties().containsKey(Property.Extern)) {
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

    List<Statement> ret = new EvlList<Statement>();

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
