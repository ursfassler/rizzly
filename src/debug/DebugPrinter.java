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

  public DebugPrinter(Evl root, String debugdir) {
    super();
    this.root = root;
    this.debugdir = debugdir;
  }

  public void print(String info) {
    print(info, root);
  }

  public void print(String info, Evl root) {
    PrettyPrinter.print(root, debugdir + info + ".rzy", true);
  }

  public void print(String info, SimpleGraph<Evl> root) {
    printGraph(debugdir + info + ".gv", root);
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
