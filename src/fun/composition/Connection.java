package fun.composition;

import java.util.HashMap;
import java.util.Map;

import common.Direction;
import common.ElementInfo;

import evl.composition.MessageType;
import fun.FunBase;
import fun.expression.reference.Reference;

public class Connection extends FunBase {
  final private Map<Direction, Reference> endpoint;
  private MessageType type;

  public Connection(ElementInfo info, Reference src, Reference dst, MessageType type) {
    super(info);
    this.type = type;
    endpoint = new HashMap<Direction, Reference>();
    endpoint.put(Direction.in, src);
    endpoint.put(Direction.out, dst);
  }

  public Reference getEndpoint(Direction dir) {
    return endpoint.get(dir);
  }

  public void setEndpoint(Direction dir, Reference ref) {
    endpoint.put(dir, ref);
  }

  public MessageType getType() {
    return type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

}
