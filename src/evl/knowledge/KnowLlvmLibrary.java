package evl.knowledge;

import common.Designator;
import common.ElementInfo;
import common.Property;

import evl.Evl;
import evl.expression.reference.SimpleRef;
import evl.function.header.FuncCtrlOutDataOut;
import evl.other.EvlList;
import evl.other.Named;
import evl.statement.Block;
import evl.type.Type;
import evl.variable.FuncVariable;

//TODO rename
//TODO rename trap to runtime exception and provide arguments
public class KnowLlvmLibrary extends KnowledgeEntry {
  private static final ElementInfo info = ElementInfo.NO;
  private KnowledgeBase kb;
  private KnowBaseItem kbi;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  private Evl findItem(String name) {
    return kb.getRoot().getChildren().find(name);
  }

  private void addItem(Named item) {
    kb.getRoot().getChildren().add(item);
  }

  public FuncCtrlOutDataOut getTrap() {
    final String NAME = Designator.NAME_SEP + "trap";

    FuncCtrlOutDataOut ret = (FuncCtrlOutDataOut) findItem(NAME);

    if (ret == null) {
      ret = new FuncCtrlOutDataOut(info, NAME, new EvlList<FuncVariable>(), new SimpleRef<Type>(info, kbi.getVoidType()), new Block(info));
      ret.properties().put(Property.Extern, true);
      ret.properties().put(Property.Public, true);
      addItem(ret);
    }

    return ret;
  }

}
