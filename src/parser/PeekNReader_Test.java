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

import parser.hfsm.Scanner_Dummy;

public class PeekNReader_Test {
  private final Scanner_Dummy<String> scanner = new Scanner_Dummy<String>("");
  final private PeekNReader<String> testee = new PeekNReader<String>(scanner);

  @Test
  public void can_consume_next_token() {
    scanner.add("hello");

    Assert.assertEquals("hello", testee.next());
  }

  @Test
  public void can_peek_next_token() {
    scanner.add("world");

    Assert.assertEquals("world", testee.peek(0));
  }

  @Test
  public void peek_returns_the_same_token_as_the_next_next() {
    scanner.add("test");

    String peek = testee.peek(0);
    String next = testee.next();

    Assert.assertEquals(peek, next);
  }

  @Test
  public void can_peek_2_token() {
    scanner.add("hello");
    scanner.add("world");

    Assert.assertEquals("hello", testee.peek(0));
    Assert.assertEquals("world", testee.peek(1));
  }

  @Test
  public void peeking_2_tokens_does_not_remove_them() {
    scanner.add("hello");
    scanner.add("world");

    String peek0 = testee.peek(0);
    String peek1 = testee.peek(1);
    String next0 = testee.next();
    String next1 = testee.next();

    Assert.assertEquals(next0, peek0);
    Assert.assertEquals(next1, peek1);
  }

  @Test
  public void can_peek_5_tokens() {
    scanner.add("0");
    scanner.add("1");
    scanner.add("2");
    scanner.add("3");
    scanner.add("4");

    Assert.assertEquals("0", testee.peek(0));
    Assert.assertEquals("1", testee.peek(1));
    Assert.assertEquals("2", testee.peek(2));
    Assert.assertEquals("3", testee.peek(3));
    Assert.assertEquals("4", testee.peek(4));
  }

}
