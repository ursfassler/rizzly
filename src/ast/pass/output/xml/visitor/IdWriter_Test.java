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

package ast.pass.output.xml.visitor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ast.data.Ast;
import ast.pass.output.xml.IdReader;

public class IdWriter_Test {
  final private XmlStreamWriter stream = mock(XmlStreamWriter.class);
  final private IdReader astId = mock(IdReader.class);
  final private Ast item = mock(Ast.class);
  final private IdWriter testee = new IdWriter(stream, astId);

  @Test
  public void does_not_call_begin_or_end_when_id_is_not_set() {
    when(astId.hasId(item)).thenReturn(false);

    testee.visit(item);

    verify(stream, never()).beginNode(anyString());
    verify(stream, never()).endNode();
  }

  @Test
  public void does_not_call_begin_or_end_when_id_is_set() {
    when(astId.hasId(item)).thenReturn(true);

    testee.visit(item);

    verify(stream, never()).beginNode(anyString());
    verify(stream, never()).endNode();
  }

  @Test
  public void does_not_write_id_if_none_is_set() {
    when(astId.hasId(item)).thenReturn(false);

    testee.visit(item);

    verify(stream, never()).attribute(anyString(), anyString());
  }

  @Test
  public void does_write_id_when_set() {
    when(astId.hasId(item)).thenReturn(true);
    when(astId.getId(item)).thenReturn("the id");

    testee.visit(item);

    verify(stream, times(1)).attribute(eq("id"), eq("the id"));
  }
}
