package fun.other;

import common.ElementInfo;

import fun.Fun;
import fun.content.CompIfaceContent;
import fun.statement.Block;

public class ImplElementary extends CompImpl {
  final private FunList<Template> declaration = new FunList<Template>();
  final private FunList<Fun> instantiation = new FunList<Fun>();
  private Block entryFunc = new Block(ElementInfo.NO);
  private Block exitFunc = new Block(ElementInfo.NO);

  public ImplElementary(ElementInfo info, String name) {
    super(info, name);
  }

  public FunList<Template> getDeclaration() {
    return declaration;
  }

  public FunList<Fun> getInstantiation() {
    return instantiation;
  }

  public Block getEntryFunc() {
    return entryFunc;
  }

  public Block getExitFunc() {
    return exitFunc;
  }

  public void setEntryFunc(Block entryFunc) {
    this.entryFunc = entryFunc;
  }

  public void setExitFunc(Block exitFunc) {
    this.exitFunc = exitFunc;
  }

  @Override
  public FunList<CompIfaceContent> getInterface() {
    return findInterface(instantiation);
  }

}
