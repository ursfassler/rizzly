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

package ast.doc;

import org.w3c.dom.Element;

import ast.Designator;
import ast.data.Ast;
import ast.data.Named;
import ast.knowledge.KnowPath;
import ast.knowledge.KnowledgeBase;

public class HtmlPrinter extends FunPrinter {
  private KnowPath kfp;

  public HtmlPrinter(Writer xw, KnowledgeBase kb) {
    super(xw);
    kfp = kb.getEntry(KnowPath.class);
  }

  public static void print(Ast ast, Element parent, KnowledgeBase kb) {
    HtmlPrinter pp = new HtmlPrinter(new HtmlWriter(parent), kb);
    pp.traverse(ast, null);
  }

  @Override
  protected String getId(Named obj) {
    Designator fullpath = kfp.find(obj);
    if ((fullpath == null) || (fullpath.size() == 0)) {
      // internal type or so
      return "_" + Integer.toHexString(obj.hashCode());
    }

    // TODO verify
    // RizzlyFile file = kff.get(obj);
    //
    // assert (file != null);
    // assert (fullpath.size() >= file.getFullName().size());
    //
    // Designator locpath = new
    // Designator(fullpath.toList().subList(file.getFullName().size(),
    // fullpath.size()));
    // locpath = new Designator(locpath, obj.name);
    //
    Designator locpath = new Designator(fullpath, obj.name);

    return locpath.toString();
  }

}
