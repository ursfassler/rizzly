package evl.knowledge;

import common.ElementInfo;
import common.FuncAttr;

import evl.function.impl.FuncProtoVoid;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.variable.Variable;

public class KnowLlvmLibrary extends KnowledgeEntry {
  private static final ElementInfo info = new ElementInfo();
  private KnowledgeBase kb;
  private KnowBaseItem kbi;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  private Named findItem(String name) {
    return kb.getRoot().find(name);
  }

  private void addItem(Named item) {
    assert (findItem(item.getName()) == null);
    kb.getRoot().add(item);

  }

  public FuncProtoVoid getTrap() {
    // declare void @llvm.trap() noreturn nounwind

    final String NAME = "llvm.trap";

    FuncProtoVoid ret = (FuncProtoVoid) findItem(NAME);
    if (ret == null) {
      ret = new FuncProtoVoid(info, NAME, new ListOfNamed<Variable>());
      ret.getAttributes().add(FuncAttr.LlvmNoreturn);
      ret.getAttributes().add(FuncAttr.LlvmNounwind);
      addItem(ret);
    }
    return ret;
  }

}
