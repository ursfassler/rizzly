package evl.pass;

import java.util.List;

import pass.EvlPass;
import evl.expression.BoolValue;
import evl.knowledge.KnowConst;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.Statement;
import evl.traverser.StmtReplacer;

public class TautoStmtDel extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    TautoStmtDelWorker worker = new TautoStmtDelWorker(kb);
    worker.traverse(evl, null);
  }

}

class TautoStmtDelWorker extends StmtReplacer<Void> {
  private final KnowConst kc;

  public TautoStmtDelWorker(KnowledgeBase kb) {
    super();
    this.kc = kb.getEntry(KnowConst.class);
  }

  @Override
  protected List<Statement> visitIfStmt(IfStmt obj, Void param) {
    super.visitIfStmt(obj, param);

    EvlList<IfOption> keep = new EvlList<IfOption>();

    for (IfOption opt : obj.getOption()) {
      if (kc.isConst(opt.getCondition())) {
        BoolValue value = (BoolValue) opt.getCondition();
        if (value.isValue()) {
          obj.setDefblock(opt.getCode());
          break;
        }
      } else {
        keep.add(opt);
      }
    }

    if (keep.isEmpty()) {
      List<Statement> ret = new EvlList<Statement>();
      ret.add(obj.getDefblock());
      return ret;
    } else {
      obj.getOption().clear();
      obj.getOption().addAll(keep);
      return null;
    }
  }

}
