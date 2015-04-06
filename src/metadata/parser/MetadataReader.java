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

package metadata.parser;

import java.util.LinkedList;
import java.util.List;

import parser.PeekReader;

import common.ElementInfo;
import common.Metadata;

/**
 *
 * @author urs
 */
public class MetadataReader implements PeekReader<Character> {

  private LinkedList<Metadata> metadata;
  private int pos;
  private Character nextSym = null;

  public MetadataReader(List<Metadata> metadata) {
    this.metadata = new LinkedList<Metadata>(metadata);
    pos = 0;
    next();
  }

  private void closeFile() {
    metadata.clear();
  }

  @Override
  public Character peek() {
    return nextSym;
  }

  @Override
  public boolean hasNext() {
    return peek() != null;
  }

  @Override
  public Character next() {
    Character sym = nextSym;

    if (metadata.isEmpty()) {
      nextSym = null;
      closeFile();
      return sym;
    } else if (pos >= getActualMeta().getValue().length()) {
      metadata.pop();
      pos = 0;
    }

    if (metadata.isEmpty()) {
      nextSym = null;
      closeFile();
      return sym;
    }

    nextSym = getActualMeta().getValue().charAt(pos);
    pos++;

    return sym;
  }

  public ElementInfo getInfo() {
    ElementInfo metaInfo = getActualMeta().getInfo();
    int keySize = getActualMeta().getKey().length() + 1;
    ElementInfo info = new ElementInfo(metaInfo.filename, metaInfo.line, metaInfo.row + pos + keySize);
    return info;
  }

  private Metadata getActualMeta() {
    return metadata.getFirst();
  }
}
