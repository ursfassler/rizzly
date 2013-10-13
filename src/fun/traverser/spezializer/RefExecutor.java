package fun.traverser.spezializer;

import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefTemplCall;
import fun.function.impl.FuncGlobal;
import fun.generator.TypeGenerator;
import fun.knowledge.KnowledgeBase;
import fun.traverser.Memory;

public class RefExecutor extends NullTraverser<Fun, RefItem> {

  private KnowledgeBase kb;

  public RefExecutor(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Expression visitDefault(Fun obj, RefItem param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitFuncGlobal(FuncGlobal obj, RefItem param) {
    assert (param instanceof RefCall);
    return StmtExecutor.process(obj, ((RefCall) param).getActualParameter(), new Memory(), kb);
  }

  @Override
  protected Fun visitTypeGenerator(TypeGenerator obj, RefItem param) {
    assert (param instanceof RefTemplCall);
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

}
