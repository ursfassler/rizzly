package fun.composition;

import common.ElementInfo;

import fun.Fun;
import fun.content.CompIfaceContent;
import fun.other.CompImpl;
import fun.other.FunList;

public class ImplComposition extends CompImpl {
  final private FunList<Fun> instantiation = new FunList<Fun>();
  final private FunList<Connection> connection = new FunList<Connection>();

  public ImplComposition(ElementInfo info, String name) {
    super(info, name);
  }

  public FunList<Connection> getConnection() {
    return connection;
  }

  public FunList<Fun> getInstantiation() {
    return instantiation;
  }

  @Override
  public FunList<CompIfaceContent> getInterface() {
    return findInterface(instantiation);
  }
}
