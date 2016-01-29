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

package ast.data.raw;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.statement.Block;
import ast.data.template.Template;
import ast.meta.MetaList;
import ast.visitor.Visitor;

final public class RawElementary extends RawComponent {
  final private AstList<Template> declaration = new AstList<Template>();
  final private AstList<Ast> instantiation = new AstList<Ast>();
  private Block entryFunc = new Block();
  private Block exitFunc = new Block();

  public RawElementary(String name) {
    super(name);
  }

  @Deprecated
  public RawElementary(MetaList info, String name) {
    super(info, name);
  }

  public AstList<Template> getDeclaration() {
    return declaration;
  }

  public AstList<Ast> getInstantiation() {
    return instantiation;
  }

  public Block getEntryFunc() {
    return entryFunc;
  }

  public Block getExitFunc() {
    return exitFunc;
  }

  public void setEntryFunc(Block entryFunc) {
    this.entryFunc = entryFunc;
  }

  public void setExitFunc(Block exitFunc) {
    this.exitFunc = exitFunc;
  }

}
