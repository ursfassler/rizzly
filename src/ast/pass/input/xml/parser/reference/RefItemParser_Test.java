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

package ast.pass.input.xml.parser.reference;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import ast.data.reference.RefItem;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class RefItemParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private RefItemParser testee = new RefItemParser(stream, parser, error);

  @Test
  public void has_no_parsers_yet() {
  }

  @Test
  public void has_correct_type() {
    assertEquals(testee, testee.parserFor(RefItem.class));
  }

}
