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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import main.ClaOption;
import parser.FileParser;
import pass.EvlPass;

import common.Designator;
import common.ElementInfo;

import evl.data.Evl;
import evl.data.Namespace;
import evl.data.file.RizzlyFile;
import evl.knowledge.KnowledgeBase;

public class FileLoader extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    Designator rootfile = kb.getOptions().getRootComp().sub(0, kb.getOptions().getRootComp().size() - 1);

    Queue<Designator> toload = new LinkedList<Designator>();
    toload.add(rootfile);

    while (!toload.isEmpty()) {
      Designator lname = toload.poll();
      assert (lname.size() > 0);
      lname.sub(0, lname.size() - 1);
      evl.data.Namespace parent = ChildPath.force(root, lname.sub(0, lname.size() - 1).toList());
      if (parent.children.find(lname.last()) != null) {
        continue;
      }
      String filename = kb.getOptions().getRootPath() + lname.toString(File.separator) + ClaOption.extension;
      RizzlyFile lfile = FileParser.parse(filename, lname.last());

      parent.children.add(lfile);

      for (Designator name : lfile.getImports()) {
        toload.add(name);
      }
    }
  }

}

@Deprecated
class ChildPath {
  // TODO make it correct (cleanup)

  static public evl.data.Namespace force(evl.data.Namespace ns, List<String> des) {
    LinkedList<String> ipath = new LinkedList<String>(des);
    evl.data.Namespace itr = ns;

    while (!ipath.isEmpty()) {
      String ename = ipath.pop();
      itr = force(itr, ename);
    }
    return itr;
  }

  static private evl.data.Namespace force(evl.data.Namespace ns, String ename) {
    assert (findItem(ns, ename) == null);
    evl.data.Namespace ret = findSpace(ns, ename);
    if (ret == null) {
      ret = new Namespace(ElementInfo.NO, ename);
      ns.children.add(ret);
    }
    return ret;
  }

  static private evl.data.Namespace findSpace(evl.data.Namespace ns, String name) {
    Evl ret = ns.children.find(name);
    if (ret instanceof Namespace) {
      return (evl.data.Namespace) ret;
    } else {
      return null;
    }
  }

  static private Evl findItem(evl.data.Namespace ns, String name) {
    Evl ret = ns.children.find(name);
    if (!(ret instanceof Namespace)) {
      return ret;
    } else {
      return null;
    }
  }

}
