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

package parser;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class PeekNReader_Test {
  private final TokenReader<String> scanner = Mockito.mock(TokenReader.class);
  final private PeekNReader<String> testee = new PeekNReader<String>(scanner);

  @Test
  public void can_consume_next_token() {
    Mockito.when(scanner.next()).thenReturn("hello");

    Assert.assertEquals("hello", testee.next());
  }

  @Test
  public void can_peek_next_token() {
    Mockito.when(scanner.next()).thenReturn("world");

    Assert.assertEquals("world", testee.peek(0));
  }

  @Test
  public void peek_returns_the_same_token_as_the_next_next() {
    Mockito.when(scanner.next()).thenReturn("test");

    String peek = testee.peek(0);
    String next = testee.next();

    Assert.assertEquals(peek, next);
    Mockito.verify(scanner).next();
  }

  @Test
  public void can_peek_2_token() {
    Mockito.when(scanner.next()).thenReturn("hello").thenReturn("world");

    Assert.assertEquals("hello", testee.peek(0));
    Assert.assertEquals("world", testee.peek(1));
    Mockito.verify(scanner, Mockito.times(2)).next();
  }

  @Test
  public void peeking_2_tokens_does_not_remove_them() {
    Mockito.when(scanner.next()).thenReturn("hello").thenReturn("world");

    String peek0 = testee.peek(0);
    String peek1 = testee.peek(1);
    String next0 = testee.next();
    String next1 = testee.next();

    Assert.assertEquals(next0, peek0);
    Assert.assertEquals(next1, peek1);
    Mockito.verify(scanner, Mockito.times(2)).next();
  }

  @Test
  public void can_peek_3_tokens() {
    Mockito.when(scanner.next()).thenReturn("0").thenReturn("1").thenReturn("2");

    Assert.assertEquals("0", testee.peek(0));
    Assert.assertEquals("1", testee.peek(1));
    Assert.assertEquals("2", testee.peek(2));
    Mockito.verify(scanner, Mockito.times(3)).next();
  }

}
