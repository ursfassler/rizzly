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

import ast.data.reference.Anchor;
import ast.pass.input.xml.infrastructure.XmlParser;
import ast.pass.input.xml.linker.LinkDummyRecorder;
import ast.pass.input.xml.parser.Names;
import ast.pass.input.xml.scanner.ExpectionParser;
import error.RizzlyError;

public class AnchorParser_Test {
  final private ExpectionParser stream = mock(ExpectionParser.class);
  final private LinkDummyRecorder linkDummyRecorder = mock(LinkDummyRecorder.class);
  final private XmlParser parser = mock(XmlParser.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private AnchorParser testee = new AnchorParser(stream, linkDummyRecorder, parser, error);

  @Test
  public void has_both_anchors() {
    assertEquals(Names.list("UnlinkedAnchor", "LinkedAnchor"), testee.names());
  }

  @Test
  public void has_correct_type() {
    assertEquals(Anchor.class, testee.type());
  }

}
