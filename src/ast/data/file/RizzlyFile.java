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

package ast.data.file;

import java.util.ArrayList;
import java.util.List;

import ast.Designator;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.meta.MetaList;

final public class RizzlyFile extends Named {
  final public List<Designator> imports = new ArrayList<Designator>();
  final public AstList<Ast> objects = new AstList<Ast>();

  public RizzlyFile(String name) {
    setName(name);
  }

  @Deprecated
  public RizzlyFile(MetaList info, String name) {
    metadata().add(info);
    setName(name);
  }

}
