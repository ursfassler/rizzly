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

package evl.pass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pass.EvlPass;
import util.GraphHelper;
import util.SimpleGraph;
import error.RError;
import evl.copy.Relinker;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.pass.check.type.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.type.out.AliasType;

/**
 * Find equal types with different names and merge them
 *
 * @author urs
 *
 */
public class TypeMerge extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    EvlList<Type> types = evl.getItems(Type.class, true);
    Set<Set<Type>> ss = sameSets(kb, types);

    Map<Type, Type> linkmap = linkmap(ss, evl);
    Relinker.relink(evl, linkmap);
  }

  /**
   * Creates from the same types a link map. Decides who the root-type is
   *
   * @param ss
   * @return
   */
  private Map<Type, Type> linkmap(Set<Set<Type>> ss, Namespace typespace) {

    // TODO by just getting the alphabetical first type this leads to strange or wrong names / types (e.g. for arrays)
    Map<Type, Type> linkmap = new HashMap<Type, Type>();
    for (Set<Type> itr : ss) {
      Type root = getRoot(itr);

      for (Type type : itr) {
        if (type != root) {
          AliasType alias = new AliasType(type.getInfo(), type.getName(), new SimpleRef<Type>(type.getInfo(), root));
          linkmap.put(type, alias);

          RError.ass(typespace.getChildren().contains(type), type.getInfo(), "merging types in subtree not yet implemented");
          typespace.getChildren().remove(type);
          typespace.add(alias);
        }
      }
    }
    return linkmap;
  }

  private Type getRoot(Set<Type> itr) {
    Type root = null;
    for (Type type : itr) {
      if ((root == null) || (type.getName().compareTo(root.getName()) < 0)) {
        root = type;
      }
    }
    return root;
  }

  private Set<Set<Type>> sameSets(KnowledgeBase kb, EvlList<Type> types) {
    types = new EvlList<Type>(types);
    SimpleGraph<Type> same = findSame(types, kb);
    GraphHelper.doTransitiveClosure(same);
    Set<Set<Type>> ss = new HashSet<Set<Type>>();
    for (int i = 0; i < types.size(); i++) {
      Set<Type> group = same.getOutVertices(types.get(i));
      if (!group.isEmpty()) {
        ss.add(group);
        types.removeAll(group);
      }
    }
    return ss;
  }

  private SimpleGraph<Type> findSame(EvlList<Type> types, KnowledgeBase kb) {
    SimpleGraph<Type> ret = new SimpleGraph<Type>();
    for (Type type : types) {
      ret.addVertex(type);
    }
    for (int i = 0; i < types.size() - 1; i++) {
      Type first = types.get(i);
      for (int k = i + 1; k < types.size(); k++) {
        Type second = types.get(k);
        if (LeftIsContainerOfRightTest.areEual(first, second, kb)) {
          ret.addEdge(first, second);
          ret.addEdge(second, first);
        }
      }
    }
    return ret;
  }

}
