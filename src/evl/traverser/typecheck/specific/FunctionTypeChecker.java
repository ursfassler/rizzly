package evl.traverser.typecheck.specific;

import evl.Evl;
import evl.NullTraverser;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.TypeChecker;
import evl.type.Type;
import evl.variable.Variable;

public class FunctionTypeChecker extends NullTraverser<Void, Void> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;

  public FunctionTypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static void process(FunctionBase obj, KnowledgeBase kb) {
    FunctionTypeChecker check = new FunctionTypeChecker(kb);
    check.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void sym) {
    for (Variable param : obj.getParam()) {
      TypeChecker.process(param, kb, sym);
    }
    if (obj instanceof FuncWithBody) {
      Type ret;
      if (obj instanceof FuncWithReturn) {
        ret = ((FuncWithReturn) obj).getRet().getRef();
      } else {
        ret = kbi.getVoidType();
      }
      StatementTypeChecker.process(((FuncWithBody) obj).getBody(), ret, kb);
    }

    return null;
  }
}
