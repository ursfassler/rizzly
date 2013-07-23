package joGraph;

import java.util.Map;
import java.util.Set;

import evl.Evl;


public class JoGraphWriter<T extends Evl> extends GraphWriter {

  public static <T extends Evl> void print(Map<T, Set<T>> graph, Writer writer) {
    JoGraphWriter<T> visitor = new JoGraphWriter<T>(writer);
    visitor.process(graph);
  }

  public JoGraphWriter(Writer wr) {
    super(wr);
  }

  private void process(Map<T, Set<T>> graph) {
    wrHeader(graph);

    for (Object src : graph.keySet()) {
      wrVertex(src);
      for (Object dst : graph.get(src)) {
        wrEdge(src, dst, 0);
      }
    }

    wrFooter();
  }
}
