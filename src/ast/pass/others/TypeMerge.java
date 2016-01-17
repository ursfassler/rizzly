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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import main.Configuration;
import ast.copy.Relinker;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.out.AliasType;
import ast.doc.SimpleGraph;
import ast.knowledge.KnowLeftIsContainerOfRight;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.helper.GraphHelper;
import ast.repository.query.Collector;
import ast.specification.IsClass;
import error.RError;

/**
 * Find equal types with different names and merge them
 */
public class TypeMerge extends AstPass {
  public TypeMerge(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    AstList<Type> types = Collector.select(ast, new IsClass(Type.class)).castTo(Type.class);
    Set<Set<Type>> ss = sameSets(kb.getEntry(KnowLeftIsContainerOfRight.class), types);

    Map<Type, Type> linkmap = linkmap(ss, ast);
    Relinker.relink(ast, linkmap);

    kb.clear();
  }

  /**
   * Creates from the same types a link map. Decides who the root-type is
   *
   * @param ss
   * @return
   */
  private Map<Type, Type> linkmap(Set<Set<Type>> ss, Namespace typespace) {

    // TODO by just getting the alphabetical first type this leads to strange or
    // wrong names / types (e.g. for arrays)
    Map<Type, Type> linkmap = new HashMap<Type, Type>();
    for (Set<Type> itr : ss) {
      Type root = getRoot(itr);

      for (Type type : itr) {
        if (type != root) {
          AliasType alias = new AliasType(type.metadata(), type.getName(), TypeRefFactory.create(type.metadata(), root));
          linkmap.put(type, alias);

          RError.ass(typespace.children.contains(type), type.metadata(), "merging types in subtree not yet implemented");
          typespace.children.remove(type);
          typespace.children.add(alias);
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

  private Set<Set<Type>> sameSets(KnowLeftIsContainerOfRight kc, AstList<Type> types) {
    types = new AstList<Type>(types);
    SimpleGraph<Type> same = findSame(types, kc);
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

  private SimpleGraph<Type> findSame(AstList<Type> types, KnowLeftIsContainerOfRight kc) {
    SimpleGraph<Type> ret = new SimpleGraph<Type>();
    for (Type type : types) {
      ret.addVertex(type);
    }
    for (int i = 0; i < types.size() - 1; i++) {
      Type first = types.get(i);
      for (int k = i + 1; k < types.size(); k++) {
        Type second = types.get(k);
        if (kc.areEqual(first, second)) {
          ret.addEdge(first, second);
          ret.addEdge(second, first);
        }
      }
    }
    return ret;
  }

}
