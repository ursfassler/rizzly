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

package fun.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import util.Pair;

import common.Designator;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.other.RizzlyFile;

/**
 * Knows in which file an object is.
 *
 * @author urs
 *
 */
public class KnowFunFile extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Fun, RizzlyFile> cache = new HashMap<Fun, RizzlyFile>();
  private Map<RizzlyFile, Designator> path = new HashMap<RizzlyFile, Designator>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public RizzlyFile get(Designator path) {
    Fun item = base.getRoot().getChildItem(path.toList());
    assert (item instanceof RizzlyFile);
    return (RizzlyFile) item;
  }

  public RizzlyFile get(Fun obj) {
    RizzlyFile ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  public RizzlyFile find(Fun obj) {
    RizzlyFile ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    return ret;
  }

  public Designator path(RizzlyFile file) {
    return path.get(file);
  }

  private void rebuild() {
    clear();
    KnowFileTraverser traverser = new KnowFileTraverser(cache);
    Set<Pair<Designator, RizzlyFile>> items = new HashSet<Pair<Designator, RizzlyFile>>();
    base.getRoot().getItems(RizzlyFile.class, new Designator(), items);
    for (Pair<Designator, RizzlyFile> file : items) {
      traverser.traverse(file.second, null);
      path.put(file.second, file.first);
    }
  }

  public void clear() {
    cache.clear();
    path.clear();
  }

}

class KnowFileTraverser extends DefTraverser<Void, RizzlyFile> {
  private Map<Fun, RizzlyFile> cache;

  public KnowFileTraverser(Map<Fun, RizzlyFile> cache) {
    super();
    this.cache = cache;
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, RizzlyFile param) {
    assert (param == null);
    return super.visitRizzlyFile(obj, obj);
  }

  @Override
  protected Void visit(Fun obj, RizzlyFile param) {
    if (cache.containsKey(obj)) {
      RizzlyFile oldparent = cache.get(obj);
      RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times: " + oldparent + " and " + param);
    }
    cache.put(obj, param);
    return super.visit(obj, param);
  }

}
