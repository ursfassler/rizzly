package evl.pass;

import java.util.List;

import pass.EvlPass;

import common.ElementInfo;
import common.Property;

import error.RError;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.TupleValue;
import evl.expression.reference.RefCall;
import evl.expression.reference.Reference;
import evl.function.Function;
import evl.knowledge.KnowBacklink;
import evl.knowledge.KnowUniqueName;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.statement.AssignmentSingle;
import evl.statement.CallStmt;
import evl.statement.Return;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.traverser.ClassGetter;
import evl.traverser.StmtReplacer;
import evl.variable.FuncVariable;

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

    if (obj.getCall().getLink() instanceof Function) {
      assert (param != null);
      Function func = (Function) obj.getCall().getLink();

      List<Statement> fr = visit(func, param);
      assert (fr == null);

      if (doInline(func) && (func != param)) {
        // RError.err(ErrorType.Hint, param.getName() + " :: " + func.getName());
        assert (obj.getCall().getOffset().size() == 1);  // FIXME maybe not true in the future
        RefCall call = (RefCall) obj.getCall().getOffset().get(0);
        return inline(func, call.getActualParameter());
      }
    }

    return null;
  }

  private boolean doInline(Function func) {
    if (!func.properties().containsKey(Property.Public) && !func.properties().containsKey(Property.Extern)) {
      if (ClassGetter.get(Return.class, func.getBody()).isEmpty()) {
        int refCount = kbl.get(func).size();
        // assert(refCount > 0); //XXX why is this not true?

        if (refCount == 1) {
          return true;
        }

        if (func.getBody().getStatements().size() == 1) {
          return true;
        }
      }
    }
    return false;
  }

  private List<Statement> inline(Function func, TupleValue actualParameter) {
    RError.ass(func.getParam().size() == actualParameter.getValue().size(), "expected same number of arguments");

    func = Copy.copy(func);

    List<Statement> ret = new EvlList<Statement>();

    for (int i = 0; i < func.getParam().size(); i++) {
      FuncVariable inner = func.getParam().get(i);
      Expression arg = actualParameter.getValue().get(i);

      inner.setName(kun.get(inner.getName()));

      VarDefStmt def = new VarDefStmt(inner.getInfo(), inner);
      AssignmentSingle ass = new AssignmentSingle(arg.getInfo(), new Reference(ElementInfo.NO, inner), arg);

      ret.add(def);
      ret.add(ass);
    }

    ret.addAll(func.getBody().getStatements());

    return ret;
  }

}
