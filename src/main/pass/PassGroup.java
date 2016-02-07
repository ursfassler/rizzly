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

package main.pass;

import java.util.ArrayList;
import java.util.List;

import ast.pass.AstPass;

public class PassGroup extends Pass {
  final public List<Pass> passes = new ArrayList<Pass>();
  final public List<AstPass> checks = new ArrayList<AstPass>();

  PassGroup(String name) {
    super(name);
  }

  public void add(AstPass pass, String name) {
    passes.add(new PassItem(pass, name));
  }

  public void add(AstPass pass) {
    passes.add(new PassItem(pass, pass.getClass().getSimpleName()));
  }

}