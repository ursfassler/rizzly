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

import common.ElementInfo;

import evl.data.component.composition.CompUse;
import evl.data.expression.reference.Reference;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import fun.other.Template;

public class RootInstanceAdder extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    KnowChild kc = kb.getEntry(KnowChild.class);
    Template rootdecl = (Template) kc.get(root, kb.getOptions().getRootComp().toList(), root.getInfo());
    evl.data.component.composition.CompUse rootinst = new CompUse(ElementInfo.NO, "inst", new Reference(ElementInfo.NO, rootdecl));
    root.children.add(rootinst);
  }

}
