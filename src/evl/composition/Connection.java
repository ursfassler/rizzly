package evl.composition;

import java.util.HashMap;
import java.util.Map;

import common.Direction;
import common.ElementInfo;

import evl.EvlBase;

public class Connection extends EvlBase {
  final private Map<Direction, Endpoint> endpoint;
  private MessageType type;

  public Connection(ElementInfo info, Endpoint src, Endpoint dst, MessageType type) {
    super(info);
    this.type = type;
    endpoint = new HashMap<Direction, Endpoint>();
    endpoint.put(Direction.in, src);
    endpoint.put(Direction.out, dst);
  }

  public Endpoint getEndpoint(Direction dir) {
    return endpoint.get(dir);
  }

  public void setEndpoint(Direction dir, Endpoint ref) {
    endpoint.put(dir, ref);
  }

  public MessageType getType() {
    return type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

}
