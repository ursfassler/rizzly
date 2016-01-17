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

package ast.meta;

//TODO remove / merge with MetaInformation
public class Metadata {
  final private SourcePosition info;
  final private String key;
  final private String value;

  public Metadata(SourcePosition info, String key, String value) {
    super();
    this.info = info;
    this.key = key;
    this.value = value;
  }

  public SourcePosition getInfo() {
    return info;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return key + ":" + value;
  }

}
