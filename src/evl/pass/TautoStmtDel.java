package evl.pass;

import java.util.List;

import pass.EvlPass;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.expression.BoolValue;
import evl.data.statement.IfOption;
import evl.data.statement.IfStmt;
import evl.data.statement.Statement;
import evl.knowledge.KnowConst;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.StmtReplacer;

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

    for (IfOption opt : obj.option) {
      if (kc.isConst(opt.condition)) {
        BoolValue value = (BoolValue) opt.condition;
        if (value.value) {
          obj.defblock = opt.code;
          break;
        }
      } else {
        keep.add(opt);
      }
    }

    if (keep.isEmpty()) {
      List<Statement> ret = new EvlList<Statement>();
      ret.add(obj.defblock);
      return ret;
    } else {
      obj.option.clear();
      obj.option.addAll(keep);
      return null;
    }
  }

}
