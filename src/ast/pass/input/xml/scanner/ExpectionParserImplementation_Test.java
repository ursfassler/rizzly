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

package ast.pass.input.xml.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ast.meta.MetaInformation;
import ast.meta.MetaList;
import ast.meta.MetaListImplementation;
import ast.pass.input.xml.infrastructure.XmlParseError;
import error.ErrorType;
import error.RizzlyError;
import parser.PeekNReader;

public class ExpectionParserImplementation_Test {
  final private PeekNReader<XmlToken> stream = mock(PeekNReader.class);
  final private RizzlyError error = mock(RizzlyError.class);
  final private ExpectionParserImplementation testee = new ExpectionParserImplementation(stream, error);
  final private MetaInformation meta = mock(MetaInformation.class);
  final private MetaList metalist = new MetaListImplementation();

  {
    metalist.add(meta);
  }

  @Test
  public void returns_true_if_next_is_an_element() {
    when(stream.peek(eq(0))).thenReturn(XmlTokenFactory.elementStart("", meta));

    assertTrue(testee.hasElement());
  }

  @Test
  public void can_peek_an_element() {
    when(stream.peek(eq(0))).thenReturn(XmlTokenFactory.elementStart("the element", meta));

    assertEquals("the element", testee.peekElement());
  }

  @Test
  public void does_nothing_for_correctly_expecting_element_start() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementStart("the element name", meta));

    testee.elementStart("the element name");

    verify(stream).next();
  }

  @Test(expected = XmlParseError.class)
  public void exception_is_throw_for_wrong_element_name() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementStart("the wrong name", meta));

    testee.elementStart("the element name");
  }

  @Test
  public void log_error_for_an_unexpected_element_name() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementStart("the provided name", meta));

    try {
      testee.elementStart("the expected name");
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("expected \"the expected name\" for ElementStart, got \"the provided name\""), eq(metalist));
  }

  @Test(expected = XmlParseError.class)
  public void exception_is_throw_when_expecing_element_start_but_other_type_is_next() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementEnd(meta));

    testee.elementStart("the element name");
  }

  @Test
  public void log_error_when_expecing_element_start_but_other_type_is_next() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementEnd(meta));

    try {
      testee.elementStart("the expected name");
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("expected ElementStart, got type ElementEnd"), eq(metalist));
  }

  @Test
  public void does_nothing_for_correctly_expecting_element_end() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementEnd(meta));

    testee.elementEnd();

    verify(stream).next();
  }

  @Test
  public void return_the_value_of_the_expected_attribute() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementStart("", attr("the attribute", "the attribute value"), meta));
    testee.elementStart("");

    assertEquals("the attribute value", testee.attribute("the attribute"));
  }

  @Test
  public void log_error_for_an_unexpected_attribute() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementStart("the element", attr("provided attribute", ""), meta));
    testee.elementStart("the element");

    try {
      testee.attribute("expected attribute");
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("missing attribute \"expected attribute\" for element \"the element\""), eq(metalist));
  }

  @Test(expected = XmlParseError.class)
  public void exception_is_throw_for_wrong_attribute() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementStart("the element", attr("provided attribute", ""), meta));
    testee.elementStart("the element");

    testee.attribute("expected attribute");
  }

  @Test
  public void return_the_value_of_an_optional_attribute_when_available() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementStart("", attr("the attribute", "the attribute value"), meta));
    testee.elementStart("");

    assertEquals("the attribute value", testee.attribute("the attribute", ""));
  }

  @Test
  public void return_the_default_value_of_an_optional_attribute_when_not_available() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementStart("", meta));
    testee.elementStart("");

    assertEquals("the default value", testee.attribute("the attribute", "the default value"));
  }

  @Test
  public void log_error_if_attribute_is_requested_for_token_different_from_start_element() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementEnd(meta));
    testee.elementEnd();

    try {
      testee.attribute("expected attribute");
    } catch (XmlParseError e) {
    }

    verify(error).err(eq(ErrorType.Error), eq("expected ElementStart, got type ElementEnd"), eq(metalist));
  }

  @Test(expected = XmlParseError.class)
  public void exception_is_throw_if_attribute_is_requested_for_token_different_from_start_element() {
    when(stream.next()).thenReturn(XmlTokenFactory.elementEnd(meta));
    testee.elementEnd();

    testee.attribute("expected attribute");
  }

  private Map<String, String> attr(String name, String value) {
    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put(name, value);
    return attributes;
  }

}
