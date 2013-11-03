package evl.traverser;

import java.util.ArrayList;
import java.util.List;

import evl.DefTraverser;
import evl.other.RizzlyProgram;
import evl.statement.Block;
import evl.statement.IfOption;
import evl.statement.IfStmt;

/**
 * Ensures that the if statement has at most one option
 * 
 */
public class IfCutter extends DefTraverser<Void, Void> {
  static final private IfCutter INSTANCE = new IfCutter();

  public static void process(RizzlyProgram prg) {
    INSTANCE.traverse(prg, null);
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, Void param) {
    if (obj.getOption().size() > 1) {
      int optcount = obj.getOption().size();
      IfOption first = obj.getOption().get(0);
      List<IfOption> opt = new ArrayList<IfOption>(obj.getOption());
      obj.getOption().clear();
      obj.getOption().add(first);
      opt.remove(0);
      assert (obj.getOption().size() + opt.size() == optcount);

      IfStmt nif = new IfStmt(opt.get(0).getInfo(), opt, obj.getDefblock());
      Block newElse = new Block(obj.getInfo());
      newElse.getStatements().add(nif);
      obj.setDefblock(newElse);
    }
    return super.visitIfStmt(obj, param);
  }

}
