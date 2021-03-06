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

package ast.debug;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import joGraph.HtmlGraphWriter;

import org.jgrapht.Graph;

import util.Pair;
import ast.data.Ast;
import ast.doc.FunPrinter;
import ast.doc.SimpleGraph;
import ast.doc.StreamWriter;

public class DebugPrinter {
  private Ast root;
  private String debugdir;
  private int nr = 0;

  public DebugPrinter(Ast root, String debugdir) {
    super();
    this.root = root;
    this.debugdir = debugdir;
  }

  public void print(String info) {
    print(info, root);
  }

  public void print(String info, Ast root) {
    String filename = debugdir + nr + " " + info + ".rzy";
    print(root, filename);
    nr++;
  }

  static private void print(Ast ast, String filename) {
    try {
      StreamWriter writer = new StreamWriter(new PrintStream(filename));
      FunPrinter pp = new FunPrinter(writer);
      pp.traverse(ast, null);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void print(String info, SimpleGraph<Ast> root) {
    printGraph(debugdir + nr + " " + info + ".gv", root);
    nr++;
  }

  private static <T extends Ast> void printGraph(String filename, Graph<T, Pair<T, T>> cg) {
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
