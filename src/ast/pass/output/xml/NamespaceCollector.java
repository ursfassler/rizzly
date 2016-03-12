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

package ast.pass.output.xml;

import java.util.Collection;
import java.util.HashSet;

import ast.pass.output.xml.visitor.XmlStreamWriter;

public class NamespaceCollector implements XmlStreamWriter {
  private final Collection<String> namespaces = new HashSet<>();

  public Collection<String> getNamespaces() {
    return namespaces;
  }

  @Override
  public void beginNode(String namespace, String localName) {
    namespaces.add(namespace);
  }

  @Override
  public void beginNode(String name) {
  }

  @Override
  public void endNode() {
  }

  @Override
  public void attribute(String name, String value) {
  }

}
