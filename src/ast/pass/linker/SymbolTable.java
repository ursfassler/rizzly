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

package ast.pass.linker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ast.data.Ast;
import ast.data.Named;
import error.ErrorType;
import error.RError;

/**
 *
 * @author urs
 */
public class SymbolTable {

  private SymbolTable parent;
  private Map<String, Named> entries = new HashMap<String, Named>();

  public SymbolTable(SymbolTable parent) {
    this.parent = parent;
  }

  public SymbolTable() {
    this.parent = null;
  }

  public Named find(String name, boolean recursive) {
    if (entries.containsKey(name)) {
      return entries.get(name);
    } else if (recursive && (parent != null)) {
      return parent.find(name, recursive);
    } else {
      return null;
    }
  }

  public Named find(String name) {
    return find(name, true);
  }

  public void addAll(Collection<? extends Ast> syms) {
    for (Ast itr : syms) {
      add(itr);
    }
  }

  public void add(Named obj) {
    Ast old = find(obj.name, false);
    if (old != null) {
      if (old instanceof Ast) {
        RError.err(ErrorType.Hint, old.getInfo(), "First definition was here");
        RError.err(ErrorType.Error, obj.getInfo(), "Entry already defined: " + obj.name);
      } else {
        RError.err(ErrorType.Error, "Entry already defined: " + obj.name);
      }
    }
    entries.put(obj.name, obj);
  }

  public void add(Ast obj) {
    if (obj instanceof Named) {
      add((Named) obj);
    }
  }

  public SymbolTable getParent() {
    return parent;
  }

}
