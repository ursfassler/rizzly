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
import java.util.Queue;

import main.ClaOption;
import parser.FileParser;
import pass.FunPass;

import common.Designator;

import fun.knowledge.KnowledgeBase;
import fun.other.Namespace;
import fun.other.RizzlyFile;

public class FileLoader extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    Designator rootfile = kb.getOptions().getRootComp().sub(0, kb.getOptions().getRootComp().size() - 1);

    Queue<Designator> toload = new LinkedList<Designator>();
    toload.add(rootfile);

    while (!toload.isEmpty()) {
      Designator lname = toload.poll();
      assert (lname.size() > 0);
      lname.sub(0, lname.size() - 1);
      Namespace parent = root.forceChildPath(lname.sub(0, lname.size() - 1).toList());
      if (parent.getChildren().find(lname.last()) != null) {
        continue;
      }
      String filename = kb.getOptions().getRootPath() + lname.toString(File.separator) + ClaOption.extension;
      RizzlyFile lfile = FileParser.parse(filename, lname.last());

      parent.getChildren().add(lfile);

      for (Designator name : lfile.getImports()) {
        toload.add(name);
      }
    }
  }

}
