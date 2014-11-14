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

package fun.doc;

import java.util.Collection;

import util.Pair;

import common.Designator;

import fun.knowledge.KnowledgeBase;
import fun.other.RizzlyFile;

public class DocWriter {

  static public void print(Collection<Pair<Designator, RizzlyFile>> files, KnowledgeBase kb) {
    ComponentFilePrinter.printCodeStyle(kb.getRootdir());
    CompositionGraphPrinter.printStyle(kb.getRootdir() + ComponentFilePrinter.CompositionStyleName);
    for (Pair<Designator, RizzlyFile> file : files) {
      ComponentFilePrinter printer = new ComponentFilePrinter(kb);
      printer.createDoc(file.second, file.first);
      printer.makeSource(file.second);
      printer.makePicture(file.second);
      printer.print(file.second, file.first);
    }
  }

}
