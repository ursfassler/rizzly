package evl.composition;

import common.ElementInfo;

import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;

public class ImplComposition extends Component {
  final private EvlList<CompUse> component = new EvlList<CompUse>();
  final private EvlList<Connection> connection = new EvlList<Connection>();

  public ImplComposition(ElementInfo info, String name) {
    super(info, name);
  }

  public EvlList<CompUse> getComponent() {
    return component;
  }

  public EvlList<Connection> getConnection() {
    return connection;
  }

}
