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

package ast.meta;

import ast.visitor.Visitor;
import ast.visitor.VisitorAcceptor;

public class SourcePosition implements VisitorAcceptor, MetaInformation {
  final public String filename;
  final public int line;
  final public int row;

  public SourcePosition(String filename, int line, int row) {
    super();
    this.filename = filename;
    this.line = line;
    this.row = row;
  }

  @Override
  public String toString() {
    return filename + ": " + line + "," + row;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}