package evl.composition;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import evl.other.CompUse;
import evl.other.Component;
import evl.other.ListOfNamed;


public class ImplComposition extends Component {
  final private ListOfNamed<CompUse> component = new ListOfNamed<CompUse>();
  final private List<Connection> connection = new ArrayList<Connection>();

  public ImplComposition(ElementInfo info, String name) {
    super(info, name);
  }

  public ListOfNamed<CompUse> getComponent() {
    return component;
  }

  public List<Connection> getConnection() {
    return connection;
  }

}
