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

package ast.pass.input.xml.parser;

import java.util.Collection;

import ast.data.file.RizzlyFile;
import ast.pass.input.xml.infrastructure.Parser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class RizzlyFileParser implements Parser {
  private static final String Name = "RizzlyFile";
  private final ExpectionParser stream;
  private final RizzlyError error;

  public RizzlyFileParser(ExpectionParser stream, RizzlyError error) {
    this.stream = stream;
    this.error = error;
  }

  @Override
  public Collection<String> names() {
    return Names.list(Name);
  }

  @Override
  public Class type() {
    return RizzlyFile.class;
  }

  @Override
  public RizzlyFile parse() {
    expectElementStart(Name);
    String name = expectAttribute("name");
    expectElementEnd();

    return new RizzlyFile(name);
  }

  private void expectElementEnd() {
    stream.elementEnd();
  }

  private String expectAttribute(String value) {
    return stream.attribute(value);
  }

  private void expectElementStart(String value) {
    stream.elementStart(value);
  }

}
