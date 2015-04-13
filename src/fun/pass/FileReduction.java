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

import pass.EvlPass;
import evl.data.Namespace;
import evl.data.file.RizzlyFile;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

public class FileReduction extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    FileReductionWorker reduction = new FileReductionWorker();
    reduction.traverse(root, null);
  }

}

class FileReductionWorker extends DefTraverser<Void, Namespace> {

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, Namespace param) {
    assert (param.children.contains(obj));
    int idx = param.children.indexOf(obj);
    evl.data.Namespace space = new Namespace(obj.getInfo(), obj.name);
    space.children.addAll(obj.getObjects());
    param.children.set(idx, space);
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Namespace param) {
    return super.visitNamespace(obj, obj);
  }

}
