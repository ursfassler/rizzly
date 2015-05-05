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

import main.ClaOption;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.composition.CompUse;
import ast.repository.query.TypeFilter;

public class KnowledgeBase {
  final public HashMap<Class<? extends KnowledgeEntry>, KnowledgeEntry> entries = new HashMap<Class<? extends KnowledgeEntry>, KnowledgeEntry>();
  final public Namespace root;
  final public String outDir;
  final public String debugDir;
  final public ClaOption options;

  public KnowledgeBase(Namespace root, String outDir, String debugDir, ClaOption options) {
    super();
    this.root = root;
    this.debugDir = debugDir;
    this.outDir = outDir;
    this.options = options;
  }

  public String getOutDir() {
    return outDir;
  }

  public String getDebugDir() {
    return debugDir;
  }

  public Namespace getRoot() {
    return root;
  }

  public ClaOption getOptions() {
    return options;
  }

  public CompUse getRootComp() {
    AstList<CompUse> list = TypeFilter.select(root.children, CompUse.class);
    assert (list.size() == 1);
    return list.get(0);
  }

  // TODO rename to invalidate
  public void clear() {
    entries.clear();
  }

  // TODO rename to invalidate
  public <T extends KnowledgeEntry> void clear(Class<T> id) {
    entries.remove(id);
  }

  @SuppressWarnings("unchecked")
  public <T extends KnowledgeEntry> T getEntry(Class<T> id) {
    if (!entries.containsKey(id)) {
      KnowledgeEntry entry;
      try {
        entry = id.newInstance();
        entry.init(this);
        entries.put(id, entry);
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return (T) entries.get(id);
  }

}
