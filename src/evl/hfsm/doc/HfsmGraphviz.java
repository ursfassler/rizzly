package evl.hfsm.doc;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.doc.StreamWriter;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;
import evl.other.ImplElementary;

public class HfsmGraphviz extends DefTraverser<Void, StreamWriter> {

  public static void print(Evl ast, String filename) {
    HfsmGraphviz pp = new HfsmGraphviz();
    try {
      pp.traverse(ast, new StreamWriter(new PrintStream(filename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void list(List<? extends Evl> list, String sep, StreamWriter param) {
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        param.wr(sep);
      }
      visit(list.get(i), param);
    }
  }

  private void visitList(List<? extends Evl> list, StreamWriter param) {
    for (int i = 0; i < list.size(); i++) {
      visit(list.get(i), param);
    }
  }

  private void wrId(Evl obj, StreamWriter wr) {
    // wr.wr("[" + obj.hashCode() % 10000 + "]");
  }

  private String getId(State obj) {
    int id = obj.hashCode();
    return "_" + id;
  }

  @Override
  public Void traverse(Evl obj, StreamWriter param) {
    param.wr("digraph{");
    param.nl();
    param.incIndent();
    super.traverse(obj, param);
    param.decIndent();
    param.wr("}");
    param.nl();
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, StreamWriter param) {
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, StreamWriter param) {
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, StreamWriter param) {
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, StreamWriter param) {
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, StreamWriter param) {
    for (State child : obj.getItemList(State.class)) {
      wrEdge(obj, child, "", 1, param);
      visit(child, param);
    }
    return null;
  }

  @Override
  protected Void visitState(State obj, StreamWriter param) {
    param.wr(getId(obj) + " [label=\"" + obj.getName() + "\"];");
    param.nl();
    visitItr(obj.getItemList(Transition.class), param);
    return super.visitState(obj, param);
  }

  private void wrProperty(String name, Object value, StreamWriter param) {
    param.wr(name);
    param.wr("=\"");
    param.wr(value.toString());
    param.wr("\" ");
  }

  private void wrEdge(State src, State dst, String label, int weight, StreamWriter param) {
    param.wr(getId(src));
    param.wr(" -> ");
    param.wr(getId(dst));

    param.wr(" [");

    // wrProperty( "label", label, param );
    wrProperty("weight", weight, param);
    if (weight == 0) {
      wrProperty("style", "dotted", param);
    }

    param.wr("];");
    param.nl();
  }

  @Override
  protected Void visitTransition(Transition obj, StreamWriter param) {
    wrEdge(obj.getSrc(), obj.getDst(), obj.getEventIface().getName() + "." + obj.getEventFunc(), 0, param);
    return null;
  }

}
