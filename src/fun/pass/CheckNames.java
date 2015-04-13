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

package fun.pass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.type.Type;
import evl.data.type.template.ArrayTemplate;
import evl.data.type.template.RangeTemplate;
import evl.data.type.template.TypeTemplate;
import evl.data.type.template.TypeTypeTemplate;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;
import evl.traverser.other.ClassGetter;
import fun.other.Template;

/**
 * Check if a reserved name is used.
 *
 * @author urs
 *
 */
public class CheckNames extends EvlPass {
  // TODO find more elegant way to check Template names
  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    EvlList<Type> blacklist = ClassGetter.filter(Type.class, root.children);

    EvlList<Evl> tocheck = new EvlList<Evl>(root.children);
    tocheck.removeAll(blacklist);

    Set<String> names = getNames(blacklist);
    names.addAll(getTemplateNames());

    CheckNamesWorker checkNames = new CheckNamesWorker();
    for (Evl itr : tocheck) {
      checkNames.traverse(itr, names);
    }
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
  protected Void visit(Evl obj, Collection<String> param) {
    if (obj instanceof Named) {
      if (param.contains(((Named) obj).name) && !(obj instanceof Template) && !(obj instanceof TypeTemplate)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected name, got keyword " + ((Named) obj).name);
      }
    }
    return super.visit(obj, param);
  }
}
