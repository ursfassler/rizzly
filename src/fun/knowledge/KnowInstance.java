package fun.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;
import fun.Fun;
import fun.other.ActualTemplateArgument;

public class KnowInstance extends KnowledgeEntry {
  final private Map<Pair<Fun, List<ActualTemplateArgument>>, Fun> instances = new HashMap<Pair<Fun, List<ActualTemplateArgument>>, Fun>();

  @Override
  public void init(KnowledgeBase base) {
  }

  public Fun find(Fun fun, List<ActualTemplateArgument> param) {
    return instances.get(new Pair<Fun, List<ActualTemplateArgument>>(fun, param));
  }

  public void add(Fun fun, List<ActualTemplateArgument> param, Fun inst) {
    instances.put(new Pair<Fun, List<ActualTemplateArgument>>(fun, param), inst);
  }

  public void replace(Fun fun, List<ActualTemplateArgument> param, Fun inst) {
    instances.put(new Pair<Fun, List<ActualTemplateArgument>>(fun, param), inst);
  }
}
