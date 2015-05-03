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

package ast.pass.helper;

public class GraphHelper {

  // TODO do it more elegant
  static public <T> void doTransitiveClosure(ast.doc.SimpleGraph<T> cg) {
    for (T a : cg.vertexSet()) {
      int outdeg = 0;
      while (outdeg != cg.outDegreeOf(a)) {
        outdeg = cg.outDegreeOf(a);
        for (T b : cg.getOutVertices(a)) {
          for (T c : cg.getOutVertices(b)) {
            cg.addEdge(a, c);
          }
        }
      }
    }
  }

}