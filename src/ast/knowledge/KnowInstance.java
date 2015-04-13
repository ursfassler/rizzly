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

package ast.knowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Pair;
import ast.data.Ast;
import ast.data.template.ActualTemplateArgument;

public class KnowInstance extends KnowledgeEntry {
  final private Map<Pair<Ast, List<ActualTemplateArgument>>, Ast> instances = new HashMap<Pair<Ast, List<ActualTemplateArgument>>, Ast>();

  @Override
  public void init(ast.knowledge.KnowledgeBase base) {
  }

  public Ast find(Ast fun, List<ActualTemplateArgument> param) {
    return instances.get(new Pair<Ast, List<ActualTemplateArgument>>(fun, param));
  }

  public void add(Ast fun, List<ActualTemplateArgument> param, Ast inst) {
    instances.put(new Pair<Ast, List<ActualTemplateArgument>>(fun, param), inst);
  }

  public void replace(Ast fun, List<ActualTemplateArgument> param, Ast inst) {
    instances.put(new Pair<Ast, List<ActualTemplateArgument>>(fun, param), inst);
  }
}
