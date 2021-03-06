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

package ast.doc.compgraph;

import java.util.LinkedList;
import java.util.List;

import ast.meta.Metadata;

public class Connection {
  final private Interface src;
  final private Interface dst;
  final private LinkedList<Vertex> vias = new LinkedList<Vertex>();
  final protected List<Metadata> metadata;

  public Connection(Interface src, Interface dst, List<Metadata> metadata) {
    super();
    this.src = src;
    this.dst = dst;
    this.metadata = metadata;
  }

  public Interface getSrc() {
    return src;
  }

  public Interface getDst() {
    return dst;
  }

  public LinkedList<Vertex> getVias() {
    return vias;
  }

  public List<Metadata> getMetadata() {
    return metadata;
  }

  @Override
  public String toString() {
    return src + " -> " + dst;
  }

}
