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

package main.pass;

import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

public class PassArgumentParser_Test {
  private final PassArgumentParser testee = new PassArgumentParser();

  @Test
  public void returns_nothing_when_nothing_is_passed() {
    Assert.assertEquals(list(), testee.parse(""));
  }

  @Test
  public void return_the_name_when_specified_without_arguments() {
    Assert.assertEquals(list("test"), testee.parse("test"));
    Assert.assertEquals(list("test"), testee.parse("test()"));
    Assert.assertEquals(list("test"), testee.parse("test ()"));
    Assert.assertEquals(list("test"), testee.parse("test( )"));
    Assert.assertEquals(list("test"), testee.parse("test ( )"));
  }

  @Test
  public void return_the_name_and_one_argument() {
    Assert.assertEquals(list("test", "argument"), testee.parse("test(argument)"));
    Assert.assertEquals(list("test", "argument"), testee.parse("test (argument)"));
    Assert.assertEquals(list("test", "argument"), testee.parse("test( argument)"));
    Assert.assertEquals(list("test", "argument"), testee.parse("test(argument )"));
    Assert.assertEquals(list("test", "argument"), testee.parse("test ( argument )"));
  }

  @Test
  public void return_the_name_and_two_arguments() {
    Assert.assertEquals(list("test", "argument", "second"), testee.parse("test(argument,second)"));
    Assert.assertEquals(list("test", "argument", "second"), testee.parse("test(argument ,second)"));
    Assert.assertEquals(list("test", "argument", "second"), testee.parse("test(argument, second)"));
    Assert.assertEquals(list("test", "argument", "second"), testee.parse("test(argument , second)"));
  }

  @Test
  public void return_the_name_and_one_string_argument() {
    Assert.assertEquals(list("test", "x"), testee.parse("test('x')"));
    Assert.assertEquals(list("test", " x"), testee.parse("test(' x')"));
    Assert.assertEquals(list("test", "x "), testee.parse("test('x ')"));
    Assert.assertEquals(list("test", "x y"), testee.parse("test('x y')"));
  }

  private LinkedList<String> list() {
    return new LinkedList<String>();
  }

  private LinkedList<String> list(String arg0) {
    LinkedList<String> list = list();
    list.add(arg0);
    return list;
  }

  private LinkedList<String> list(String arg0, String arg1) {
    LinkedList<String> list = list(arg0);
    list.add(arg1);
    return list;
  }

  private LinkedList<String> list(String arg0, String arg1, String arg2) {
    LinkedList<String> list = list(arg0, arg1);
    list.add(arg2);
    return list;
  }
}
