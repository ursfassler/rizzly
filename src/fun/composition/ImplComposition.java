package fun.composition;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import fun.other.Component;
import fun.other.ListOfNamed;
import fun.variable.CompUse;

public class ImplComposition extends Component {
  final private ListOfNamed<CompUse> component = new ListOfNamed<CompUse>();
  final private List<Connection> connection = new ArrayList<Connection>();

  public ImplComposition(ElementInfo info,String name) {
    super(info,name);
  }

  public ListOfNamed<CompUse> getComponent() {
    return component;
  }

  public List<Connection> getConnection() {
    return connection;
  }

}
