package debug;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import joGraph.HtmlGraphWriter;

import org.jgrapht.Graph;

import util.Pair;
import util.SimpleGraph;
import evl.Evl;
import evl.traverser.PrettyPrinter;

public class DebugPrinter {
  private Evl root;
  private String debugdir;
  private int nr = 0;

  public DebugPrinter(Evl root, String debugdir) {
    super();
    this.root = root;
    this.debugdir = debugdir;
  }

  public void print(String info) {
    print(info, root);
  }

  public void print(String info, Evl root) {
    nr++;
    PrettyPrinter.print(root, debugdir + "evl_" + nr + "_" + info + ".rzy", true);
  }

  public void print(String info, SimpleGraph<Evl> root) {
    nr++;
    printGraph(debugdir + "evl_" + nr + "_" + info + ".gv", root);
  }

  private static <T extends Evl> void printGraph(String filename, Graph<T, Pair<T, T>> cg) {
    try {
      @SuppressWarnings("resource")
      HtmlGraphWriter<T, Pair<T, T>> writer = new HtmlGraphWriter<T, Pair<T, T>>(new joGraph.Writer(new PrintStream(filename))) {

        @Override
        protected void wrVertex(T v) {
          wrVertexStart(v);
          wrRow(v.toString());
          wrVertexEnd();
        }
      };
      writer.print(cg);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public String getDebugdir() {
    return debugdir;
  }

}
