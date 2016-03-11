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

import ast.meta.MetaInformation;
import ast.meta.MetaList;
import ast.meta.MetaListImplementation;

public class XmlTokenFactory {

  public static XmlToken elementStart(String name, MetaInformation meta) {
    return new XmlToken(XmlType.ElementStart, name, new HashMap<String, String>(), metalist(meta));
  }

  public static XmlToken elementStart(String name, Map<String, String> attribute, MetaInformation meta) {
    return new XmlToken(XmlType.ElementStart, name, attribute, metalist(meta));
  }

  public static XmlToken elementEnd(MetaInformation meta) {
    return new XmlToken(XmlType.ElementEnd, metalist(meta));
  }

  public static XmlToken documentStart(MetaInformation meta) {
    return new XmlToken(XmlType.DocumentStart, metalist(meta));
  }

  public static XmlToken documentEnd(MetaInformation meta) {
    return new XmlToken(XmlType.DocumentEnd, metalist(meta));
  }

  public static XmlToken endOfFile(MetaInformation meta) {
    return new XmlToken(XmlType.EndOfFile, metalist(meta));
  }

  private static MetaList metalist(MetaInformation meta) {
    MetaList list = new MetaListImplementation();
    list.add(meta);
    return list;
  }

}
