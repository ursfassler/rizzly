package evl.knowledge;

import common.Designator;
import common.ElementInfo;
import common.FuncAttr;

import evl.function.impl.FuncProtoVoid;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.variable.FuncVariable;

//TODO rename
//TODO rename trap to runtime exception and provide arguments
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
    final String NAME = Designator.NAME_SEP + "trap";

    FuncProtoVoid ret = (FuncProtoVoid) findItem(NAME);
    if (ret == null) {
      ret = new FuncProtoVoid(info, NAME, new ListOfNamed<FuncVariable>());
      ret.getAttributes().add(FuncAttr.Extern);
      ret.getAttributes().add(FuncAttr.Public);
      addItem(ret);
    }
    return ret;
  }

}
