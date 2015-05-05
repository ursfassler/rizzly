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

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import main.ClaOption;
import parser.FileParser;
import ast.Designator;
import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.file.RizzlyFile;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.List;
import ast.specification.HasName;

public class FileLoader extends AstPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    Set<Designator> loaded = new HashSet<Designator>();
    Queue<Designator> loadQueue = new LinkedList<Designator>();

    loadQueue.add(getRoot(kb));

    while (!loadQueue.isEmpty()) {
      Designator lname = loadQueue.poll();

      RizzlyFile file = loadFile(kb, lname);
      Designator path = getPath(lname);
      addItem(root, path, file);

      loaded.add(lname);
      Set<Designator> newfiles = new HashSet<Designator>(file.imports);
      newfiles.removeAll(loaded);
      newfiles.removeAll(loadQueue);
      loadQueue.addAll(newfiles);
    }
  }

  private Designator getRoot(KnowledgeBase kb) {
    return kb.getOptions().getRootComp().sub(0, kb.getOptions().getRootComp().size() - 1);
  }

  private RizzlyFile loadFile(KnowledgeBase kb, Designator lname) {
    String filename = getFilename(kb, lname);

    String moduleName = lname.last();
    return FileParser.parse(filename, moduleName);
  }

  private Designator getPath(Designator lname) {
    return lname.sub(0, lname.size() - 1);
  }

  private String getFilename(KnowledgeBase kb, Designator lname) {
    return kb.getOptions().getRootPath() + lname.toString(File.separator) + ClaOption.extension;
  }

  private void addItem(Namespace root, Designator path, Ast item) {
    Namespace parent = createOrGetPath(root, path);
    parent.children.add(item);
  }

  static public Namespace createOrGetPath(Namespace ns, Designator des) {
    for (String part : des) {
      ns = createOrGetNs(ns, part);
    }
    return ns;
  }

  static private Namespace createOrGetNs(Namespace ns, String name) {
    AstList<Ast> matches = List.select(ns.children, new HasName(name));
    assert (matches.size() <= 1);

    if (matches.isEmpty()) {
      Namespace ret = new Namespace(ElementInfo.NO, name);
      ns.children.add(ret);
      return ret;
    } else {
      assert (matches.get(0) instanceof Namespace);
      return (Namespace) matches.get(0);
    }
  }
}
