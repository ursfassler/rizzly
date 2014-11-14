/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.hfsm.doc;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import util.StreamWriter;
import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
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
    for (State child : obj.getItem().getItems(State.class)) {
      wrEdge(obj, child, "", 1, param);
      visit(child, param);
    }
    return null;
  }

  @Override
  protected Void visitState(State obj, StreamWriter param) {
    param.wr(getId(obj) + " [label=\"" + obj.getName() + "\"];");
    param.nl();
    visitList(obj.getItem().getItems(Transition.class), param);
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
    wrEdge(obj.getSrc().getLink(), obj.getDst().getLink(), obj.getEventFunc().getLink().getName(), 0, param);
    return null;
  }

}
