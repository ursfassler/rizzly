package fun.other;

import common.ElementInfo;

import fun.Fun;
import fun.FunBase;
import fun.content.CompIfaceContent;
import fun.content.FileContent;

abstract public class CompImpl extends FunBase implements FileContent, Named {
  private String name;
  private final FunList<Fun> objects = new FunList<Fun>();

  public CompImpl(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public FunList<Fun> getObjects() {
    return objects;
  }

  public abstract FunList<CompIfaceContent> getInterface();

  static protected FunList<CompIfaceContent> findInterface(FunList<Fun> instantiation) {
    return instantiation.getItems(CompIfaceContent.class);
  }
}
