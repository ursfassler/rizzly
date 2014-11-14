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

package common;

import java.util.ArrayList;

public class ElementInfo {
  private String filename;
  private int line;
  private int row;
  final private ArrayList<Metadata> metadata = new ArrayList<Metadata>();
  public final static ElementInfo NO = new ElementInfo("", 0, 0);

  public ElementInfo(String filename, int line, int row) {
    super();
    this.filename = filename;
    this.line = line;
    this.row = row;
  }

  public String getFilename() {
    return filename;
  }

  public int getLine() {
    return line;
  }

  public int getRow() {
    return row;
  }

  public ArrayList<Metadata> getMetadata() {
    return metadata;
  }

  public ArrayList<Metadata> getMetadata(String filterKey) {
    ArrayList<Metadata> ret = new ArrayList<Metadata>();
    for (Metadata itr : metadata) {
      if (itr.getKey().equals(filterKey)) {
        ret.add(itr);
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    return filename + ": " + line + "," + row;
  }

}
