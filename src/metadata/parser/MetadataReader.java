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
import ast.meta.MetaList;
import ast.meta.MetaListImplementation;
import ast.meta.Metadata;
import ast.meta.SourcePosition;

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

  public MetaList getInfo() {
    SourcePosition metaInfo = getActualMeta().getInfo();
    int keySize = getActualMeta().getKey().length() + 1;
    SourcePosition info = new SourcePosition(metaInfo.filename, metaInfo.line, metaInfo.row + pos + keySize);
    MetaList meta = new MetaListImplementation();
    meta.add(info);
    return meta;
  }

  private Metadata getActualMeta() {
    return metadata.getFirst();
  }
}
