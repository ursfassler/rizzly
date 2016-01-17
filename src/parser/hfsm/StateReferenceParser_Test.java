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

package parser.hfsm;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Test;

import parser.PeekNReader;
import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.data.component.hfsm.StateRef;
import ast.data.reference.RefName;
import ast.meta.MetaList;
import error.ErrorType;
import error.RizzlyError;

public class StateReferenceParser_Test {

  private final RizzlyError error = mock(RizzlyError.class);
  private final Scanner_Dummy<Token> scanner = new Scanner_Dummy<Token>(new Token(TokenType.EOF));
  private final StateReferenceParser testee = new StateReferenceParser(new PeekNReader<Token>(scanner), error);

  @Test
  public void can_be_one_identifier() {
    scanner.add(new Token(TokenType.IDENTIFIER, "xyz"));

    StateRef stateRef = testee.parse();

    Assert.assertEquals("xyz", stateRef.ref.link.getName());
    Assert.assertEquals(0, stateRef.ref.offset.size());
  }

  @Test
  public void can_be_2_identifiers_with_a_dot_between() {
    scanner.add(new Token(TokenType.IDENTIFIER, "x"));
    scanner.add(new Token(TokenType.PERIOD));
    scanner.add(new Token(TokenType.IDENTIFIER, "y"));

    StateRef stateRef = testee.parse();

    Assert.assertEquals("x", stateRef.ref.link.getName());
    Assert.assertEquals(1, stateRef.ref.offset.size());

    Assert.assertTrue(stateRef.ref.offset.get(0) instanceof RefName);
    Assert.assertEquals("y", ((RefName) stateRef.ref.offset.get(0)).name);
  }

  @Test
  public void can_be_multiple_identifiers_with_a_dot_between() {
    scanner.add(new Token(TokenType.IDENTIFIER, "x"));
    scanner.add(new Token(TokenType.PERIOD));
    scanner.add(new Token(TokenType.IDENTIFIER, "y"));
    scanner.add(new Token(TokenType.PERIOD));
    scanner.add(new Token(TokenType.IDENTIFIER, "z"));

    StateRef stateRef = testee.parse();

    Assert.assertEquals("x", stateRef.ref.link.getName());
    Assert.assertEquals(2, stateRef.ref.offset.size());

    Assert.assertTrue(stateRef.ref.offset.get(0) instanceof RefName);
    Assert.assertEquals("y", ((RefName) stateRef.ref.offset.get(0)).name);
    Assert.assertTrue(stateRef.ref.offset.get(1) instanceof RefName);
    Assert.assertEquals("z", ((RefName) stateRef.ref.offset.get(1)).name);
  }

  @Test
  public void can_be_self() {
    scanner.add(new Token(TokenType.IDENTIFIER, "self"));

    StateRef stateRef = testee.parse();

    Assert.assertEquals("self", stateRef.ref.link.getName());
    Assert.assertEquals(0, stateRef.ref.offset.size());
  }

  @Test
  public void emits_error_for_unexpected_token() {
    scanner.add(new Token(TokenType.SEMI));

    testee.parse();

    verify(error).err(eq(ErrorType.Error), anyString(), any(MetaList.class));
  }

  @Test
  public void uses_info_from_token_for_error_message_for_unexpected_token() {
    MetaList meta = mock(MetaList.class);
    scanner.add(new Token(TokenType.SEMI, meta));

    testee.parse();

    verify(error).err(any(ErrorType.class), anyString(), eq(meta));
  }

  @Test
  public void has_meaningfull_error_message_for_unexpected_token() {
    scanner.add(new Token(TokenType.SEMI));

    testee.parse();

    verify(error).err(any(ErrorType.class), eq("expected IDENTIFIER, got SEMI"), any(MetaList.class));
  }

  @Test
  public void returns_null_for_unexpected_token() {
    scanner.add(new Token(TokenType.SEMI));

    StateRef stateRef = testee.parse();

    Assert.assertEquals(null, stateRef);
  }

}
