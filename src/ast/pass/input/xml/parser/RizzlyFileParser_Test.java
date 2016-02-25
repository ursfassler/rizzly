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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ast.data.file.RizzlyFile;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class RizzlyFileParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private RizzlyFileParser testee = new RizzlyFileParser(stream, error);
  final private InOrder order = Mockito.inOrder(stream);

  @Test
  public void has_correct_name() {
    assertEquals(Names.list("RizzlyFile"), testee.names());
  }

  @Test
  public void has_correct_type() {
    assertEquals(RizzlyFile.class, testee.type());
  }

  @Test
  public void parse_rizzly_file_node() {
    when(stream.attribute(eq("name"))).thenReturn("the file name");

    RizzlyFile file = testee.parse();

    assertEquals("the file name", file.getName());

    order.verify(stream).elementStart(eq("RizzlyFile"));
    order.verify(stream).attribute(eq("name"));
    order.verify(stream).elementEnd();
  }

}
