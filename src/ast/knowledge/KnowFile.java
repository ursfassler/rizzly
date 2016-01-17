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
import java.util.Map;

import ast.Designator;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.file.RizzlyFile;
import ast.dispatcher.DfsTraverser;
import ast.dispatcher.NullDispatcher;
import ast.repository.query.ChildByName;
import error.ErrorType;
import error.RError;

/**
 * Knows in which file an object is.
 *
 * @author urs
 *
 */
public class KnowFile extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Ast, RizzlyFile> cache = new HashMap<Ast, RizzlyFile>();
  private Map<RizzlyFile, Designator> path = new HashMap<RizzlyFile, Designator>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public RizzlyFile get(Designator path) {
    Ast item = ChildByName.staticGet(base.getRoot(), path, base.getRoot().metadata());
    assert (item instanceof RizzlyFile);
    return (RizzlyFile) item;
  }

  public RizzlyFile get(Ast obj) {
    RizzlyFile ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, "Object not reachable: " + obj, obj.metadata());
    }
    return ret;
  }

  public RizzlyFile find(Ast obj) {
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

class KnowFileTraverser extends DfsTraverser<Void, RizzlyFile> {
  private Map<Ast, RizzlyFile> cache;

  public KnowFileTraverser(Map<Ast, RizzlyFile> cache) {
    super();
    this.cache = cache;
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, RizzlyFile param) {
    assert (param == null);
    return super.visitRizzlyFile(obj, obj);
  }

  @Override
  protected Void visit(Ast obj, RizzlyFile param) {
    if (cache.containsKey(obj)) {
      RizzlyFile oldparent = cache.get(obj);
      RError.err(ErrorType.Fatal, "Same object (" + obj + ") found 2 times: " + oldparent + " and " + param, obj.metadata());
    }
    cache.put(obj, param);
    return super.visit(obj, param);
  }

}

class FileNamespace extends NullDispatcher<Void, Designator> {

  // TODO replace with KnowPath

  static Map<RizzlyFile, Designator> get(ast.data.Namespace ns) {
    FileNamespace traverser = new FileNamespace();
    traverser.traverse(ns, new Designator());
    return traverser.files;
  }

  final private Map<RizzlyFile, Designator> files = new HashMap<RizzlyFile, Designator>();

  @Override
  protected Void visitDefault(Ast obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, Designator param) {
    files.put(obj, param);
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    param = new Designator(param, obj.getName());
    for (Ast itr : obj.children) {
      visit(itr, param);
    }
    return null;
  }

}
