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

package evl.knowledge;

import java.util.HashMap;
import java.util.Map;

import common.Designator;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.Namespace;
import evl.data.file.RizzlyFile;
import evl.traverser.DefTraverser;
import evl.traverser.NullTraverser;

/**
 * Knows in which file an object is.
 *
 * @author urs
 *
 */
public class KnowFile extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Evl, RizzlyFile> cache = new HashMap<Evl, RizzlyFile>();
  private Map<RizzlyFile, Designator> path = new HashMap<RizzlyFile, Designator>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public RizzlyFile get(Designator path) {
    KnowChild kc = base.getEntry(KnowChild.class);
    Evl item = kc.get(base.getRoot(), path.toList(), base.getRoot().getInfo());
    assert (item instanceof RizzlyFile);
    return (RizzlyFile) item;
  }

  public RizzlyFile get(Evl obj) {
    RizzlyFile ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  public RizzlyFile find(Evl obj) {
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
    Map<RizzlyFile, Designator> items = FileNamespace.get(base.getRoot());
    for (RizzlyFile file : items.keySet()) {
      Designator ns = items.get(file);
      traverser.traverse(file, null);
      path.put(file, ns);
    }
  }

  public void clear() {
    cache.clear();
    path.clear();
  }

}

class KnowFileTraverser extends DefTraverser<Void, RizzlyFile> {
  private Map<Evl, RizzlyFile> cache;

  public KnowFileTraverser(Map<Evl, RizzlyFile> cache) {
    super();
    this.cache = cache;
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, RizzlyFile param) {
    assert (param == null);
    return super.visitRizzlyFile(obj, obj);
  }

  @Override
  protected Void visit(Evl obj, RizzlyFile param) {
    if (cache.containsKey(obj)) {
      RizzlyFile oldparent = cache.get(obj);
      RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times: " + oldparent + " and " + param);
    }
    cache.put(obj, param);
    return super.visit(obj, param);
  }

}

class FileNamespace extends NullTraverser<Void, Designator> {

  // TODO replace with KnowPath

  static Map<RizzlyFile, Designator> get(evl.data.Namespace ns) {
    FileNamespace traverser = new FileNamespace();
    traverser.traverse(ns, new Designator());
    return traverser.files;
  }

  final private Map<RizzlyFile, Designator> files = new HashMap<RizzlyFile, Designator>();

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, Designator param) {
    files.put(obj, param);
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    param = new Designator(param, obj.name);
    for (Evl itr : obj.children) {
      visit(itr, param);
    }
    return null;
  }

}
