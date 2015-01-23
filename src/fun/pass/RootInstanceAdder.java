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

import pass.FunPass;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.knowledge.KnowledgeBase;
import fun.other.Namespace;
import fun.other.Template;
import fun.variable.CompUse;

public class RootInstanceAdder extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    Template rootdecl = (Template) root.getChildItem(kb.getOptions().getRootComp().toList());
    CompUse rootinst = new CompUse(ElementInfo.NO, "inst", new Reference(ElementInfo.NO, rootdecl));
    root.getChildren().add(rootinst);
  }

}
