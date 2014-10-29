package evl.traverser.typecheck.specific;

import evl.function.Function;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.TypeChecker;
import evl.type.Type;
import evl.variable.Variable;

public class FunctionTypeChecker {

  public static void process(Function obj, KnowledgeBase kb) {
    for (Variable param : obj.getParam()) {
      TypeChecker.process(param, kb);
    }
    Type ret;
    ret = obj.getRet().getLink();
    StatementTypeChecker.process(obj.getBody(), ret, kb);
  }

}
