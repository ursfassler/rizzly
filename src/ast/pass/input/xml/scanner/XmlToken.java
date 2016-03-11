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

package ast.pass.input.xml.scanner;

import java.util.Map;

import ast.meta.MetaList;

public class XmlToken {
  public final XmlType type;  // TODO encapsulate
  public final String name;  // TODO encapsulate
  private final Map<String, String> attribute;
  public final MetaList meta;

  public XmlToken(XmlType type, String name, Map<String, String> attribute, MetaList meta) {
    this.type = type;
    this.name = name;
    this.attribute = attribute;
    this.meta = meta;
  }

  public XmlToken(XmlType type, MetaList meta) {
    this.type = type;
    this.name = null;
    this.attribute = null;
    this.meta = meta;
  }

  public boolean hasAttribute(String name) {
    return attribute.containsKey(name);
  }

  public String getAttribute(String name) {
    return attribute.get(name);
  }
}
