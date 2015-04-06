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

import java.util.ArrayList;
import java.util.List;

import pass.EvlPass;
import evl.data.Namespace;
import evl.knowledge.KnowledgeBase;

abstract public class GroupPass extends EvlPass {
  final private List<EvlPass> passes = new ArrayList<EvlPass>();

  protected void append(EvlPass pass) {
    passes.add(pass);
  }

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (EvlPass pass : passes) {
      pass.process(evl, kb);
    }
  }
}
