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

package fun.traverser.spezializer;

import pass.FunPass;
import fun.knowledge.KnowledgeBase;
import fun.other.Namespace;

/**
 * Replaces all types with the evaluated expression:
 *
 * a : U{3+5} => a : U_8
 *
 * @author urs
 *
 */
public class TypeEvalReplacerPass extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    kb.clear();
    TypeEvalReplacer replacer = new TypeEvalReplacer(kb);
    replacer.traverse(root, null);
  }
}
