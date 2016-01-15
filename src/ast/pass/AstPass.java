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

package ast.pass;

import main.Configuration;
import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;
import ast.specification.AlwaysTrueSpec;
import ast.specification.Specification;

public abstract class AstPass {
  protected final Configuration configuration;

  public AstPass(Configuration configuration) {
    super();
    this.configuration = configuration;
  }

  public Specification getPrecondition() {
    return new AlwaysTrueSpec();
  }

  public Specification getPostcondition() {
    return new AlwaysTrueSpec();
  }

  public abstract void process(Namespace ast, KnowledgeBase kb);

  @Deprecated
  public String getName() {
    return getClass().getName();
  }
}
