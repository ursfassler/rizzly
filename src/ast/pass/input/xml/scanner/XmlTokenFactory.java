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

import java.util.HashMap;
import java.util.Map;

public class XmlTokenFactory {

  public static XmlToken elementStart(String name) {
    return new XmlToken(XmlType.ElementStart, name, new HashMap<String, String>());
  }

  public static XmlToken elementStart(String name, Map<String, String> attribute) {
    return new XmlToken(XmlType.ElementStart, name, attribute);
  }

  public static XmlToken elementEnd() {
    return new XmlToken(XmlType.ElementEnd);
  }

  public static XmlToken documentStart() {
    return new XmlToken(XmlType.DocumentStart);
  }

  public static XmlToken documentEnd() {
    return new XmlToken(XmlType.DocumentEnd);
  }

}
