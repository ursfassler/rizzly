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

package ast.pass.others;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.template.ArrayTemplate;
import ast.data.type.template.RangeTemplate;
import ast.data.type.template.TypeTemplate;
import ast.data.type.template.TypeTypeTemplate;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.TypeFilter;
import ast.traverser.DefTraverser;
import error.ErrorType;
import error.RError;

/**
 * Check if a reserved name is used.
 *
 * @author urs
 *
 */
public class CheckNames extends AstPass {
  // TODO find more elegant way to check Template names
  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    AstList<Type> blacklist = TypeFilter.select(root.children, Type.class);

    AstList<Ast> tocheck = new AstList<Ast>(root.children);
    tocheck.removeAll(blacklist);

    Set<String> names = getNames(blacklist);
    names.addAll(getTemplateNames());

    CheckNamesWorker checkNames = new CheckNamesWorker();
    checkNames.traverse(tocheck, names);
  }

  static private Set<String> getNames(Collection<? extends Named> list) {
    Set<String> names = new HashSet<String>();

    for (Named itr : list) {
      names.add(itr.name);
    }

    return names;
  }

  private Set<String> getTemplateNames() {
    Set<String> ret = new HashSet<String>();
    ret.add(RangeTemplate.NAME);
    ret.add(ArrayTemplate.NAME);
    ret.add(TypeTypeTemplate.NAME);
    return ret;
  }
}

class CheckNamesWorker extends DefTraverser<Void, Collection<String>> {

  @Override
  protected Void visit(Ast obj, Collection<String> param) {
    if (obj instanceof Named) {
      if (param.contains(((Named) obj).name) && !(obj instanceof Template) && !(obj instanceof TypeTemplate)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected name, got keyword " + ((Named) obj).name);
      }
    }
    return super.visit(obj, param);
  }
}
